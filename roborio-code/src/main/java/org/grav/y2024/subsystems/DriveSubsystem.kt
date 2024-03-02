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
import org.grav.y2024.Constants
import swervelib.parser.SwerveParser
import java.io.File

typealias CSS = () -> ChassisSpeeds


object DriveSubsystem: BetterSubsystem() {

    val swerveDrive = SwerveParser(File(Filesystem.getDeployDirectory(), "swerve")).createSwerveDrive(Constants.robotMaxSpeed)!!

    fun driveCommand(fieldRelative: Boolean = true, stopOnEnd: Boolean = true, supplier: CSS): Command{
        return command{
            periodic {
                val cs = supplier()
                swerveDrive.drive(
                    t2d(cs.vxMetersPerSecond, cs.vyMetersPerSecond),
                    cs.omegaRadiansPerSecond,
                    fieldRelative,
                    true
                )
            }
            onEnd {
                if(stopOnEnd){
                    swerveDrive.drive(ChassisSpeeds(0.0, 0.0, 0.0))
                }
            }
        }
    }

    fun sim() = command(){
        !RobotBase.isSimulation()
    }

    fun stopCommand() = command{

        periodic {
            swerveDrive.drive(ChassisSpeeds(0.0, 0.0, 0.0))
        }

    }

}