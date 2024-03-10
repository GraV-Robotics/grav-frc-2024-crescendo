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
import kotlin.io.path.Path




object RobotContainer {

    val `Recording-Access-Enabled` by quickDashboard(false, persistent = false)

    init { eventLoopContext {

        var recordingPath = "recording.json"
        var replayAllowed = false

        chooser("Auto-Mode"){
            default("Do Nothing"){
                recordingPath = "do-nothing.json"
                replayAllowed = false
            }
            choice("Movement"){
                recordingPath = "movement.json"
                replayAllowed = true
            }
            choice("Center 1-Note"){
                recordingPath = "center-1-note.json"
                replayAllowed = true
            }
            choice("Center 2-Note"){
                recordingPath = "center-2-note.json"
                replayAllowed = true
            }
            choice("Center 3-Note"){
                recordingPath = "center-2-note.json"
                replayAllowed = true
            }
            choice("Center 4-Note"){
                recordingPath = "center-2-note.json"
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

            driver = BetterXboxController(0).also {
                record = trueTeleop and it.leftBumper and ::`Recording-Access-Enabled` and !DriverStation::isFMSAttached
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
        val robotCentric     = driver.leftBumper
        val zeroGyroInput    = driver.startButton
        val driverAngle      = { driver.povDegrees()?.let { 180.degrees - it.degrees } }
        val trackNoteInput   = driver.rightTrigger gt 0.25

        val continuousGyro = {DriveSubsystem.measuredAngle}.makeContinuous(-180.degrees, 180.degrees)
        var alternateRotationInput: Double? = null
        /* -------------------- TELEOP -------------------- */
        TELEOPERATED {

            zeroGyroCommand() whileTrue zeroGyroInput

            command{
                val constraints = TrapezoidProfile.Constraints(2.0.rotations,2.0.rotations)
                val getSetpoint = { driverAngle()!! }
                var currentSetpoint = getSetpoint()
                var profile = TrapezoidProfile(constraints)
                var startTime = 0.0

                periodic {
                    val newSetpoint = getSetpoint()
                    if(newSetpoint != currentSetpoint){
                        profile = TrapezoidProfile(constraints)
                        currentSetpoint = newSetpoint
                        startTime = elapsedTime
                    }
                    val output = profile.calculate(
                        (elapsedTime - startTime),
                        TrapezoidProfile.State(DriveSubsystem.measuredAngle, DriveSubsystem.measuredAngularRate),
                        TrapezoidProfile.State(currentSetpoint, 0.0)
                    )
                    val error = output.position - DriveSubsystem.measuredAngle
                    alternateRotationInput = -(error * 4)
                    println("${error.toDegrees}")
                }
                onEnd { alternateRotationInput = null }
            } whileTrue {driverAngle() != null}

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
