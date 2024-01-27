package com.gattagdev.newcommands

import com.gattagdev.defered.BS
import com.gattagdev.defered.DS
import com.gattagdev.units.milli
import edu.wpi.first.wpilibj.event.EventLoop
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.Subsystem
import edu.wpi.first.wpilibj2.command.button.Trigger

infix operator fun Command.plus(right: Command): Command = Commands.sequence(this, right)
infix operator fun Command.times(right: Command): Command = Commands.parallel(this, right)
infix operator fun Command.div(right: Command): Command = Commands.race(this, right)
infix operator fun Command.rem(right: Command): Command = Commands.deadline(right, this)
operator fun Command.unaryPlus(): Command = Commands.repeatingSequence(this)


class CommandBuilder internal constructor(){

    private var inInit = false
    private val periodics: MutableList<PeriodicBuilder.() -> Unit> = ArrayList()
    private val conditions: MutableList<PeriodicBuilder.() -> Boolean> = ArrayList()
    private val ends: MutableList<(Boolean) -> Unit> = ArrayList()

    internal fun clear(){
        periodics.clear()
        conditions.clear()
        ends.clear()
    }

    private fun checkInInit(){
        if(this.inInit) return
        throw IllegalStateException("")
    }

    class PeriodicBuilder(val startTime: Double, val lastTime: Double, val thisTime: Double){

        val timeDiff get() = thisTime - lastTime
        val elapsedTime get() = thisTime - startTime
    }

    fun periodic(body: PeriodicBuilder.() -> Unit): Unit{
        this.checkInInit()
        this.periodics.add(body)
    }

    fun finish(condition: PeriodicBuilder.() -> Boolean){
        this.checkInInit()
        this.conditions.add(condition)
    }

    fun immediate(): Unit {
        this.finish { true }
    }

    fun deadline(time: DS){
        val startTime = System.currentTimeMillis()
        finish { System.currentTimeMillis() > startTime + time() * 1000.0 }
    }

    fun onEnd(body: (interrupted: Boolean) -> Unit){
        this.checkInInit()
        this.ends.add(body)
    }

    internal class BuiltCommand(
        private val init: CommandBuilder.() -> Unit,
        private val runDisabled: Boolean,
        vararg subsystems: Subsystem
    ) : Command(){

        init {
            this.addRequirements(*subsystems)
        }

        override fun runsWhenDisabled(): Boolean = runDisabled

        private val builder: CommandBuilder = CommandBuilder()

        private var periodicBuilder = PeriodicBuilder(0.0, 0.0, 0.0)

        override fun initialize() {
            this.builder.clear()
            this.periodicBuilder = PeriodicBuilder(
                System.currentTimeMillis().milli,
                System.currentTimeMillis().milli,
                System.currentTimeMillis().milli
            )
            try {
                this.builder.inInit = true
                init(this.builder)
            }finally {
                this.builder.inInit = false
            }
        }

        override fun execute() {
            periodicBuilder = PeriodicBuilder(
                periodicBuilder.startTime,
                periodicBuilder.thisTime,
                System.currentTimeMillis().milli
            )
            this.builder.periodics.forEach { it(periodicBuilder) }
        }

        override fun end(interrupted: Boolean) {
            this.builder.ends.forEach { it(interrupted) }
        }

        override fun isFinished(): Boolean = this.builder.conditions.any { it(periodicBuilder) }
    }

}



fun command(
    vararg subsystems: Subsystem,
    runDisabled: Boolean = false,
    init: CommandBuilder.() -> Unit
): Command = CommandBuilder.BuiltCommand(init, runDisabled, *subsystems)


class CommandWhen(val baseCondition: BS){

    val cases: MutableList<Pair<BS, Command>> = ArrayList()
    private var runningCommand: Command? = null

    fun runLoop(){
        if(baseCondition()){
            val nextCommand = cases.find { it.first() }?.second
            if(nextCommand !== runningCommand){
                runningCommand?.cancel()
                runningCommand = nextCommand
                runningCommand?.schedule()
            }
        }else{
            runningCommand?.cancel()
            runningCommand = null
        }
    }
}

class CommandWhenBuilder(private val cw: CommandWhen){
    infix fun BS.then(command: Command){
        cw.cases.add(Pair(this, command))
    }

    fun sink(supplier: BS){
        cw.cases.add(Pair(supplier, command{ immediate()}))
    }

    fun always(command: Command){
        cw.cases.add(Pair({true}, command))
    }
}


fun commandWhen(
    eventLoop: EventLoop = CommandScheduler.getInstance().defaultButtonLoop,
    baseCondition: BS = { true },
    body: CommandWhenBuilder.() -> Unit
){
    val cw = CommandWhen(baseCondition)
    body(CommandWhenBuilder(cw))
    eventLoop.bind { cw.runLoop() }
}
