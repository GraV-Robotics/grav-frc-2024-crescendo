package com.gattagdev.joystick

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Joystick


fun Joystick.setLeftRumble(value: Double){
    this.setRumble(GenericHID.RumbleType.kLeftRumble, value)
}
fun Joystick.setRightRumble(value: Double){
    this.setRumble(GenericHID.RumbleType.kRightRumble, value)
}
