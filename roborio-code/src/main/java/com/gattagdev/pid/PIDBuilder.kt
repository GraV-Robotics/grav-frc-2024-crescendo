package com.gattagdev.pid


class PIDConfig constructor(val builder: PIDConfigBuilder.() -> Unit)

interface PIDConfigBuilder{
    fun p(value: Double)
    fun i(value: Double)
    fun d(value: Double)
    fun ff(value: FeedForwardConfig)

}



sealed interface FeedForwardConfig{
    fun computeFF(setpoint: Double, measured: Double): Double
}

fun PIDConfigBuilder.constantFF(constant: Double) = this.ff(ConstantFFC(constant))
class ConstantFFC(val constant: Double): FeedForwardConfig{
    override fun computeFF(setpoint: Double, measured: Double): Double = constant
}

fun PIDConfigBuilder.linearFF(linear: Double, constant: Double = 0.0) = this.ff(LinearFFC(linear, constant))
class LinearFFC(val linear: Double, val constant: Double): FeedForwardConfig{
    override fun computeFF(setpoint: Double, measured: Double): Double = (setpoint) * linear + constant
}

fun PIDConfigBuilder.quadraticFF(quadratic: Double, linear: Double, constant: Double) = this.ff(QuadraticFFC(quadratic, linear, constant))
class QuadraticFFC(val quadratic: Double, val linear: Double, val constant: Double): FeedForwardConfig{
    override fun computeFF(setpoint: Double, measured: Double): Double{
        return quadratic * (setpoint * setpoint) + linear * setpoint + constant
    }
}

fun PIDConfigBuilder.functionFF(func: (setpoint: Double, measured: Double) -> Double) = this.ff(FunctionFFC(func))
class FunctionFFC(val func: (setpoint: Double, measured: Double) -> Double): FeedForwardConfig{
    override fun computeFF(setpoint: Double, measured: Double): Double = func(setpoint, measured)
}



