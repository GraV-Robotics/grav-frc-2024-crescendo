package org.grav.y2024

import com.gattagdev.units.feet
import com.gattagdev.units.inches
import com.gattagdev.units.rotations

object Constants {
    //Robot Base
    val robotMaxSpeed = 22.feet
    val robotMaxRotationRate = 2.0.rotations

    //Climbing
    val climbingPos = 12.0.inches
    val retractedPos = 0.0.inches
    val climbMaxHeight = 14.0.inches
    val climbMinHeight = 0.0.inches

    //Intake
    val intakeSpeed = 29.0.feet
    val topIntakeWheelDiameter = 2.0.inches
    val bottomIntakeWheelDiameter = 1.0.inches

    //Trigger
    val triggerSpeed = 1.0.feet
    val triggerWheelDiameter = 2.0.inches
    val waitTime = 2.0

    //Shooter
    val shooterWheelAmpSpeed = 2.feet
    val shooterWheelSpeakerSpeed = 15.feet
    val shooterWheelDiameter = 4.0.inches
    val shooterFactor = 60.0/((shooterWheelDiameter/2)*Math.PI*2)

    //Reverse
    val motorsReverseSpeed = -2.0.feet

    //Motors
    //NEO
    val neoCurrentLimit = 40
    val neoVoltageCompensation = 12.0
    val neoVoltage = 12.0
    //NEO550
    val neo550CurrentLimit = 20
    val neo550VoltageCompensation = 12.0
    val neo550Voltage = 12.0

}