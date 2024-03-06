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

    // Button States
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

    // Axis and Trigger States
    val leftTrigger: DS
    val rightTrigger: DS
    val leftStickX: DS
    val leftStickY: DS
    val rightStickX: DS
    val rightStickY: DS

    // D-Pad (POV) States
    val povDegrees: DS

    // Straight directions
    val dPadUp: BS
    val dPadRight: BS
    val dPadDown: BS
    val dPadLeft: BS

    // Diagonal directions
    val dPadUpRight: BS
    val dPadDownRight: BS
    val dPadDownLeft: BS
    val dPadUpLeft: BS

    companion object{
        operator fun invoke(port: Int): BetterXboxController = BetterXboxControllerImpl(port)
    }
}

internal class BetterXboxControllerImpl(port: Int) : BetterXboxController {

    private val base = XboxController(port)

    // Button States
    override val xButton: BS get() = { base.xButton }
    override val aButton: BS get() = { base.aButton }
    override val bButton: BS get() = { base.bButton }
    override val yButton: BS get() = { base.yButton }
    override val leftBumper: BS get() = { base.leftBumper }
    override val rightBumper: BS get() = { base.rightBumper }
    override val backButton: BS get() = { base.backButton }
    override val startButton: BS get() = { base.startButton }
    override val leftStickButton: BS get() = { base.leftStickButton }
    override val rightStickButton: BS get() = { base.rightStickButton }

    // Axis and Trigger States
    override val leftTrigger: DS get() = { base.leftTriggerAxis }
    override val rightTrigger: DS get() = { base.rightTriggerAxis }
    override val leftStickX: DS get() = { base.leftX }
    override val leftStickY: DS get() = { -base.leftY } // Inverted Y axis
    override val rightStickX: DS get() = { base.rightX }
    override val rightStickY: DS get() = { base.rightY }

    // D-Pad (POV) States
    override val povDegrees: DS get() = { base.pov.toDouble() }

    // Straight directions
    override val dPadUp: BS get() = { base.pov == 0 }
    override val dPadRight: BS get() = { base.pov == 90 }
    override val dPadDown: BS get() = { base.pov == 180 }
    override val dPadLeft: BS get() = { base.pov == 270 }

    // Diagonal directions
    override val dPadUpRight: BS get() = { base.pov == 45 }
    override val dPadDownRight: BS get() = { base.pov == 135 }
    override val dPadDownLeft: BS get() = { base.pov == 225 }
    override val dPadUpLeft: BS get() = { base.pov == 315 }
}

@Serializable
data class ControllerState(
    val xButton: Boolean = false,
    val aButton: Boolean = false,
    val bButton: Boolean = false,
    val yButton: Boolean = false,
    val leftBumper: Boolean = false,
    val rightBumper: Boolean = false,
    val backButton: Boolean = false,
    val startButton: Boolean = false,
    val leftStickButton: Boolean = false,
    val rightStickButton: Boolean = false,
    val leftTrigger: Double = 0.0,
    val rightTrigger: Double = 0.0,
    val leftStickX: Double = 0.0,
    val leftStickY: Double = 0.0,
    val rightStickX: Double = 0.0,
    val rightStickY: Double = 0.0,
    val povDegrees: Double = 0.0
)

internal val ZERO_STATE = ControllerState()

class ReplayableXboxController(private val base: BetterXboxController) : BetterXboxController {

    private var simulatedState: ControllerState? = null

    override val xButton: BS get() = { simulatedState?.xButton ?: base.xButton() }
    override val aButton: BS get() = { simulatedState?.aButton ?: base.aButton() }
    override val bButton: BS get() = { simulatedState?.bButton ?: base.bButton() }
    override val yButton: BS get() = { simulatedState?.yButton ?: base.yButton() }
    override val leftBumper: BS get() = { simulatedState?.leftBumper ?: base.leftBumper() }
    override val rightBumper: BS get() = { simulatedState?.rightBumper ?: base.rightBumper() }
    override val backButton: BS get() = { simulatedState?.backButton ?: base.backButton() }
    override val startButton: BS get() = { simulatedState?.startButton ?: base.startButton() }
    override val leftStickButton: BS get() = { simulatedState?.leftStickButton ?: base.leftStickButton() }
    override val rightStickButton: BS get() = { simulatedState?.rightStickButton ?: base.rightStickButton() }

