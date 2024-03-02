package com.gattagdev.pid

import com.revrobotics.CANSparkMax

fun CANSparkMax.setPID(config: PIDConfig) {
    TODO()
}
fun CANSparkMax.setPID(builder: PIDConfigBuilder.() -> Unit) = setPID(PIDConfig(builder))