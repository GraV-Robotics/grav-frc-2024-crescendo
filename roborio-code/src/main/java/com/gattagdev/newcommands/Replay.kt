@file:OptIn(ExperimentalContracts::class)

package com.gattagdev.newcommands

import com.gattagdev.defered.BS
import com.gattagdev.internal.builderScope
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.Notifier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class RecordingDescriptor(
    val partDescriptors: List<RecoringPartDescriptor>
)

@Serializable
data class RecoringPartDescriptor(
    val relativePath: String,
    val size: Int,
)

@Serializable
data class RecordingPart(
    val components: List<ComponentRecording>
)

@Serializable
data class ComponentRecording(
    val name: String,
    val frames: List<ByteArray>
)


class RecordingLoader(
    val path: Path,
    val replayComponents: List<ReplayComponent<*>>
): Closeable{

    private val descriptorPath = path.resolve("descriptor")

    val closed: Boolean get() = replayExecutor.isShutdown
    private val replayExecutor = Executors.newSingleThreadExecutor()

    private val descriptor = ProtoBuf.decodeFromByteArray(
        RecordingDescriptor.serializer(),
        Files.readAllBytes(descriptorPath)
    )
    private val totalFrames = descriptor.partDescriptors.sumOf { it.size }
    private class ComponentRecordingLoader<T: Any>(val component: ReplayComponent<T>){
        val frames = LinkedBlockingQueue<T>()
        var lastConsumed = -1
    }
    private val componentLoaders = replayComponents.map { it to ComponentRecordingLoader(it) }.toMap()
    private val <T: Any> ReplayComponent<T>.loader: ComponentRecordingLoader<T> get() = componentLoaders[this@loader]!! as ComponentRecordingLoader<T>
    private var loadingPart = -1
    private var loadingFrame = -1
    private fun loadPart(partIndex: Int){
        if(partIndex < 0 || partIndex >= descriptor.partDescriptors.size) return
        if(partIndex <= loadingPart) return
        if(partIndex > loadingPart + 1) loadPart(partIndex - 1)
        val partDescriptor = descriptor.partDescriptors[partIndex]
        loadingPart = partIndex
        loadingFrame += partDescriptor.size
        replayExecutor.submit{
            val partPath = path.resolve(partDescriptor.relativePath)
            val partData = ProtoBuf.decodeFromByteArray(
                RecordingPart.serializer(),
                Files.readAllBytes(partPath)
            )
            for (comp in replayComponents){
                val frames = partData.components.find { it.name == comp.name }?.frames!!
                val cLoader = comp.loader
                frames.forEach {
                    val frame = ProtoBuf.decodeFromByteArray(comp.serializer, it)
                    (cLoader.frames as BlockingQueue<Any>).add(frame)
                }
            }
        }
    }

    private fun loadFrame(frameIndex: Int){
        if(frameIndex < 0 || frameIndex >= totalFrames) return
        if(frameIndex <= loadingFrame) return
        do {
            loadPart(loadingPart + 1)
        } while (frameIndex > loadingFrame)
    }

    operator fun <T: Any> get(component: ReplayComponent<T>): T?{
        val cl = component.loader
        if(cl.lastConsumed >= totalFrames - 1) return null
        loadFrame(cl.lastConsumed + 100)
        cl.lastConsumed++
        return cl.frames.poll(100, TimeUnit.MILLISECONDS)
    }

    override fun close() {
        replayExecutor.shutdownNow()
    }

    fun awaitClose(){
        replayExecutor.awaitTermination(500, TimeUnit.SECONDS)
    }

}

val frameBucketSize = 50

class Recorder(
    val path: Path,
    val replayComponents: List<ReplayComponent<*>>
): Closeable{
    var startedFullSave = false

    val closed: Boolean get() = executor.isShutdown
    private val executor = Executors.newSingleThreadExecutor()

    private var nextPartIndex = 0
    private val partDescriptors = mutableListOf<RecoringPartDescriptor>()

    class ComponentRecorder<T: Any>(val component: ReplayComponent<T>){
        var frameBucket = ArrayList<T>(frameBucketSize)
        fun newFrameBucket(){ frameBucket = ArrayList(frameBucketSize) }
    }

    private val componentRecorders = replayComponents.map { it to Recorder.ComponentRecorder(it) }.toMap()
    private val <T: Any> ReplayComponent<T>.recorder get() = componentRecorders[this]!! as ComponentRecorder<T>

    operator fun <T: Any> set(component: ReplayComponent<T>, value: T): Unit{
        component.recorder.frameBucket.add(value)
    }

    fun savePart(minSize: Int = frameBucketSize){
        val partIndex = nextPartIndex++
        val recordingMap = componentRecorders.map { (comp, rec) -> comp to rec.frameBucket }.toMap()
        val partSize = recordingMap.values.first().size
        if(partSize < minSize) return
        componentRecorders.forEach{ it.value.newFrameBucket() }
        val relativePath = "./part$partIndex"
        partDescriptors += RecoringPartDescriptor(
            relativePath = relativePath,
            size = partSize
        )
        executor.submit{
            val partPath = path.resolve(relativePath)
            val partData = RecordingPart(
                components = recordingMap.map { (comp, frames) ->
                    ComponentRecording(
                        name = comp.name,
                        frames = frames.map { ProtoBuf.encodeToByteArray(comp.serializer as KSerializer<Any>, it) }
                    )
                }
            )
            Files.write(partPath, ProtoBuf.encodeToByteArray(RecordingPart.serializer(), partData))
        }
    }

    fun saveDescriptor() {
        val descriptor = RecordingDescriptor(partDescriptors)
        executor.submit{
            Files.write(
                path.resolve("descriptor"),
                ProtoBuf.encodeToByteArray(RecordingDescriptor.serializer(), descriptor)
            )
        }
    }

    fun fullSave(){
        startedFullSave = true
        savePart(minSize = 1)
        saveDescriptor()
        executor.shutdown()
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }

    override fun close() {
        executor.shutdownNow()
    }
    fun awaitClose(){
        executor.awaitTermination(500, TimeUnit.MILLISECONDS)
    }
}

