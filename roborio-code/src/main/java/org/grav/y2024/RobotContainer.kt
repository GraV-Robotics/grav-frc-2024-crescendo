package org.grav.y2024

import com.gattagdev.defered.*
import com.gattagdev.joystick.BetterXboxController
import com.gattagdev.joystick.replayable
import com.gattagdev.misc.AUTONOMOUS
import com.gattagdev.misc.always
import com.gattagdev.newcommands.*
import com.gattagdev.nt.chooser
import com.gattagdev.nt.quickDashboard
import com.gattagdev.units.degrees
import com.gattagdev.units.inches
import com.gattagdev.units.rotations
import com.gattagdev.units.toDegrees
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.trajectory.TrapezoidProfile
import edu.wpi.first.wpilibj.DriverStation
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

    val `Recording-Access-Enabled` by quickDashboard(false, persistent = false)

    init { eventLoopContext {

        var recordingPath = "center-2-note"
        var replayAllowed = true

        chooser("Auto-Mode"){
            default("Do Nothing"){
                recordingPath = "do-nothing"
                replayAllowed = false
            }
            choice("Movement"){
                recordingPath = "movement"
                replayAllowed = true
            }
            choice("Center 1-Note"){
                recordingPath = "center-1-note"
                replayAllowed = true
            }
            choice("Center 2-Note"){
                recordingPath = "center-2-note"
                replayAllowed = true
            }
            choice("Center 3-Note"){
                recordingPath = "center-3-note"
                replayAllowed = true
            }
            choice("Center 4-Note"){
                recordingPath = "center-4-note"
                replayAllowed = true
            }
        }

        val driver: BetterXboxController
        val manipulator: BetterXboxController
        val TELEOPERATED: BS

        replayManager {
            path = { Path(recordingPath) }
            replay = AUTONOMOUS and { replayAllowed }
            val trueTeleop = DriverStation::isTeleopEnabled

            replayReady = { false }
            driver = BetterXboxController(0).also {
                record = trueTeleop and (it.leftBumper) and ::`Recording-Access-Enabled` and !DriverStation::isFMSAttached
            }.replayable("driver")
            manipulator = BetterXboxController(1).replayable("manipulator")
            TELEOPERATED = trueTeleop.replayable("teleop")
        }

        val intakeInput      = manipulator.leftTrigger gt 0.5
        val reverseInput     = manipulator.xButton
        val speakerSpinInput = manipulator.aButton
        val ampSpinInput     = manipulator.yButton
        val shootInput       = manipulator.rightTrigger gt 0.5

        val climbingPos      = 12.0.inches
        val retractedPos     = 0.0.inches
        val extendInput      = manipulator.leftBumper
        val retractInput     = manipulator.rightBumper

        val driveSpeed       = { DriveSubsystem.trueRobotMaxSpeed }
        val rotationRate     = { DriveSubsystem.robotMaxRotationRate }
        val deadband         = 0.05

        val forwardInput     = driver.leftStickY  deadBand deadband times driveSpeed
        val sideInput        = driver.leftStickX  deadBand deadband times -driveSpeed
        val rotationInput    = driver.rightStickX deadBand deadband signPow { 2.0 } times -rotationRate
        val robotCentric     = driver.leftTrigger gt 0.5
        val zeroGyroInput    = driver.startButton
        val driverAngle      = { driver.povDegrees()?.let { 180.degrees - it.degrees } }
        val trackNoteInput   = driver.rightTrigger gt 0.25

        val continuousGyro = {DriveSubsystem.measuredAngle}.makeContinuous(-180.degrees, 180.degrees)
        var alternateRotationInput: Double? = null
        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {

            zeroGyroCommand() whileTrue zeroGyroInput

            val frontCamera = PhotonCamera("front-camera")


            val lowerCameraCutoff by quickDashboard(-11.degrees, name="ring-track/cutoff"){ degrees }
            val ringTrackKP by quickDashboard(10.0, name="ring-track/kP"){ 10.0 }
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
                        if(last.targets[0].pitch.degrees < lowerCameraCutoff) unlocked = false
                    }
                    val setpoint = lastRobotAngle + lastCameraAngle

                    val measured = continuousGyro()
                    val error = setpoint - measured

                    alternateRotationInput = error * ringTrackKP
                }

                onEnd { alternateRotationInput = null }
            } whileTrue trackNoteInput


            command{

            }

            driveCommand (fieldRelative = !robotCentric) {
                ChassisSpeeds(forwardInput(), sideInput(), alternateRotationInput ?: rotationInput())
            } whileTrue always

            switch {
                case(reverseInput) {
                    IntakeSubsystem.reverseCommand() * TriggerSubsystem.reverseCommand() whileTrue always
                }
                case(always) {
                    IntakeSubsystem.intakeCommand() whileTrue intakeInput
                    waitForSetpointCommand() + shootCommand() whileTrue shootInput
                    switch {
                        case(speakerSpinInput) { speakerCommand() whileTrue always }
                        case(ampSpinInput)     { ampCommand() whileTrue always }
                    }
                }
            }
        }

        /* -------------------- AUTO -------------------- */
        AUTONOMOUS {
        }


    } }

}
