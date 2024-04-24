package org.grav.y2024

import com.gattagdev.defered.*
import com.gattagdev.joystick.BetterXboxController
import com.gattagdev.joystick.bothRumble
import com.gattagdev.joystick.replayable
import com.gattagdev.misc.AUTONOMOUS
import com.gattagdev.misc.always
import com.gattagdev.newcommands.*
import com.gattagdev.nt.chooser
import com.gattagdev.nt.quickDashboard
import com.gattagdev.units.degrees
import com.gattagdev.units.inches
import com.gattagdev.units.rotations
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.trajectory.TrapezoidProfile
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints
import edu.wpi.first.math.trajectory.TrapezoidProfile.State
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.grav.y2024.subsystems.DriveSubsystem
import org.grav.y2024.subsystems.DriveSubsystem.driveCommand
import org.grav.y2024.subsystems.DriveSubsystem.zeroGyroCommand
import org.grav.y2024.subsystems.FlywheelSubsystem
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

        chooser("Auto-Mode") {
            default("Do Nothing") {
                recordingPath = "do-nothing"
                replayAllowed = false
            }
            choice("Movement") {
                recordingPath = "movement"
                replayAllowed = true
            }
            choice("Center 1-Note") {
                recordingPath = "center-1-note"
                replayAllowed = true
            }
            choice("Center 2-Note") {
                recordingPath = "center-2-note"
                replayAllowed = true
            }
            choice("Center 3-Note") {
                recordingPath = "center-3-note"
                replayAllowed = true
            }
            choice("Center 4-Note") {
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
            driver = BetterXboxController(0).also { }.replayable("driver")
            manipulator = BetterXboxController(1).also{
                record = trueTeleop and {it.povDegrees() != null} and ::`Recording-Access-Enabled` and !DriverStation::isFMSAttached
            }.replayable("manipulator")
            TELEOPERATED = trueTeleop.replayable("teleop")
        }

        val intakeInput      = manipulator.leftBumper
        val reverseInput     = manipulator.xButton
        val speakerSpinInput = driver.aButton
        val ampSpinInput     = driver.yButton
        val shootInput       = driver.rightBumper
        val topIntakeInput    = driver.leftBumper

        val climbingPos      = 12.0.inches
        val retractedPos     = 0.0.inches
        val extendInput      = manipulator.leftBumper
        val retractInput     = manipulator.rightBumper

        val driveSpeed       = { 1.5 }
        val rotationRate     = { 0.6 }
        val deadband         = 0.05

        val forwardInput     = driver.leftStickY  deadBand deadband times driveSpeed
        val sideInput        = driver.leftStickX  deadBand deadband times -driveSpeed
        val rotationInput    = driver.rightStickX deadBand deadband signPow { 2.0 } times -rotationRate
        val robotCentric     = driver.leftTrigger gt 0.5
        val zeroGyroInput    = driver.startButton
        val driverAngle      = { driver.povDegrees()?.let { 180.degrees - it.degrees } }
        val trackNoteInput   = driver.rightTrigger gt 0.25

        val continuousGyro   = DriveSubsystem::measuredAngle.makeContinuous(-180.degrees, 180.degrees)
        val gyroHistory      = continuousGyro maintainHistory 10.0


        var gyroOut by quickDashboard(0.0, persistent = false){ degrees }
        var historyOut by quickDashboard(0.0, persistent = false){ degrees }
        onPeriodic { gyroOut = continuousGyro(); historyOut = gyroHistory(1.0) }

        var alternateRotationInput: Double? = null
        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {
            zeroGyroCommand() whileTrue zeroGyroInput
            val frontCamera = PhotonCamera("front-camera")
//            trackNoteInput {
//
//                val last = { frontCamera.latestResult }.checkpoint
//                val lastTimestamp = { last().timestampSeconds }
//
//                val lowerCameraCutoff by quickDashboard(-87.09.degrees, name="ring-track/cutoff"){ degrees }
//
//                var unlocked by setOnStart { true }
//                var goal by setOnStart { continuousGyro() }
//                val hasNewInfo = lastTimestamp.hasChanged and { last().targets.isNotEmpty() } and { unlocked }
//                hasNewInfo{ onPeriodic {
//                    val target = last().targets[0]
//                    goal = continuousGyro() - target.yaw.degrees
//                    if(target.pitch.degrees < lowerCameraCutoff) unlocked = false
//                } }
//
//                val ringTrackKP by quickDashboard(10.0, name="ring-track/kP")
//                val ringTrackKI by quickDashboard(0.0, name="ring-track/kI")
//                val ringTrackKD by quickDashboard(0.7, name="ring-track/kD")
//                val ringTrackMaxVelocity by quickDashboard(1.5.rotations, name="ring-track/MaxVelocity"){ rotations }
//                val ringTrackMaxAccel by quickDashboard(0.75.rotations, name="ring-track/MaxAccel"){ rotations }
//                var goalOut by quickDashboard(0.0, persistent = false){ degrees }
//                onPeriodic { goalOut = goal }
//
//                val constraints = { Constraints(ringTrackMaxVelocity,ringTrackMaxAccel) }
////                val pidController = { State(goal,0.0) }.profiledPID(
////                    {ringTrackKP}, {ringTrackKI}, {ringTrackKD}, constraints, continuousGyro
////                )
//                val pidController = { goal }.pid(
//                    {ringTrackKP},
//                    {ringTrackKI},
//                    {ringTrackKD}
//                ){ continuousGyro() }
//                onPeriodic { alternateRotationInput = -pidController() }
//                onStop { alternateRotationInput = null }
//            }

            val pid = PIDController(10.0, 0.0, 0.0)
            SmartDashboard.putData("ringTrackPID", pid)
            val ringTrackKP by quickDashboard(5.0, name="ring-track/kP")
            val ringTrackKI by quickDashboard(0.1, name="ring-track/kI")
            val ringTrackKD by quickDashboard(0.0, name="ring-track/kD")
            val ringTrackCutoff by quickDashboard(-11.0.degrees){ degrees }
            command{
                val initialAngle = continuousGyro()
                var lastGoal = initialAngle
                var lastTimestamp = frontCamera.latestResult.timestampSeconds
                var unlocked = true
                pid.reset()
                pid.iZone = 15.degrees
                pid.setTolerance(1.5.degrees)
                var seesNote = false


                periodic {
//                    pid.p = if(AUTONOMOUS()) 10.0 else 2.0
                    pid.setPID(ringTrackKP, ringTrackKI, ringTrackKD)
                    val last = frontCamera.latestResult
                    if(last.timestampSeconds != lastTimestamp) {
                        if(last.targets!!.isNotEmpty() && unlocked){
                            seesNote = true
                            lastTimestamp = last.timestampSeconds
                            lastGoal = continuousGyro() - last.targets[0].yaw.degrees
                            if(last.targets[0].pitch.degrees < ringTrackCutoff) unlocked = false
                        }else {
                            seesNote = false
                        }
                    }

                    if(!unlocked) driver.bothRumble = 0.0
                    else if(seesNote == true) driver.bothRumble = 1.0
                    else driver.bothRumble = 0.25


                    val setpoint = lastGoal

                    val measured = continuousGyro()
                    val error = setpoint - measured

                    alternateRotationInput = if(unlocked) pid.calculate(measured, setpoint) else 0.0
                }

                onEnd { alternateRotationInput = null }
            } whileTrue trackNoteInput

            driveCommand (fieldRelative = !(robotCentric or (trackNoteInput and !AUTONOMOUS))) {
                ChassisSpeeds(forwardInput(), sideInput(), alternateRotationInput ?: rotationInput())
            } whileTrue always

            switch {
                case(reverseInput) {
                    IntakeSubsystem.reverseCommand() * TriggerSubsystem.reverseCommand() whileTrue always
                }
                case(topIntakeInput) {
                    TriggerSubsystem.reverseCommand() * FlywheelSubsystem.intakeCommand() whileTrue always
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
