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
import kotlin.math.absoluteValue

object FlywheelSubsystem : SubsystemBase() {

    private val upperMotor = BrushlessCANSparkMax(20)
    private val lowerMotor = BrushlessCANSparkMax(21)

    private val wheelDiameter = 4.inches

    private var atSetpoint = false
    val isAtSetpoint: Boolean get() = atSetpoint

    private fun basicMotorConfig (motor: CANSparkMax){
        motor.restoreFactoryDefaults()
        motor.idleMode = CANSparkBase.IdleMode.kBrake
        motor.setSmartCurrentLimit(40)
        motor.enableVoltageCompensation(12.0)
        motor.setPID{
            p(0.0)
            i(0.0)
            d(0.0)
            linearFF(0.0, 0.0)
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
            val factor = 60.0/((wheelDiameter/2)*Math.PI*2)
            val rate = rateStream()
            // setReference requires units of rotations per minute
            upperMotor.pidController.setReference(rate * factor, kVelocity)
            lowerMotor.pidController.setReference(rate * factor, kVelocity)

            val upperMotorAtSpeed = (upperMotor.encoder.velocity/factor - rate).absoluteValue < 0.25.feet
            val lowerMotorAtSpeed = (upperMotor.encoder.velocity/factor - rate).absoluteValue < 0.25.feet
            atSetpoint = upperMotorAtSpeed && lowerMotorAtSpeed
        }
    }

    fun ampCommand() = runCommand { 2.feet }
    fun speakerCommand() = runCommand { 15.feet }
    fun waitForSetpointCommand() = command {
        finish { isAtSetpoint }
    }

    fun reverseCommand() = command(FlywheelSubsystem){
        periodic {
            upperMotor.set(-0.5)
            lowerMotor.set(-0.5)
        }
    }

}