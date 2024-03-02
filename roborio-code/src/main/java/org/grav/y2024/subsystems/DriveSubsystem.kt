package org.grav.y2024.subsystems

import com.gattagdev.geo.t2d
import com.gattagdev.newcommands.BetterSubsystem
import com.gattagdev.newcommands.command
import com.gattagdev.units.*
import com.revrobotics.ColorMatch
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import swervelib.parser.SwerveParser
import java.io.File

typealias CSS = () -> ChassisSpeeds


object DriveSubsystem: BetterSubsystem() {

    val maxSpeed = 14.5.feet

    val maxRotationRate = 5.0.rotations //TODO This is dummy data, fix me

    val swerveDrive = SwerveParser(File(Filesystem.getDeployDirectory(), "swerve")).createSwerveDrive(maxSpeed)!!

    fun driveCommand(fieldRelative: Boolean = true, stopOnEnd: Boolean = true, supplier: CSS): Command{
        return command{
            periodic {
                val cs = supplier()
                swerveDrive.drive(
                    t2d(cs.vxMetersPerSecond, cs.vyMetersPerSecond),
                    cs.omegaRadiansPerSecond,
                    fieldRelative,
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

    fun sim() {
        !RobotBase.isSimulation()
    }

    fun stopCommand() = command{

        periodic {
            swerveDrive.drive(ChassisSpeeds(0.0, 0.0, 0.0))
        }

    }

}