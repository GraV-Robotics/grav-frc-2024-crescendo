package org.grav.y2024

import com.gattagdev.defered.*
import com.gattagdev.joystick.BetterXboxController
import com.gattagdev.joystick.replayable
import com.gattagdev.misc.AUTONOMOUS
import com.gattagdev.misc.always
import com.gattagdev.newcommands.*
import com.gattagdev.units.degrees
import com.gattagdev.units.inches
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj.DriverStation
import org.grav.y2024.subsystems.ClimberSubsystem
import org.grav.y2024.subsystems.DriveSubsystem
import org.grav.y2024.subsystems.DriveSubsystem.driveCommand
import org.grav.y2024.subsystems.DriveSubsystem.zeroGyroCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.ampCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.speakerCommand
import org.grav.y2024.subsystems.FlywheelSubsystem.waitForSetpointCommand
import org.grav.y2024.subsystems.IntakeSubsystem
import org.grav.y2024.subsystems.TriggerSubsystem
import org.grav.y2024.subsystems.TriggerSubsystem.shootCommand
import org.photonvision.PhotonCamera
import kotlin.io.path.Path


object RobotContainer {

    init { eventLoopContext {


        val driver: BetterXboxController
        val manipulator: BetterXboxController
        val TELEOPERATED: BS

        replayManager {
            path = { Path("recording.json") }
            val trueTeleop = DriverStation::isTeleopEnabled

            driver = BetterXboxController(0).replayable("driver")
            manipulator = BetterXboxController(1).also {
                record = trueTeleop and it.startButton
                replay = AUTONOMOUS
            }.replayable("manipulator")
            TELEOPERATED = trueTeleop.replayable("teleop")
        }

        val intakeInput      = manipulator.leftTrigger gt 0.5
        val reverseInput     = manipulator.xButton
        val speakerSpinInput = manipulator.aButton
        val ampSpinInput     = manipulator.yButton
        val shootInput       = manipulator.rightTrigger gt 0.5

        val extendInput      = manipulator.leftBumper
        val retractInput     = manipulator.rightBumper

        val climbingPos = 12.0.inches
        val retractedPos = 0.0.inches

        val driveSpeed = (manipulator.rightTrigger gt 0.5).toDouble(1.0, 0.5) times DriveSubsystem.robotMaxSpeed
        val rotationRate = { DriveSubsystem.robotMaxRotationRate }
        val robotCentric = driver.leftBumper
        val zeroGyroInput = driver.startButton



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


        var alternateRotationInput: Double? = null
        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {

            zeroGyroCommand() whileTrue zeroGyroInput


            driveCommand (fieldRelative = !robotCentric) {
                ChassisSpeeds(
                    forwardInput(),
                    sideInput(),
                    rotationInput()
                )
            } whileTrue always

            switch {
                case(reverseInput) {
                    IntakeSubsystem.reverseCommand() * TriggerSubsystem.reverseCommand() whileTrue always
                }
                case(always) {
                    IntakeSubsystem.intakeCommand() whileTrue intakeInput
                    waitForSetpointCommand() + shootCommand() whileTrue shootInput
                    switch {
                        case(speakerSpinInput) {
                            speakerCommand() whileTrue always
                        }
                        case(ampSpinInput) {
                            ampCommand() whileTrue always
                        }
                    }
                }
            }

            switch {
                case(extendInput) {
                    ClimberSubsystem.moveToTarget { climbingPos } whileTrue always
                }
                case(retractInput) {
                    ClimberSubsystem.moveToTarget { retractedPos } whileTrue always
                }
            }
        }

        /* -------------------- AUTO -------------------- */
        AUTONOMOUS {
        }


    } }

}
