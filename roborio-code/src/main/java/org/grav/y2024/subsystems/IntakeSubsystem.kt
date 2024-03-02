package org.grav.y2024.subsystems

import com.gattagdev.misc.BrushlessCANSparkMax
import com.gattagdev.newcommands.command
import com.gattagdev.pid.linearFF
import com.gattagdev.pid.setPID
import com.gattagdev.units.inches
import com.revrobotics.CANSparkBase
import com.revrobotics.CANSparkMax
import edu.wpi.first.wpilibj2.command.SubsystemBase

object IntakeSubsystem : SubsystemBase() {
    private val topRoller = BrushlessCANSparkMax(23)
    private val bottomRoller = BrushlessCANSparkMax(24)

    private val intakeWheelDiameter = 2.0.inches

    private fun basicMotorConfig (motor: CANSparkMax){
        motor.restoreFactoryDefaults()
        motor.idleMode = CANSparkBase.IdleMode.kBrake
        motor.setSmartCurrentLimit(20)
        motor.enableVoltageCompensation(12.0)
        motor.setPID{
            p(0.0)
            i(0.0)
            d(0.0)
            linearFF(0.0, 0.0)
        }
    }
    init {
        basicMotorConfig(topRoller)
        basicMotorConfig(bottomRoller)

        defaultCommand = stopCommand()
    }

    fun stopCommand() = command(IntakeSubsystem){
        periodic {
            topRoller.set(0.0)
            bottomRoller.set(0.0)
        }
    }

    fun intakeCommand() = command(IntakeSubsystem){
        periodic {
            topRoller.set(1.0)
            bottomRoller.set(1.0)
        }
    }

    fun reverseCommand() = command(IntakeSubsystem){
        topRoller.set(-1.0)
        bottomRoller.set(-1.0)
    }

}