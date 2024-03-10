package org.grav.y2024.subsystems

import com.gattagdev.defered.BS
import com.gattagdev.geo.t2d
import com.gattagdev.newcommands.BetterSubsystem
import com.gattagdev.newcommands.command
import com.gattagdev.units.*
import com.kauailabs.navx.frc.AHRS
import com.revrobotics.ColorMatch
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import swervelib.parser.SwerveParser
import java.io.File

typealias CSS = () -> ChassisSpeeds


object DriveSubsystem: BetterSubsystem() {
    val robotMaxSpeed = 4.46
    val robotMaxRotationRate = 1.8.rotations

    val swerveDrive = SwerveParser(File(Filesystem.getDeployDirectory(), "swerve")).createSwerveDrive(robotMaxSpeed)!!

    init {
        defaultCommand = stopCommand()
    }

    fun driveCommand(fieldRelative: BS = { true }, stopOnEnd: Boolean = true, supplier: CSS): Command{
        return command{
            periodic {
                val cs = supplier()
                swerveDrive.drive(
                    t2d(cs.vxMetersPerSecond, cs.vyMetersPerSecond),
                    cs.omegaRadiansPerSecond,
                    fieldRelative(),
                    false
                )
            }
            onEnd {
                if(stopOnEnd){
                    swerveDrive.drive(ChassisSpeeds(0.0, 0.0, 0.0))
                }
            }
        }
    }

    fun stopCommand() = command(DriveSubsystem){
        periodic {
            swerveDrive.drive(ChassisSpeeds(0.0, 0.0, 0.0))
        }
    }

    fun zeroGyroCommand() = command{
        periodic { swerveDrive.zeroGyro() }
    }

    val measuredAngle: Double get() = swerveDrive.gyroRotation3d.z
    val measuredAngularRate: Double get() = swerveDrive.robotVelocity.omegaRadiansPerSecond


    override fun periodic() {
//        println(measuredAngle)
    }
}