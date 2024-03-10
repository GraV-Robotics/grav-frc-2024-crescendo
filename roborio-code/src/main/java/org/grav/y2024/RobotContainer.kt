package org.grav.y2024

import com.gattagdev.defered.*
import com.gattagdev.joystick.BetterXboxController
import com.gattagdev.joystick.replayable
import com.gattagdev.misc.AUTONOMOUS
import com.gattagdev.misc.always
import com.gattagdev.misc.makeContinuous
import com.gattagdev.newcommands.*
import com.gattagdev.units.degrees
import com.gattagdev.units.inches
import com.gattagdev.units.rotations
import com.gattagdev.units.toDegrees
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.trajectory.TrapezoidProfile
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


        val trueTeleop = DriverStation::isTeleopEnabled

        val driver: BetterXboxController
        val manipulator: BetterXboxController
        val TELEOPERATED: BS
        val recordInput: BS

        var recordingEnabled = false
        replayManager {

            path = { Path("2-note.json") }
            replay = AUTONOMOUS
            record = { recordingEnabled } and trueTeleop
            driver = BetterXboxController(0).also {
                recordInput = it.leftTrigger gt 0.5
            }.replayable("driver")
            manipulator = BetterXboxController(1).also {
            }.replayable("manipulator")
            TELEOPERATED = trueTeleop.replayable("teleop")
        }
        command{
            recordingEnabled = true
            finish { trueTeleop() }
            onEnd { recordingEnabled = false }
        } onTrue (recordInput and {!recordingEnabled })

        val intakeInput      = manipulator.leftTrigger gt 0.5
        val reverseInput     = manipulator.xButton
        val speakerSpinInput = manipulator.aButton
        val ampSpinInput     = manipulator.yButton
        val shootInput       = manipulator.rightTrigger gt 0.5

        val extendInput      = manipulator.leftBumper
        val retractInput     = manipulator.rightBumper

        val climbingPos = 12.0.inches
        val retractedPos = 0.0.inches

        val driveSpeed =  { DriveSubsystem.robotMaxSpeed }
        val rotationRate = { DriveSubsystem.robotMaxRotationRate }
        val robotCentric = driver.leftBumper
        val zeroGyroInput = driver.startButton



        val driverAngle: () -> Double? = {
//            if (driver.aButton()) 0.0.degrees
//            else if (driver.bButton()) 90.0.degrees
//            else if (driver.yButton()) 180.0.degrees
//            else if (driver.xButton()) 270.0.degrees
//            else null
            driver.povDegrees()?.let { 180.degrees - it.degrees }
        }

        val drivePow = { 1.0 }

        val deadband = 0.05

        val forwardInput = driver.leftStickY deadBand deadband signPow drivePow times driveSpeed
        val sideInput = driver.leftStickX deadBand deadband signPow drivePow times -driveSpeed
        val rotationInput = driver.rightStickX deadBand deadband signPow { 2.0 } times -rotationRate

        val trackNoteInput = driver.rightTrigger gt 0.25

        val continuousGyro = {DriveSubsystem.measuredAngle}.makeContinuous(-180.degrees, 180.degrees)

        var alternateRotationInput: Double? = null
        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {

            zeroGyroCommand() whileTrue zeroGyroInput

//            command{
//                val constraints = TrapezoidProfile.Constraints(2.0.rotations,2.0.rotations)
//                val getSetpoint = { driverAngle()!! }
//                var currentSetpoint = getSetpoint()
//                var profile = TrapezoidProfile(constraints)
//                var startTime = 0.0
//
//                periodic {
//                    val newSetpoint = getSetpoint()
//                    if(newSetpoint != currentSetpoint){
//                        profile = TrapezoidProfile(constraints)
//                        currentSetpoint = newSetpoint
//                        startTime = elapsedTime
//                    }
//
//                    val output = profile.calculate(
//                        (elapsedTime - startTime),
//                        TrapezoidProfile.State(DriveSubsystem.measuredAngle, DriveSubsystem.measuredAngularRate),
//                        TrapezoidProfile.State(currentSetpoint, 0.0)
//                    )
//                    val error = output.position - DriveSubsystem.measuredAngle
//                    alternateRotationInput = -(error * 4)
//                    println("${error.toDegrees}")
//                }
//                onEnd { alternateRotationInput = null }
//            } whileTrue {driverAngle() != null}

            val frontCamera = PhotonCamera("front-camera")

            command{
                val initialAngle = continuousGyro()
                var lastRobotAngle = initialAngle
                var lastTimestamp = frontCamera.latestResult.timestampSeconds
                var lastCameraAngle = 0.0
                var unlocked = true

                periodic {
                    val last = frontCamera.latestResult
                    if(last.timestampSeconds != lastTimestamp && last.targets!!.isNotEmpty() && unlocked){
                        lastTimestamp = last.timestampSeconds
                        lastRobotAngle = continuousGyro()
                        lastCameraAngle = -last.targets[0].yaw.degrees
                        if(last.targets[0].pitch.degrees < -11.degrees) unlocked = false
                    }
                    val setpoint = lastRobotAngle + lastCameraAngle

                    val measured = continuousGyro()
                    val error = setpoint - measured

                    alternateRotationInput = error * 10
                }

                onEnd { alternateRotationInput = null }
            } whileTrue trackNoteInput



            driveCommand (fieldRelative = !robotCentric) {
                ChassisSpeeds(
                    forwardInput(),
                    sideInput(),
                    alternateRotationInput ?: rotationInput()
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
