package org.grav.y2024

import com.gattagdev.misc.reportKotlinUsage
import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler


object Robot : TimedRobot() {

    override fun robotInit() {
        reportKotlinUsage()
        RobotContainer
    }


    override fun robotPeriodic() {
        CommandScheduler.getInstance().run()

    }
}