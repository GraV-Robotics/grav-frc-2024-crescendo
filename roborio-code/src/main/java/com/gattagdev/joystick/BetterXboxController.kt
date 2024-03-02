package com.gattagdev.joystick

import com.gattagdev.defered.BS
import com.gattagdev.defered.DS
import edu.wpi.first.wpilibj.XboxController

class BetterXboxController(val port: Int) {

    val base = XboxController(port)

    // Button States
    val xButton: BS get() = { base.xButton }
    val aButton: BS get() = { base.aButton }
    val bButton: BS get() = { base.bButton }
    val yButton: BS get() = { base.yButton }
    val leftBumper: BS get() = { base.leftBumper }
    val rightBumper: BS get() = { base.rightBumper }
    val backButton: BS get() = { base.backButton }
    val startButton: BS get() = { base.startButton }
    val leftStickButton: BS get() = { base.leftStickButton }
    val rightStickButton: BS get() = { base.rightStickButton }

    // Axis and Trigger States
    val leftTrigger: DS get() = { base.leftTriggerAxis }
    val rightTrigger: DS get() = { base.rightTriggerAxis }
    val leftStickX: DS get() = { base.leftX }
    val leftStickY: DS get() = { -base.leftY }
    val rightStickX: DS get() = { base.rightX }
    val rightStickY: DS get() = { base.rightY }

    // D-Pad (POV) States
    val povDegrees: DS get() = { base.pov.toDouble() }

    // Straight directions
    val dPadUp: BS get() = { base.pov == 0 }
    val dPadRight: BS get() = { base.pov == 90 }
    val dPadDown: BS get() = { base.pov == 180 }
    val dPadLeft: BS get() = { base.pov == 270 }

    // Diagonal directions
    val dPadUpRight: BS get() = { base.pov == 45 }
    val dPadDownRight: BS get() = { base.pov == 135 }
    val dPadDownLeft: BS get() = { base.pov == 225 }
    val dPadUpLeft: BS get() = { base.pov == 315 }
}
