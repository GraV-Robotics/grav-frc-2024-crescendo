package com.gattagdev.mechanical

import com.gattagdev.units.minutes
import com.gattagdev.units.rotations


interface Motor {
    val clockwise: Boolean
    val kv: Double
    val kt: Double
    val name: String

    companion object{
        operator fun invoke(
            clockwise: Boolean,
            kv: Double,
            kt: Double,
            name: String = "Unnamed Motor"
        ) = MotorImpl(
            clockwise = clockwise,
            kv = kv,
            kt = kt,
            name = name
        )
    }
}

open class MotorImpl(
    override val clockwise: Boolean,
    override val kv: Double,
    override val kt: Double,
    override val name: String = "Unnamed Motor"
): Motor

interface EncoderMotor{
    val countsPerRad: Double
}

interface RevMotor: EncoderMotor{
    override val countsPerRad: Double get() = 42 * (1/1.rotations)
}


object REVNeoMotor: MotorImpl(
    clockwise = false,
    kv = 473.0 * (1.0.rotations / 1.0.minutes),
    kt = 2.6 / 105.0,
    name = "Rev Neo"
), RevMotor

object RevNeo550Motor: MotorImpl(
    clockwise = false,
    kv = 917.0 * (1.0.rotations / 1.0.minutes),
    kt = 0.0,
    name = "Rev Neo 550"
), RevMotor