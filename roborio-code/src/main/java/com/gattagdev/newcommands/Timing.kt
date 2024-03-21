package com.gattagdev.newcommands

import com.gattagdev.misc.Context
import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.Timer

internal var _timeSupplier: TimeSupplier = BaseTimeSupplier

interface TimeSupplier {
    val time: Double
    val matchTime: Double
    val timeNano: Long
}

val time get() = _timeSupplier.time
val matchTime get() = _timeSupplier.matchTime
val timeNano get() = _timeSupplier.timeNano

object BaseTimeSupplier : TimeSupplier {
    override val time: Double get() = Timer.getFPGATimestamp()
    override val matchTime: Double get() = Timer.getMatchTime()
    override val timeNano: Long get() = RobotController.getFPGATime() * 1000
}


internal val timeInfoContext = Context<ELTimeInfo?>(null)

data class ELTimeInfo(
    val period: Double,
    val recognizedTime: Double
)

val EventLoopExecutable.period: Double get() = timeInfoContext.value!!.period
val EventLoopExecutable.recognizedTime: Double get() = timeInfoContext.value!!.recognizedTime

fun <R> withTimeInfo(timeInfo: ELTimeInfo, block: () -> R): R {
    return timeInfoContext.runWith(timeInfo, block)
}

