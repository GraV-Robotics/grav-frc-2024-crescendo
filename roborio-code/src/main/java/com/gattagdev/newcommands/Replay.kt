@file:OptIn(ExperimentalContracts::class)

package com.gattagdev.newcommands

import com.gattagdev.defered.BS
import com.gattagdev.internal.builderScope
import edu.wpi.first.wpilibj.Filesystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class FullRecording(
    val components: List<ComponentRecording>
)

@Serializable
data class ComponentRecording(
    val name: String,
    val frames: List<JsonElement>
)

interface ReplayBuilder{
    var path: () -> Path
    var record: BS
    var replay: BS
    fun addReplayComponent(component: ReplayComponent<*>)
}

abstract class ReplayComponent<T: Any> {
    abstract val name: String
    abstract var serializer: KSerializer<T>
    open fun elStart() { }
    open fun elStop() { }
    abstract fun periodic(state: T?)
    abstract fun record(): T

    private val loadedFrames = mutableListOf<T>()
    @PublishedApi
    internal fun loadRecording(recording: ComponentRecording){
        loadedFrames.clear()
        recording.frames.forEach { loadedFrames += Json.decodeFromJsonElement(serializer, it) }
    }

    @PublishedApi
    internal fun clearRecording(){
        loadedFrames.clear()
    }

    private var started: Boolean = false
    @PublishedApi
    internal fun periodicInternal(frameId: Int?){
        periodic(frameId?.takeIf { it < loadedFrames.size }?.let { loadedFrames[it] })
    }

    private val recordedFrames = mutableListOf<T>()
    @PublishedApi
    internal fun startRecording(){
        recordedFrames.clear()
    }

    @PublishedApi
    internal fun recordInternal(){
        recordedFrames += record()
    }

    @PublishedApi
    internal fun captureRecording(): ComponentRecording{
        val ret = ComponentRecording(
            name = name,
            frames = recordedFrames.map { Json.encodeToJsonElement(serializer, it) }
        )
        recordedFrames.clear()
        return ret
    }


    @PublishedApi
    internal fun startInternal(){
        started = true
        elStart()
    }

    @PublishedApi
    internal fun stopInternal(){
        started = false
        elStop()
    }
}


inline fun <reified RET> EventLoopContext.replayManager(builder: @EventLoopContextDSLMarker ReplayBuilder.() -> RET): RET {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }

    var pathSupplier: (() -> Path)? = null
    var recordSupplier: BS? = null
    var replaySupplier: BS? = null
    val components = mutableListOf<ReplayComponent<*>>()
    val ret = builderScope {
        builder(object: ReplayBuilder{
            override var path: () -> Path
                get() = pathSupplier!!
                set(value) { tryRun { pathSupplier = value } }
            override var record: BS
                get() = recordSupplier!!
                set(value) { tryRun {  recordSupplier = value } }
            override var replay: BS
                get() = replaySupplier!!
                set(value) { tryRun { replaySupplier = value } }
            override fun addReplayComponent(component: ReplayComponent<*>) {
                tryRun { components += component }
            }
        })
    }


    val basePath = Filesystem.getOperatingDirectory().toPath().resolve("recordings")
    Files.createDirectories(basePath)
    // This is a really lazy way to do the check, I'll get around to it later
    val path = pathSupplier!!.let { { basePath.resolve(it()) } }
    val record = recordSupplier!!
    val replay = replaySupplier!!

    addExecutable(object: EventLoopExecutable{
        override fun start() {
            components.forEach { it.startInternal() }
        }

        var isRecording = false
        var isReplaying = false
        var frameId = 0
        override fun periodic() {
            val recordRead = record()
            val replayRead = replay()

            if(!isReplaying){
                if(!isRecording && recordRead) startRecording()
            } else {
                if(recordRead || !replayRead) stopReplaying()
            }

            if(!isRecording){
                if(!isReplaying && replayRead) startReplaying()
            } else{
                if(!recordRead) stopRecording()
            }

            val currentFrame = frameId++
            components.forEach {
                it.periodicInternal(if(isReplaying) currentFrame else null)
                if(isRecording) it.recordInternal()
            }
        }

        fun startRecording(){
            isRecording = true
            components.forEach { it.startRecording() }
        }

        fun stopRecording(){
            isRecording = false
            val recording = FullRecording(components = components.map { it.captureRecording() })
            Files.writeString(path(), Json.encodeToString(FullRecording.serializer(), recording))
        }

        fun startReplaying(){
            isReplaying = true
            frameId = 0
            val recording = Json.decodeFromString(FullRecording.serializer(), Files.readString(path()))
            val recordingMap = mutableMapOf<String, ComponentRecording>()
            recording.components.forEach{ recordingMap[it.name] = it }
            // This is also a really lazy check, I'll get around to it later
            components.forEach { it.loadRecording(recordingMap[it.name]!!) }
        }

        fun stopReplaying(){
            isReplaying = false
            components.forEach { it.clearRecording() }
        }

        override fun stop() {
            if(isRecording) stopRecording()
            if(isReplaying) stopReplaying()
            components.forEach { it.stopInternal() }
        }
    })

    return ret
}

context(ReplayBuilder)
inline fun <reified T: Any> (() -> T).replayable(key: String): () -> T {
    val realSupplier = this
    var currentSupplier = realSupplier

    addReplayComponent(object: ReplayComponent<T>() {
        override val name: String = key
        override var serializer = serializer<T>()
        override fun periodic(state: T?) {
            currentSupplier = if (state != null) { { state } } else realSupplier
        }
        override fun record(): T = currentSupplier()
    })

    return { currentSupplier() }
}
