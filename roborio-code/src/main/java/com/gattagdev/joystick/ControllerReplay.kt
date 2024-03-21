package com.gattagdev.joystick

import com.gattagdev.defered.BS
import com.gattagdev.defered.DS
import com.gattagdev.newcommands.ReplayBuilder
import com.gattagdev.newcommands.ReplayComponent
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
    override val povDegrees: () -> Double? get() = { input().povDegrees() }
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
    override val povDegrees: () -> Double? get() = { state().povDegrees }
}

enum class ButtonKey {
    A_BUTTON,
    B_BUTTON,
    X_BUTTON,
    Y_BUTTON,
    LEFT_BUMPER,
    RIGHT_BUMPER,
    BACK_BUTTON,
    START_BUTTON,
    LEFT_STICK_BUTTON,
    RIGHT_STICK_BUTTON;

    operator fun invoke(state: Boolean): Int = if(state) 1 shl ordinal else 0
    operator fun invoke(state: Int): Boolean = state and (1 shl ordinal) != 0

}

internal fun compressAxis(axis: Double): Short = (axis * Short.MAX_VALUE).toInt().toShort()
internal fun decompressAxis(axis: Short): Double = axis.toDouble() / Short.MAX_VALUE
internal fun compressPOV(pov: Double?): Short = pov?.toInt()?.toShort() ?: -1
internal fun decompressPOV(pov: Short): Double? = if(pov.toInt() == -1) null else pov.toDouble()

// TODO Could really use some range validation here
@Serializable
data class ControllerState(
    private val _buttons: Int = 0,
    private val _leftTrigger: Short = 0,
    private val _rightTrigger: Short = 0,
    private val _leftStickX: Short = 0,
    private val _leftStickY: Short = 0,
    private val _rightStickX: Short = 0,
    private val _rightStickY: Short = 0,
    private val _povDegrees: Short = -1
) {
    constructor(
        xButton: Boolean = false,
        aButton: Boolean = false,
        bButton: Boolean = false,
        yButton: Boolean = false,
        leftBumper: Boolean = false,
        rightBumper: Boolean = false,
        backButton: Boolean = false,
        startButton: Boolean = false,
        leftStickButton: Boolean = false,
        rightStickButton: Boolean = false,
        leftTrigger: Double = 0.0,
        rightTrigger: Double = 0.0,
        leftStickX: Double = 0.0,
        leftStickY: Double = 0.0,
        rightStickX: Double = 0.0,
        rightStickY: Double = 0.0,
        povDegrees: Double? = null
    ) : this(
        _buttons = ButtonKey.A_BUTTON(aButton) +
                ButtonKey.B_BUTTON(bButton) +
                ButtonKey.X_BUTTON(xButton) +
                ButtonKey.Y_BUTTON(yButton) +
                ButtonKey.LEFT_BUMPER(leftBumper) +
                ButtonKey.RIGHT_BUMPER(rightBumper) +
                ButtonKey.BACK_BUTTON(backButton) +
                ButtonKey.START_BUTTON(startButton) +
                ButtonKey.LEFT_STICK_BUTTON(leftStickButton) +
                ButtonKey.RIGHT_STICK_BUTTON(rightStickButton),
        _leftTrigger = compressAxis(leftTrigger),
        _rightTrigger = compressAxis(rightTrigger),
        _leftStickX = compressAxis(leftStickX),
        _leftStickY = compressAxis(leftStickY),
        _rightStickX = compressAxis(rightStickX),
        _rightStickY = compressAxis(rightStickY),
        _povDegrees = compressPOV(povDegrees)
    )
    val aButton get() = ButtonKey.A_BUTTON(_buttons)
    val bButton get() = ButtonKey.B_BUTTON(_buttons)
    val xButton get() = ButtonKey.X_BUTTON(_buttons)
    val yButton get() = ButtonKey.Y_BUTTON(_buttons)
    val leftBumper get() = ButtonKey.LEFT_BUMPER(_buttons)
    val rightBumper get() = ButtonKey.RIGHT_BUMPER(_buttons)
    val backButton get() = ButtonKey.BACK_BUTTON(_buttons)
    val startButton get() = ButtonKey.START_BUTTON(_buttons)
    val leftStickButton get() = ButtonKey.LEFT_STICK_BUTTON(_buttons)
    val rightStickButton get() = ButtonKey.RIGHT_STICK_BUTTON(_buttons)
    val leftTrigger get() = decompressAxis(_leftTrigger)
    val rightTrigger get() = decompressAxis(_rightTrigger)
    val leftStickX get() = decompressAxis(_leftStickX)
    val leftStickY get() = decompressAxis(_leftStickY)
    val rightStickX get() = decompressAxis(_rightStickX)
    val rightStickY get() = decompressAxis(_rightStickY)
    val povDegrees get() = decompressPOV(_povDegrees)

}

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