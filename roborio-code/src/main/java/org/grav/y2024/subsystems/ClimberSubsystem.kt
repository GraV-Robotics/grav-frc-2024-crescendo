package org.grav.y2024.subsystems

import com.gattagdev.misc.BrushlessCANSparkMax
import com.gattagdev.newcommands.command
import com.gattagdev.pid.linearFF
import com.gattagdev.pid.setPID
import com.gattagdev.units.feet
import com.gattagdev.units.inches
import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkBase.ControlType.kPosition
import com.revrobotics.CANSparkMax
import edu.wpi.first.math.MathUtil
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.grav.y2024.Constants
import org.grav.y2024.RobotContainer
import kotlin.math.absoluteValue

object ClimberSubsystem : SubsystemBase() {
    private var climbingSpeed: Double = 0.0
    private val leftClimbingMotor = BrushlessCANSparkMax(25)
    private val rightClimbingMotor = BrushlessCANSparkMax(26)

    private var targetPosition = 0.0

    private var atSetpoint = false

    val climbMaxHeight = 14.0.inches
    val climbMinHeight = 0.0.inches

    private fun basicMotorConfig (motor: CANSparkMax){
        motor.restoreFactoryDefaults()
        motor.idleMode = CANSparkBase.IdleMode.kBrake
        motor.setSmartCurrentLimit(40)
        motor.enableVoltageCompensation(12.0)
        motor.pidController.setP(0.0)
        motor.pidController.setI(0.0)
        motor.pidController.setD(0.0)
        motor.pidController.setFF(0.0)
        motor.setPID {
            p(0.0)
            i(0.0)
            d(0.0)
            linearFF(0.0, 0.0)
        }
        motor.enableSoftLimit(CANSparkBase.SoftLimitDirection.kForward, true)
        motor.enableSoftLimit(CANSparkBase.SoftLimitDirection.kReverse, true)
        motor.setSoftLimit(CANSparkBase.SoftLimitDirection.kForward, climbMaxHeight.toFloat())
        motor.setSoftLimit(CANSparkBase.SoftLimitDirection.kReverse, climbMinHeight.toFloat())
    }

    init{
        basicMotorConfig(leftClimbingMotor)
        basicMotorConfig(rightClimbingMotor)

        defaultCommand = stopCommand()
    }

    override fun periodic() {
        leftClimbingMotor.pidController.setReference(targetPosition, kPosition)
        rightClimbingMotor.pidController.setReference(targetPosition, kPosition)

        val leftAtSetpoint = false
        val rightAtSetpoint = false
        atSetpoint = leftAtSetpoint && rightAtSetpoint
    }

    fun stopCommand() = command(ClimberSubsystem){}

    fun moveToTarget(heightStream: () -> Double) = command(ClimberSubsystem){
        periodic {
            val height = MathUtil.clamp(heightStream(), climbMinHeight, climbMaxHeight)
            targetPosition = height
        }
    }


}