package org.grav.y2024.subsystems

import com.gattagdev.misc.BrushlessCANSparkMax
import com.gattagdev.newcommands.command
import com.gattagdev.units.inches
import com.revrobotics.CANSparkBase.IdleMode.kBrake
import edu.wpi.first.wpilibj2.command.SubsystemBase

object TriggerSubsystem : SubsystemBase() {

    private val motor = BrushlessCANSparkMax(22)

    private val triggerWheelDiameter = 2.0.inches

    init {
        motor.restoreFactoryDefaults()
        motor.idleMode = kBrake
        motor.setSmartCurrentLimit(20)
        motor.enableVoltageCompensation(12.0)

        defaultCommand = stopCommand()
    }

    fun stopCommand() = command(TriggerSubsystem){
        periodic { motor.set(0.0) }
    }

    fun shootCommand() = command(TriggerSubsystem){
        periodic { motor.set(1.0) }
        deadline { 2.0 }
    }

    fun reverseCommand() = command(TriggerSubsystem){
        periodic { motor.set(-1.0) }
    }

}