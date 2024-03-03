package org.grav.y2024.subsystems

import com.gattagdev.misc.BrushlessCANSparkMax
import com.gattagdev.newcommands.*
import com.gattagdev.pid.linearFF
import com.gattagdev.pid.setPID
import com.gattagdev.units.feet
import com.gattagdev.units.inches
import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkBase.ControlType.kVelocity
import com.revrobotics.CANSparkMax

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.grav.y2024.Constants
import kotlin.math.absoluteValue

object FlywheelSubsystem : SubsystemBase() {

    private val upperMotor = BrushlessCANSparkMax(20)
    private val lowerMotor = BrushlessCANSparkMax(21)

    private var atSetpoint = false
    val isAtSetpoint: Boolean get() = atSetpoint

    private fun basicMotorConfig (motor: CANSparkMax){
        motor.restoreFactoryDefaults()
        motor.idleMode = CANSparkBase.IdleMode.kBrake
        motor.setSmartCurrentLimit(Constants.neoCurrentLimit)
        motor.enableVoltageCompensation(Constants.neoVoltageCompensation)
        motor.setPID{
            p(0.00001)
            i(0.0)
            d(0.0)
            linearFF(0.00017519999528303742, 0.0)
        }
    }
    init {
        basicMotorConfig(upperMotor)
        basicMotorConfig(lowerMotor)

        defaultCommand = stopCommand()
    }

    fun stopCommand() = command(FlywheelSubsystem){
        periodic {
            upperMotor.set(0.0)
            lowerMotor.set(0.0)
            atSetpoint = false
        }
    }


    /**
     * @param rateStream a Double Supplier in units of meters per second
     */
    fun runCommand(rateStream: () -> Double) = command(FlywheelSubsystem){
        periodic {
            val rate = rateStream()
            // setReference requires units of rotations per minute
            upperMotor.pidController.setReference(rate * Constants.shooterFactor, kVelocity)
            lowerMotor.pidController.setReference(rate * Constants.shooterFactor, kVelocity)

            val upperMotorAtSpeed = (upperMotor.encoder.velocity/Constants.shooterFactor - rate).absoluteValue < 0.25.feet
            val lowerMotorAtSpeed = (upperMotor.encoder.velocity/Constants.shooterFactor - rate).absoluteValue < 0.25.feet
            atSetpoint = upperMotorAtSpeed && lowerMotorAtSpeed
        }
    }

    fun ampCommand() = runCommand { Constants.shooterWheelAmpSpeed }
    fun speakerCommand() = runCommand { Constants.shooterWheelSpeakerSpeed }
    fun waitForSetpointCommand() = command {
        finish { isAtSetpoint }
    }

    fun reverseCommand() = command(FlywheelSubsystem){
        periodic {
            upperMotor.set(Constants.motorsReverseSpeed)
            lowerMotor.set(Constants.motorsReverseSpeed)
        }
    }

}