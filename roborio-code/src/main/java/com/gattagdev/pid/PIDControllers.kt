package com.gattagdev.pid

import com.revrobotics.CANSparkMax

fun CANSparkMax.setPID(config: PIDConfig) {
    val controller = this
    val pid = controller.pidController
    config.builder(object : PIDConfigBuilder {
        override fun p(value: Double) {
            pid.p = value
        }

        override fun i(value: Double) {
            pid.i = value
        }

        override fun d(value: Double) {
            pid.d = value
        }

        override fun ff(value: FeedForwardConfig) {
            when(value){
                is LinearFFC -> {
                    if(value.constant != 0.0) throw IllegalArgumentException("This controller does not support non zero constants in feed forward")
                    pid.ff = value.linear
                }
                else -> {
                    throw IllegalArgumentException("This feed forward mode is not supported for this controller config")
                }
            }
        }
    })
}
fun CANSparkMax.setPID(builder: PIDConfigBuilder.() -> Unit) = setPID(PIDConfig(builder))