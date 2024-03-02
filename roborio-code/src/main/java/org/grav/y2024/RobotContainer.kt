package org.grav.y2024

import com.gattagdev.defered.*
import com.gattagdev.joystick.BetterXboxController
import com.gattagdev.misc.*
import com.gattagdev.newcommands.*
import com.gattagdev.units.inches
import edu.wpi.first.math.kinematics.ChassisSpeeds
import org.grav.y2024.subsystems.ClimberSubsystem
import org.grav.y2024.subsystems.DriveSubsystem
import org.grav.y2024.subsystems.DriveSubsystem.driveCommand
import org.grav.y2024.subsystems.DriveSubsystem.sim
import org.grav.y2024.subsystems.DriveSubsystem.swerveDrive
import org.grav.y2024.subsystems.FlywheelSubsystem.ampCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.speakerCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.waitForSetpointCommand
import org.grav.y2024.subsystems.IntakeSubsystem
import org.grav.y2024.subsystems.TriggerSubsystem.shootCommand

object RobotContainer {

    init { eventLoopContext {


        val driver = BetterXboxController(Constants.driverControllerID)
        val manipulator = BetterXboxController(Constants.manipulatorControllerID)

        val intakeInput = manipulator.leftTrigger gt 0.5
        val shootInput = manipulator.rightTrigger gt 0.5
        val speakerSpin = manipulator.aButton
        val ampSpin = manipulator.yButton

        val extendInput = manipulator.leftBumper
        val retractInput = manipulator.rightBumper

        val driveSpeed = (manipulator.rightTrigger gt 0.5).toDouble(1.0, 0.5) times Constants.robotMaxSpeed
        val rotationRate = { Constants.robotMaxRotationRate }

        val drivePow = { 2.0 }

        val forwardInput = driver.leftStickY deadBand 0.05 signPow drivePow times driveSpeed
        val sideInput = driver.leftStickX deadBand 0.05 signPow drivePow times -driveSpeed
        val rotationInput = driver.rightStickX deadBand 0.05 signPow drivePow times -rotationRate


        sim()//can u try simulating to see if it works? //I think it literally just need this im not sure tho

        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {
            driveCommand { ChassisSpeeds(forwardInput(), sideInput(), rotationInput()) } whileTrue always


            intakeInput {
                IntakeSubsystem.intakeCommand() whileTrue always
            }


            shootInput {
                waitForSetpointCommand() + shootCommand() whileTrue always
            }

            switch {
                case(speakerSpin) {
                    speakerCommand() whileTrue always
                }
                case(ampSpin) {
                    ampCommand() whileTrue always
                }
            }
            switch {
                case(extendInput) {
                    ClimberSubsystem.moveToTarget { Constants.climbingPos } whileTrue always
                }
                case(retractInput) {
                    ClimberSubsystem.moveToTarget { Constants.retractedPos } whileTrue always
                }
            }
        }

        /* -------------------- AUTO -------------------- */
        AUTONOMOUS {
        }

    } }
}
