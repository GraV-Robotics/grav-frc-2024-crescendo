package com.gattagdev.misc

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.util.WPILibVersion


fun reportKotlinUsage(){
    HAL.report(FRCNetComm.tResourceType.kResourceType_Language, FRCNetComm.tInstances.kLanguage_Kotlin, 0, WPILibVersion.Version)
}

val TELEOPERATED = DriverStation::isTeleopEnabled
val AUTONOMOUS   = DriverStation::isAutonomousEnabled
val always = { true }
