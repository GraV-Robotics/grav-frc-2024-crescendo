package org.grav.y2024.subsystems

import com.gattagdev.defered.BS
import com.gattagdev.geo.t2d
import com.gattagdev.mechanical.gearSystem
import com.gattagdev.mechanical.meshedGears
import com.gattagdev.newcommands.BetterSubsystem
import com.gattagdev.newcommands.command
import com.gattagdev.newcommands.eventLoopContext
import com.gattagdev.nt.quickDashboard
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
    val trueRobotMaxSpeed = 4.46
    val robotMaxRotationRate by quickDashboard(1.8.rotations){ rotations }

    val swerveDrive = SwerveParser(File(Filesystem.getDeployDirectory(), "swerve")).createSwerveDrive(trueRobotMaxSpeed)!!

    val wheelDiameter by quickDashboard(3.0.inches) { inches }
    val isOpenLoop by quickDashboard(false)
    val cosineCompensator by quickDashboard(true)
    val headingCorrection by quickDashboard(false)
    val velocityCorrection by quickDashboard(true)

    val angleJoyStickRadiusDeadband by quickDashboard(swerveDrive.swerveController.config.angleJoyStickRadiusDeadband)
    val headingKP by quickDashboard(swerveDrive.swerveController.config.headingPIDF.p)
    val headingKI by quickDashboard(swerveDrive.swerveController.config.headingPIDF.i)
    val headingKD by quickDashboard(swerveDrive.swerveController.config.headingPIDF.d)

    val driveGearing = (22.0/13.0) * (45.0 / 15.0)


    init {
        defaultCommand = stopCommand()
        eventLoopContext {
            ::cosineCompensator onChange { v -> swerveDrive.setCosineCompensator(v) }
            ::headingCorrection onChange { v -> swerveDrive.headingCorrection = v }
            ::velocityCorrection onChange { v -> swerveDrive.chassisVelocityCorrection = v }
            ::wheelDiameter onChange { v -> swerveDrive.modules.forEach {
                val const = (1/driveGearing) * Math.PI * v
                println(const)
                println(driveGearing)
                it.setDriveMotorConversionFactor(const)
            }}

        }
    }

    fun driveCommand(fieldRelative: BS = { true }, stopOnEnd: Boolean = true, supplier: CSS): Command{
        return command{
            periodic {
                val cs = supplier()
                swerveDrive.drive(
                    t2d(cs.vxMetersPerSecond, cs.vyMetersPerSecond),
                    cs.omegaRadiansPerSecond,
                    fieldRelative(),
                    isOpenLoop
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

        periodic {
            swerveDrive.zeroGyro()
            println("########## ZEROING GYRO ##########")
        }
    }

    val measuredAngle: Double get() = swerveDrive.gyroRotation3d.z
    val measuredAngularRate: Double get() = swerveDrive.robotVelocity.omegaRadiansPerSecond


    override fun periodic() {
//        swerveDrive.swerveController.
    }
}