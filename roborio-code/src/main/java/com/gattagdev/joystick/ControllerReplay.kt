package com.gattagdev.joystick

import com.gattagdev.defered.BS
import com.gattagdev.defered.DS
import com.gattagdev.newcommands.*
import kotlinx.serialization.Serializable

class ProxyController(
    private val input: () -> BetterXboxController,
    private val feedback: () -> BetterXboxController
): BetterXboxController{
    override val aButton: BS get() = { input().aButton() }
    override val bButton: BS get() = { input().bButton() }
    override val xButton: BS get() = { input().xButton() }
    override val yButton: BS get() = { input().yButton() }
    override val leftBumper: BS get() = { input().leftBumper() }
    override val rightBumper: BS get() = { input().rightBumper() }
    override val backButton: BS get() = { input().backButton() }
    override val startButton: BS get() = { input().startButton() }
    override val leftStickButton: BS get() = { input().leftStickButton() }
    override val rightStickButton: BS get() = { input().rightStickButton() }
    override val leftTrigger: DS get() = { input().leftTrigger() }
    override val rightTrigger: DS get() = { input().rightTrigger() }
    override val leftStickX: DS get() = { input().leftStickX() }
    override val leftStickY: DS get() = { input().leftStickY() }
    override val rightStickX: DS get() = { input().rightStickX() }
    override val rightStickY: DS get() = { input().rightStickY() }
    override val povDegrees: DS get() = { input().povDegrees() }
}

class StateReplayController(private val state: () -> ControllerState): BetterXboxController{
    override val aButton: BS get() = { state().aButton }
    override val bButton: BS get() = { state().bButton }
    override val xButton: BS get() = { state().xButton }
    override val yButton: BS get() = { state().yButton }
    override val leftBumper: BS get() = { state().leftBumper }
    override val rightBumper: BS get() = { state().rightBumper }
    override val backButton: BS get() = { state().backButton }
    override val startButton: BS get() = { state().startButton }
    override val leftStickButton: BS get() = { state().leftStickButton }
    override val rightStickButton: BS get() = { state().rightStickButton }
    override val leftTrigger: DS get() = { state().leftTrigger }
    override val rightTrigger: DS get() = { state().rightTrigger }
    override val leftStickX: DS get() = { state().leftStickX }
    override val leftStickY: DS get() = { state().leftStickY }
    override val rightStickX: DS get() = { state().rightStickX }
    override val rightStickY: DS get() = { state().rightStickY }
    override val povDegrees: DS get() = { state().povDegrees }
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

val BetterXboxController.currentState get() = ControllerState(
    xButton = this.xButton(),
    aButton = this.aButton(),
    bButton = this.bButton(),
    yButton = this.yButton(),
    leftBumper = this.leftBumper(),
    rightBumper = this.rightBumper(),
    backButton = this.backButton(),
    startButton = this.startButton(),
    leftStickButton = this.leftStickButton(),
    rightStickButton = this.rightStickButton(),
    leftTrigger = this.leftTrigger(),
    rightTrigger = this.rightTrigger(),
    leftStickX = this.leftStickX(),
    leftStickY = this.leftStickY(),
    rightStickX = this.rightStickX(),
    rightStickY = this.rightStickY(),
    povDegrees = this.povDegrees()
)

val ZERO_STATE = ControllerState()

context(ReplayBuilder)
fun BetterXboxController.replayable(key: String): BetterXboxController {
    var currentState = ZERO_STATE

    val realController = this
    val replayController = StateReplayController { currentState }
    var currentController = realController

    addReplayComponent(object: ReplayComponent<ControllerState>() {
        override val name: String = key
        override var serializer = ControllerState.serializer()
        override fun periodic(state: ControllerState?) {
            if(state != null){
                currentState = state
                currentController = replayController
            }else{
                currentController = realController
            }
        }
        override fun record(): ControllerState = realController.currentState
    })

    return ProxyController({ currentController }, { realController })
}