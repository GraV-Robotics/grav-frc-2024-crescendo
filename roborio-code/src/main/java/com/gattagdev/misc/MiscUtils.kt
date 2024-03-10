package com.gattagdev.misc

import com.gattagdev.defered.DS
import com.gattagdev.newcommands.EventLoopContext
import com.gattagdev.newcommands.EventLoopExecutable
import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.util.WPILibVersion
import kotlin.math.absoluteValue


fun reportKotlinUsage(){
    HAL.report(FRCNetComm.tResourceType.kResourceType_Language, FRCNetComm.tInstances.kLanguage_Kotlin, 0, WPILibVersion.Version)
}

val TELEOPERATED = DriverStation::isTeleopEnabled
val AUTONOMOUS   = DriverStation::isAutonomousEnabled
val always = { true }


context(EventLoopContext)
fun DS.makeContinuous(lower: Double, upper: Double, onStartValue: () -> Double? = { null }): DS {
    val maxJumpDistance = (upper - lower)/2
    val midpoint = lower + maxJumpDistance
    val inputStream = this

    var accumulation = 0.0
    var lastReading = 0.0
    addExecutable(object: EventLoopExecutable{
        override fun start() {
            lastReading = inputStream()
            onStartValue()?.also { accumulation = it }
        }

        override fun periodic() {
            if(lastReading == null) lastReading = inputStream();
            val last = lastReading
            val current = inputStream()
            lastReading = current
            val diff = (current - last)

            if(diff.absoluteValue > maxJumpDistance){
                accumulation += diff + (if(lastReading < midpoint) (lower - upper) else (upper - lower))
            }else{
                accumulation += diff
            }
        }
        override fun stop() { }
    })

    return { accumulation }
}

//context(EventLoopContext)
//fun DS.maintainHistory(recordings: Int): (Double) -> Double{
//
//    var
//
//}