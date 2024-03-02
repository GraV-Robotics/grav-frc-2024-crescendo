package org.grav.y2024.commands

import com.gattagdev.newcommands.times
import org.grav.y2024.subsystems.FlywheelSubsystem
import org.grav.y2024.subsystems.IntakeSubsystem
import org.grav.y2024.subsystems.TriggerSubsystem

fun fullReverseCommand() = IntakeSubsystem.reverseCommand() *
        TriggerSubsystem.reverseCommand() *
        FlywheelSubsystem.reverseCommand()
