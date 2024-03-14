package com.gattagdev.geo

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d

typealias T2D = Translation2d
typealias R2D = Rotation2d

fun t2d(x: Double, y: Double) = T2D(x, y)

operator fun T2D.plus(right: T2D): T2D = this.plus(right)
operator fun T2D.minus(right: T2D): T2D = this.minus(right)
operator fun T2D.times(right: Double): T2D = this.times(right)
operator fun T2D.div(right: Double): T2D = this.div(right)

val Double.r2d: R2D get() = R2D.fromRadians(this)