abstract class ReplayComponent<T: Any>{
    abstract val name: String
    abstract var serializer: KSerializer<T>

    open fun elStart() { }
    open fun elStop() { }
    abstract fun periodic(state: T?)
    abstract fun record(): T
}


interface ReplayBuilder{
    var path: () -> Path
    var record: BS
    var replay: BS
    var replayReady: BS
    fun addReplayComponent(component: ReplayComponent<*>)
}

@OptIn(ExperimentalContracts::class)
inline fun <reified RET> EventLoopContext.replayManager(builder: @EventLoopContextDSLMarker ReplayBuilder.() -> RET): RET {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }

    var pathSupplier: (() -> Path)? = null
    var recordSupplier: BS? = null
    var replaySupplier: BS? = null
    var replayReadySupplier: BS? = null
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
            override var replayReady: BS
                get() = replayReadySupplier!!
                set(value) { tryRun { replayReadySupplier = value } }
            override fun addReplayComponent(component: ReplayComponent<*>) {
                tryRun { components += component }
            }
        })
    }


    val basePath = Filesystem.getOperatingDirectory().toPath().resolve("recordings2")
    Files.createDirectories(basePath)
    // This is a really lazy way to do the check, I'll get around to it later
    val path = pathSupplier!!.let { { basePath.resolve(it()) } }
    val record = recordSupplier!!
    val replay = replaySupplier!!
    val replayReady = replayReadySupplier!!



    addExecutable(object: EventLoopExecutable{

        val idleMode = quickExecutable { components.forEach { it.periodic(null) } }

        val recordMode = object: EventLoopExecutable{

            var recorder: Recorder? = null
            var framesSinceSave = 0

            override fun start() {
                val pathRead = path()
                Files.createDirectories(pathRead)
                recorder = Recorder(pathRead, components)
                framesSinceSave = 0
            }

            override fun periodic() {
                components.forEach { it.periodic(null) }
                components.forEach {
                    val rc = it as ReplayComponent<Any>
                    recorder!![rc] = rc.record()
                }
                framesSinceSave++
                if(framesSinceSave >= frameBucketSize){
                    recorder!!.savePart()
                    framesSinceSave = 0
                }
            }

            override fun stop() {
                recorder!!.fullSave()
                recorder = null
            }
        }


        val replayMode = object: EventLoopExecutable {

            var loader: RecordingLoader? = null
            var replayStarted = false

            override fun start() {
                loader = RecordingLoader(path(), components)
                replayStarted = false
            }

            override fun periodic() {
                val isReplay = replay()
                if(isReplay && !replayStarted) replayStarted = true;
                if(replayStarted && !isReplay) lastMode = null;
                components.forEach {
                    val rc = it as ReplayComponent<Any>
                    rc.periodic(if(isReplay) loader?.get(rc) else null)
                }
            }

            override fun stop() {
                loader?.close()
                loader?.awaitClose()
                loader = null
            }
        }

        var lastPath: Path? = null
        var validPath = false
        var lastMode: EventLoopExecutable? = null
        var currentMode: EventLoopExecutable = idleMode

        override fun start() {
            lastPath = null
            validPath = false
            lastMode = null
            currentMode = idleMode
            components.forEach { it.elStart() }
        }

        override fun periodic() {
            val pathRead = path()
            val recordRead = record()
            val replayRead = replay()
            val replayReadyRead = replayReady()

            currentMode = if (lastPath != pathRead){
                lastPath = pathRead
                validPath = Files.isDirectory(pathRead)
                idleMode
            } else if (replayRead) replayMode
            else if(recordRead) recordMode
            else if(replayReadyRead && validPath) replayMode
            else idleMode ;

            if(lastMode != currentMode){
                lastMode?.stop()
                currentMode.start()
                lastMode = currentMode
            }
            currentMode.periodic()
        }

        override fun stop() {
            components.forEach { it.elStop() }
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


