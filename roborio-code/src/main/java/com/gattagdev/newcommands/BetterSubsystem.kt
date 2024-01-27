package com.gattagdev.newcommands

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.SubsystemBase

private val subsystems = ArrayList<BetterSubsystem>()

fun runPostPeriodics(){
    subsystems.forEach{ it.postPeriodic() }
}

open class BetterSubsystem : SubsystemBase {

    constructor(){
        subsystems.add(this)

    }

    constructor(name: String) {
        this.name = name
        subsystems.add(this)
    }

    open fun postPeriodic() : Unit {

    }

    operator fun set(key: String, number: Double){
        SmartDashboard.putNumber("${name}/$key", number)
    }

    operator fun set(key: String, value: Boolean){
        SmartDashboard.putBoolean("${name}/$key", value)
    }
}

