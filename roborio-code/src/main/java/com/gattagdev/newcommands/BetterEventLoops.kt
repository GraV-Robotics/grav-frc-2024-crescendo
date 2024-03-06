package com.gattagdev.newcommands

import com.gattagdev.defered.BS
import com.gattagdev.defered.not
import com.gattagdev.internal.builderScope
import com.gattagdev.misc.always
import edu.wpi.first.wpilibj.event.EventLoop
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler

@DslMarker
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class EventLoopContextDSLMarker

typealias ELC_BODY = @EventLoopContextDSLMarker EventLoopContext.() -> Unit

@EventLoopContextDSLMarker
interface EventLoopContext{
    fun addExecutable(executable: EventLoopExecutable)
}

interface EventLoopExecutable {
    fun start()
    fun periodic()
    fun stop()
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

internal class ConditionalExecutable(private val condition: BS, private val executable: EventLoopExecutable): EventLoopExecutable{
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
    body: @EventLoopContextDSLMarker EventLoopContext.() -> Unit
){
    val executables = executablesFromBody(body)
    var first = true
    eventLoop.bind {
        executables.forEach {
            if(first) it.start()
            it.periodic()
        }
        first = false
    }
}

class Flag<T>(private val init: T): () -> T{
    var value: T = init

    infix fun set(value: T) = command{ periodic { this@Flag.value = value } }

    internal fun reset(){
        value = init
    }

    override fun invoke(): T = value
}

fun <T> EventLoopContext.flag(init: T): Flag<T> {
    val flag = Flag(init)
    this.addExecutable(object: EventLoopExecutable{
        override fun start() = Unit
        override fun periodic() = flag.reset()
        override fun stop() = Unit
    })
    return flag
}

context(EventLoopContext)
infix operator fun BS.invoke(body: @EventLoopContextDSLMarker EventLoopContext.() -> Unit){
    val executables = executablesFromBody(body)
    val condition = this@BS
    addExecutable(ConditionalExecutable(condition, RunAllExecutable(executables)))
}


context(EventLoopContext)
infix fun Command.whileTrue(condition: BS) = condition {
    addExecutable(object: EventLoopExecutable {
        override fun start() = schedule()
        override fun periodic() = Unit
        override fun stop() = cancel()
    })
}

context(EventLoopContext)
infix fun Command.onTrue(condition: BS) = condition {
    addExecutable(object: EventLoopExecutable {
        override fun start() = schedule()
        override fun periodic() = Unit
        override fun stop() = Unit
    })
}

context(EventLoopContext)
infix fun Command.whileFalse(condition: BS) = whileTrue(!condition)
context(EventLoopContext)
infix fun Command.onFalse(condition: BS) = onTrue(!condition)

@EventLoopContextDSLMarker
interface SwitchContext {
    fun case(condition: BS, body: @EventLoopContextDSLMarker EventLoopContext.() -> Unit): Unit
}



fun EventLoopContext.switch(body: @EventLoopContextDSLMarker SwitchContext.() -> Unit){
    val cases = mutableListOf<Pair<BS, EventLoopExecutable>>()
    builderScope {
        val context = object: SwitchContext{
            override fun case(condition: BS, body: @EventLoopContextDSLMarker EventLoopContext.() -> Unit) { tryRun {
                cases.add(Pair(condition, RunAllExecutable(executablesFromBody(body))))
            } }
        }
        body(context)
    }
    cases.add(Pair(always, object: EventLoopExecutable{
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

                    if(current == case.second) Unit
                    else {
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