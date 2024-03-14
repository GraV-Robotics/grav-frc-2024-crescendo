package com.gattagdev.defered

import edu.wpi.first.wpilibj.Joystick

infix fun Joystick.buttonDown(button: Int): BS = { this.getRawButton(button) }
infix fun Joystick.axis(axis: Int): DS = { this.getRawAxis(axis) }