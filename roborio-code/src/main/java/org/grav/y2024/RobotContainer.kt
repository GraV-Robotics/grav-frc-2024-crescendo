package org.grav.y2024

import com.gattagdev.defered.*
import com.gattagdev.joystick.BetterXboxController
import com.gattagdev.misc.AUTONOMOUS
import com.gattagdev.misc.TELEOPERATED
import com.gattagdev.misc.always
import com.gattagdev.newcommands.*
import com.gattagdev.units.degrees
import com.gattagdev.units.rotations
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.trajectory.TrapezoidProfile
import edu.wpi.first.wpilibj.Timer
import org.grav.y2024.subsystems.ClimberSubsystem
import org.grav.y2024.subsystems.DriveSubsystem
import org.grav.y2024.subsystems.DriveSubsystem.driveCommand
import org.grav.y2024.subsystems.DriveSubsystem.sim
import org.grav.y2024.subsystems.DriveSubsystem.zeroGyroCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.ampCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.speakerCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.waitForSetpointCommand
import org.grav.y2024.subsystems.IntakeSubsystem
import org.grav.y2024.subsystems.TriggerSubsystem
import org.grav.y2024.subsystems.TriggerSubsystem.shootCommand


object RobotContainer {

    init { eventLoopContext {


        val driver = BetterXboxController(0)
        val manipulator = BetterXboxController(1)

        val intakeInput = manipulator.leftTrigger gt 0.5
        val reverseInput = manipulator.xButton
        val shootInput = manipulator.rightTrigger gt 0.5
        val speakerSpin = manipulator.aButton
        val ampSpin = manipulator.yButton

        val extendInput = manipulator.leftBumper
        val retractInput = manipulator.rightBumper

        val driveSpeed = (manipulator.rightTrigger gt 0.5).toDouble(1.0, 0.5) times Constants.robotMaxSpeed
        val rotationRate = { Constants.robotMaxRotationRate }
        val robotCentric = driver.leftBumper
        val zeroGyro = driver.startButton

        val driverAngle: () -> Double? = {
            if (driver.aButton()) 0.0.degrees
            else if (driver.bButton()) 90.0.degrees
            else if (driver.yButton()) 180.0.degrees
            else if (driver.xButton()) 270.0.degrees
            else null
        }

        val drivePow = { 2.0 }

        val forwardInput = driver.leftStickY deadBand 0.05 signPow drivePow times driveSpeed
        val sideInput = driver.leftStickX deadBand 0.05 signPow drivePow times driveSpeed
        val rotationInput = driver.rightStickX deadBand 0.05 signPow drivePow times -rotationRate

        sim()//can u try simulating to see if it works? //I think it literally just need this im not sure tho

        val alternateRotationInput: Flag<Double?> = flag(null)
        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {
            //when drive angle is set

            zeroGyroCommand() whileTrue zeroGyro


            command {
                val angle = driverAngle()!!
                val goal = TrapezoidProfile.State(angle, 0.0)
                val profile = TrapezoidProfile(TrapezoidProfile.Constraints(Constants.robotMaxRotationRate, 5.0.rotations))
                var prevState = TrapezoidProfile.State(DriveSubsystem.measuredAngle, DriveSubsystem.measuredAngularRate)
                periodic {
                    prevState = profile.calculate(elapsedTime, prevState, goal)
                    alternateRotationInput set prevState.velocity
                }
            } whileTrue {driverAngle() != null}


            driveCommand (fieldRelative = !robotCentric) {
                val alternateRotation = alternateRotationInput()
                ChassisSpeeds(
                    forwardInput(),
                    sideInput(),
                    alternateRotation ?: rotationInput()
                )
            } whileTrue always

            IntakeSubsystem.intakeCommand() whileTrue intakeInput
            IntakeSubsystem.reverseCommand() * TriggerSubsystem.reverseCommand() whileTrue reverseInput
            waitForSetpointCommand() + shootCommand() whileTrue shootInput

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