    override val leftTrigger: DS get() = { simulatedState?.leftTrigger ?: base.leftTrigger() }
    override val rightTrigger: DS get() = { simulatedState?.rightTrigger ?: base.rightTrigger() }
    override val leftStickX: DS get() = { simulatedState?.leftStickX ?: base.leftStickX() }
    override val leftStickY: DS get() = { simulatedState?.leftStickY ?: base.leftStickY() }
    override val rightStickX: DS get() = { simulatedState?.rightStickX ?: base.rightStickX() }
    override val rightStickY: DS get() = { simulatedState?.rightStickY ?: base.rightStickY() }

    override val povDegrees: DS get() = { simulatedState?.povDegrees ?: base.povDegrees() }

    override val dPadUp: BS get() = { povDegrees() == 0.0 }
    override val dPadRight: BS get() = { povDegrees() == 90.0 }
    override val dPadDown: BS get() = { povDegrees() == 180.0 }
    override val dPadLeft: BS get() = { povDegrees() == 270.0 }

    // Diagonal directions
    override val dPadUpRight: BS get() = { povDegrees() == 45.0 }
    override val dPadDownRight: BS get() = { povDegrees() == 135.0 }
    override val dPadDownLeft: BS get() = { povDegrees() == 225.0 }
    override val dPadUpLeft: BS get() = { povDegrees() == 315.0 }

    fun record() = ControllerState(
            xButton = base.xButton(),
            aButton = base.aButton(),
            bButton = base.bButton(),
            yButton = base.yButton(),
            leftBumper = base.leftBumper(),
            rightBumper = base.rightBumper(),
            backButton = base.backButton(),
            startButton = base.startButton(),
            leftStickButton = base.leftStickButton(),
            rightStickButton = base.rightStickButton(),
            leftTrigger = base.leftTrigger(),
            rightTrigger = base.rightTrigger(),
            leftStickX = base.leftStickX(),
            leftStickY = base.leftStickY(),
            rightStickX = base.rightStickX(),
            rightStickY = base.rightStickY(),
            povDegrees = base.povDegrees()
        )


    fun replay(state: ControllerState) {
        simulatedState = state
    }

    fun passThrough(){
        simulatedState = null
    }
}

@Serializable
data class ControllerRecording(
    val states: List<ControllerState>
)


context(EventLoopContext)
fun BetterXboxController.replayable(path: () -> Path, record: BS, replay: BS): BetterXboxController {
    val base = this
    val replayController = ReplayableXboxController(base)

    val mainPath = { Filesystem.getOperatingDirectory().toPath().resolve(path()) }

    addExecutable(ConditionalExecutable(record, object: EventLoopExecutable{
        val states = mutableListOf<ControllerState>()

        override fun start() {
            states.clear()
        }

        override fun periodic() {
            states.add(replayController.record())
        }

        override fun stop() {
            val recordingObj = ControllerRecording(states = states)
            val recording = Json.encodeToString(ControllerRecording.serializer(), recordingObj)
            Files.writeString(mainPath(), recording)
        }
    }))

    addExecutable(ConditionalExecutable(!record and replay, object: EventLoopExecutable{
        var states: List<ControllerState> = mutableListOf()
        var stateIndex = 0

        override fun start() {
            try {
                val recordingString = Files.readString(mainPath())
                val recording = Json.decodeFromString(ControllerRecording.serializer(), recordingString)
                states = recording.states
            }catch (e: IOException){
                states = mutableListOf()
                stateIndex = 0
                System.err.println("Tried to replay but $mainPath does not exist")
            }
        }

        override fun periodic() {
            if(stateIndex < states.size){
                val state = states[stateIndex++]
                replayController.replay(state)
            } else replayController.replay(ZERO_STATE)

        }

        override fun stop() {
            replayController.passThrough()
        }

    }))

    return replayController
}



