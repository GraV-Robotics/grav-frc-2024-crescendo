package org.grav.y2024.subsystems

import com.gattagdev.misc.BrushlessCANSparkMax
import com.gattagdev.newcommands.command
import com.gattagdev.units.inches
import com.revrobotics.CANSparkBase.IdleMode.kBrake
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.grav.y2024.Constants

object TriggerSubsystem : SubsystemBase() {

    private val motor = BrushlessCANSparkMax(22)

    init {
        motor.restoreFactoryDefaults()
        motor.idleMode = kBrake
        motor.setSmartCurrentLimit(Constants.neo550CurrentLimit)
        motor.enableVoltageCompensation(Constants.neo550VoltageCompensation)

        defaultCommand = stopCommand()
    }

    fun stopCommand() = command(TriggerSubsystem){
        periodic { motor.set(0.0) }
    }

    fun shootCommand() = command(TriggerSubsystem){
        periodic { motor.set(Constants.triggerSpeed) }
        deadline { Constants.waitTime }
    }

    fun reverseCommand() = command(TriggerSubsystem){
        periodic { motor.set(Constants.motorsReverseSpeed) }
    }

}