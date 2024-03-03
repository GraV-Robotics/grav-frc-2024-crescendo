package org.grav.y2024.subsystems

import com.gattagdev.defered.DS
import com.gattagdev.misc.BrushlessCANSparkMax
import com.gattagdev.newcommands.*
import com.gattagdev.pid.linearFF
import com.gattagdev.pid.setPID
import com.gattagdev.units.*
import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkBase.ControlType.kVelocity
import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.grav.y2024.Constants
import kotlin.math.PI
import kotlin.math.absoluteValue

object FlywheelSubsystem : SubsystemBase() {

    private val upperMotor = BrushlessCANSparkMax(20)
    private val lowerMotor = BrushlessCANSparkMax(21)

    private val lowerAmpDefault = 275.0
    private val upperAmpDefault = 1650.0

    private val mainFFDefault = 0.002050
    private val secondFFDefault = 0.00004

    private val lowerAmpSpeed = { SmartDashboard.getNumber("lowerSpeed", lowerAmpDefault) }
    private val upperAmpSpeed = { SmartDashboard.getNumber("upperSpeed", upperAmpDefault) }

    private val mainFF = { SmartDashboard.getNumber("mainFF", mainFFDefault) }
    private val secondFF = { SmartDashboard.getNumber("secondFF", secondFFDefault) }

    private var atSetpoint = false
    val isAtSetpoint: Boolean get() = atSetpoint

    val shooterWheelAmpSpeed = 2.0.feet
    val shooterWheelSpeakerSpeed = 30.0.feet
    val shooterWheelDiameter = 4.0.inches


    private fun basicMotorConfig (motor: CANSparkMax){
        motor.restoreFactoryDefaults()
        motor.idleMode = CANSparkBase.IdleMode.kBrake
        motor.setSmartCurrentLimit(40)
        motor.enableVoltageCompensation(12.0)
        motor.setPID{
            p(0.0001)
            i(0.0)
            d(0.0)
//            linearFF(0.00017525, 0.0)
//            linearFF(0.00019, 0.0)
        }
    }
    init {
        basicMotorConfig(upperMotor)
        basicMotorConfig(lowerMotor)

        SmartDashboard.putNumber("lowerSpeed", lowerAmpDefault)
        SmartDashboard.putNumber("upperSpeed", upperAmpDefault)
        SmartDashboard.putNumber("mainFF", mainFFDefault)
        SmartDashboard.putNumber("secondFF", secondFFDefault)

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
    fun runCommand(rateStream: () -> Double) = runCommand(rateStream, rateStream)

    fun runCommand(lowerStream: DS, upperStream: DS) = command(FlywheelSubsystem){
        periodic {

            val upper = upperStream()
            val lower = lowerStream()

            val compute = {rate: Double -> (mainFF() * rate  + (secondFF() * (3500 - rate))) }
            upperMotor.pidController.setReference(upper, kVelocity, 0, compute(upper))
            lowerMotor.pidController.setReference(lower, kVelocity, 0, compute(lower))


            val upperMotorAtSpeed = (upperMotor.encoder.velocity - upper).absoluteValue < (upper * 0.05)
            val lowerMotorAtSpeed = (lowerMotor.encoder.velocity - lower).absoluteValue < (lower * 0.05)
            atSetpoint = upperMotorAtSpeed && lowerMotorAtSpeed
            println("${lowerMotor.encoder.velocity} - ${upperMotor.encoder.velocity}")
        }
    }

    fun ampCommand() = runCommand( lowerAmpSpeed, upperAmpSpeed)
    fun speakerCommand() = runCommand { 3250.0 }
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