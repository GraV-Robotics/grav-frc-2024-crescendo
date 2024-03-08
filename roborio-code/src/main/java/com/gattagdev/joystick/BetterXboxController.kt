package com.gattagdev.joystick

import com.gattagdev.defered.BS
import com.gattagdev.defered.DS
import com.gattagdev.defered.and
import com.gattagdev.defered.not
import com.gattagdev.newcommands.ConditionalExecutable
import com.gattagdev.newcommands.EventLoopContext
import com.gattagdev.newcommands.EventLoopExecutable
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.XboxController
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

interface BetterXboxController {

    val xButton: BS
    val aButton: BS
    val bButton: BS
    val yButton: BS
    val leftBumper: BS
    val rightBumper: BS
    val backButton: BS
    val startButton: BS
    val leftStickButton: BS
    val rightStickButton: BS
    val leftTrigger: DS
    val rightTrigger: DS
    val leftStickX: DS
    val leftStickY: DS
    val rightStickX: DS
    val rightStickY: DS
    val povDegrees: DS

    companion object{
        operator fun invoke(port: Int): BetterXboxController = BetterXboxControllerImpl(port)
    }
}

val BetterXboxController.dPadUp: BS get() = { povDegrees() == 0.0 }
val BetterXboxController.dPadRight: BS get() = { povDegrees() == 90.0 }
val BetterXboxController.dPadDown: BS get() = { povDegrees() == 180.0 }
val BetterXboxController.dPadLeft: BS get() = { povDegrees() == 270.0 }

// Diagonal directions
val BetterXboxController.dPadUpRight: BS get() = { povDegrees() == 45.0 }
val BetterXboxController.dPadDownRight: BS get() = { povDegrees() == 135.0 }
val BetterXboxController.dPadDownLeft: BS get() = { povDegrees() == 225.0 }
val BetterXboxController.dPadUpLeft: BS get() = { povDegrees() == 315.0 }

internal class BetterXboxControllerImpl(port: Int) : BetterXboxController {

    private val base = XboxController(port)

    override val aButton: BS get() = { base.aButton }
    override val bButton: BS get() = { base.bButton }
    override val xButton: BS get() = { base.xButton }
    override val yButton: BS get() = { base.yButton }
    override val leftBumper: BS get() = { base.leftBumper }
    override val rightBumper: BS get() = { base.rightBumper }
    override val backButton: BS get() = { base.backButton }
    override val startButton: BS get() = { base.startButton }
    override val leftStickButton: BS get() = { base.leftStickButton }
    override val rightStickButton: BS get() = { base.rightStickButton }
    override val leftTrigger: DS get() = { base.leftTriggerAxis }
    override val rightTrigger: DS get() = { base.rightTriggerAxis }
    override val leftStickX: DS get() = { base.leftX }
    override val leftStickY: DS get() = { -base.leftY } // Inverted Y axis
    override val rightStickX: DS get() = { base.rightX }
    override val rightStickY: DS get() = { base.rightY }
    override val povDegrees: DS get() = { base.pov.toDouble() }
}





