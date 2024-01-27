package org.grav.y2024

import com.gattagdev.defered.axis
import com.gattagdev.defered.deadBand
import com.gattagdev.defered.times
import com.gattagdev.defered.unaryMinus
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj2.command.Command
import org.grav.y2024.commands.NormalDriveCommand

object RobotContainer {

    init {
        configureBindings()
    }

    private fun configureBindings() {

        val driverJoystick = Joystick(0)

    }

    fun getAutonomousCommand(): Command? {
        // TODO: Implement properly
        return null
    }
}