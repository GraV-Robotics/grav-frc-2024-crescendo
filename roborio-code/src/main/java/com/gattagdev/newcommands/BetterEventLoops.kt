package com.gattagdev.newcommands

import com.gattagdev.defered.BS
import com.gattagdev.defered.not
import com.gattagdev.internal.builderScope
import com.gattagdev.misc.always
import com.gattagdev.nt.quickRW
import edu.wpi.first.wpilibj.event.EventLoop
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty


@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@DslMarker
annotation class EventLoopContextDSLMarker

typealias ELC_BODY = @EventLoopContextDSLMarker EventLoopContext.() -> Unit

@EventLoopContextDSLMarker
interface EventLoopContext{
    fun addExecutable(executable: EventLoopExecutable)

    fun onStart(executable: EventLoopExecutable.() -> Unit) = addExecutable(quickExecutable(start = executable))
    fun onPeriodic(executable: EventLoopExecutable.() -> Unit) = addExecutable(quickExecutable(periodic = executable))
    fun onStop(executable: EventLoopExecutable.() -> Unit) = addExecutable(quickExecutable(stop = executable))



    infix operator fun BS.invoke(body: (@EventLoopContextDSLMarker EventLoopContext).() -> Unit){
        val executables = executablesFromBody(body)
        val condition = this@BS
        addExecutable(ConditionalExecutable(condition, RunAllExecutable(executables)))
    }
    infix fun Command.whileTrue(condition: BS) = condition {
        addExecutable(object: EventLoopExecutable {
            override fun start() = schedule()
            override fun periodic() = Unit
            override fun stop() = cancel()
        })
    }
    infix fun Command.onTrue(condition: BS) = condition {
        addExecutable(object: EventLoopExecutable {
            override fun start() = schedule()
            override fun periodic() = Unit
            override fun stop() = Unit
        })
    }
    infix fun Command.whileFalse(condition: BS) = whileTrue(!condition)
    infix fun Command.onFalse(condition: BS) = onTrue(!condition)


    infix fun <T: Any?, F: () -> T> F.onChange(changeHandler: @EventLoopContextDSLMarker (T) -> Unit): F{
        this.onChange{prev, post -> changeHandler(post)}
        return this
    }
    infix fun <T: Any?, F: () -> T> F.onChange(changeHandler: @EventLoopContextDSLMarker (T?, T) -> Unit): F{
        var first = true
        var last: T? = null
        addExecutable(object: EventLoopExecutable {
            override fun periodic() {
                val current = this@onChange()
                if(first || last != current){
                    changeHandler(last, current)
                    first = false
                }
                last = current
            }
        })
        return this
    }
}

@EventLoopContextDSLMarker
interface EventLoopExecutable {
    fun start() { }
    fun periodic() { }
    fun stop() { }
}

internal class RunAllExecutable(private val executables: List<EventLoopExecutable>): EventLoopExecutable {
    override fun start() = Unit

    private var started = false

    override fun periodic() {
        executables.forEach {
            if(!started) it.start()
            it.periodic()
        }
        started = true
    }

    override fun stop() {
        executables.forEach{ it.stop() }
        started = false
    }
}

internal class ConditionalExecutable(private val condition: BS, private val executable: EventLoopExecutable):
    EventLoopExecutable {
    override fun start() = Unit

    private var started = false

    override fun periodic() {
        if(condition()){
            if(!started){
                started = true
                executable.start()
            }
            executable.periodic()
        } else if(started) stop()
    }

    override fun stop() {
        executable.stop()
        started = false
    }
}

@PublishedApi
internal inline fun executablesFromBody(body: ELC_BODY): List<EventLoopExecutable>{
    val executables = mutableListOf<EventLoopExecutable>()
    builderScope {
        val context = object : EventLoopContext {
            override fun addExecutable(executable: EventLoopExecutable) {
                tryRun { executables.add(executable) }
            }
        }
        body(context)
    }
    return executables
}

inline fun eventLoopContext(
    eventLoop: EventLoop = CommandScheduler.getInstance().defaultButtonLoop,
    period: Double = 0.02,
    body: @EventLoopContextDSLMarker EventLoopContext.() -> Unit
){
    val executables = executablesFromBody(body)
    var first = true
    eventLoop.bind {
        withTimeInfo(ELTimeInfo(period, time)) {
            executables.forEach {
                if (first) it.start()
                it.periodic()
            }
        }
        first = false
    }
}


inline fun <reified T> EventLoopContext.setOnStart(
    crossinline init: EventLoopExecutable.() -> T
) = PropertyDelegateProvider { thisRef: Any?, property: KProperty<*> ->
    var value: T? = null
    onStart { value = init() }
    quickRW({ value as T }, { value = it })
}

@EventLoopContextDSLMarker
interface SwitchContext {
    fun case(condition: BS, body: (@EventLoopContextDSLMarker EventLoopContext).() -> Unit): Unit
}

fun EventLoopContext.switch(body: @EventLoopContextDSLMarker SwitchContext.() -> Unit){
    val cases = mutableListOf<Pair<BS, EventLoopExecutable>>()
    builderScope {
        val context = object: SwitchContext {
            override fun case(condition: BS, body: (@EventLoopContextDSLMarker EventLoopContext).() -> Unit) { tryRun {
                cases.add(Pair(condition, RunAllExecutable(executablesFromBody(body))))
            } }
        }
        body(context)
    }
    cases.add(Pair(always, object: EventLoopExecutable {
        override fun start() = Unit
        override fun periodic() = Unit
        override fun stop() = Unit
    }))

    var current: EventLoopExecutable? = null
    val executable = object: EventLoopExecutable {
        override fun start() = Unit

        override fun periodic() {
            for(case in cases){
                if(case.first()){
                    if(current != case.second) this.stop()

                    if(current == case.second) {

                    } else {
                        current = case.second
                        case.second.start()
                    }
                    break
                }
            }
            current?.periodic()
        }

        override fun stop() {
            if(current != null){
                current!!.stop()
                current = null
            }
        }
    }
    addExecutable(executable)
}

fun quickExecutable(
    start: EventLoopExecutable.() -> Unit = {},
    stop: EventLoopExecutable.() -> Unit = {},
    periodic: EventLoopExecutable.() -> Unit = {}
): EventLoopExecutable {
    return object: EventLoopExecutable {
        override fun start() = start(this)
        override fun stop() = stop(this)
        override fun periodic() = periodic(this)
    }
}

