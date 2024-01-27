package com.gattagdev.defered

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.min
import kotlin.math.max

typealias DS = ()->Double

// ---------- DOUBLE SUPPLIER EXTENSIONS ----------

infix operator fun DS.plus(right: Double): DS = { this() + right }
infix operator fun DS.plus(right: DS): DS = { this() + right() }
infix operator fun Double.plus(right: DS): DS = { this + right() }

infix operator fun DS.minus(right: Double): DS = { this() - right }
infix operator fun DS.minus(right: DS): DS = { this() - right() }
infix operator fun Double.minus(right: DS): DS = { this - right() }

infix operator fun DS.times(right: Double): DS = { this() * right }
infix operator fun DS.times(right: DS): DS = { this() * right() }
infix operator fun Double.times(right: DS): DS = { this * right() }

infix operator fun DS.div(right: Double): DS = { this() / right }
infix operator fun DS.div(right: DS): DS = { this() / right() }
infix operator fun Double.div(right: DS): DS = { this / right() }

infix operator fun DS.rem(right: Double): DS = { this() % right }
infix operator fun DS.rem(right: DS): DS = { this() % right() }
infix operator fun Double.rem(right: DS): DS = { this % right() }

infix fun DS.eq(right: Double): BS = { this() == right }
infix fun DS.eq(right: DS): BS = { this() == right() }
infix fun Double.eq(right: DS): BS = { this == right() }

infix fun DS.neq(right: Double): BS = { this() != right }
infix fun DS.neq(right: DS): BS = { this() != right() }
infix fun Double.neq(right: DS): BS = { this != right() }

infix fun DS.gt(right: Double): BS = { this() > right }
infix fun DS.gt(right: DS): BS = { this() > right() }
infix fun Double.gt(right: DS): BS = { this > right() }

infix fun DS.gte(right: Double): BS = { this() >= right }
infix fun DS.gte(right: DS): BS = { this() >= right() }
infix fun Double.gte(right: DS): BS = { this >= right() }

infix fun DS.lt(right: Double): BS = { this() < right }
infix fun DS.lt(right: DS): BS = { this() < right() }
infix fun Double.lt(right: DS): BS = { this < right() }

infix fun DS.lte(right: Double): BS = { this() <= right }
infix fun DS.lte(right: DS): BS = { this() <= right() }
infix fun Double.lte(right: DS): BS = { this <= right() }

infix fun DS.pow(right: Double): DS = { this().pow(right) }
infix fun DS.pow(right: DS): DS = { this().pow(right()) }
infix fun Double.pow(right: DS): DS = { this.pow(right()) }

infix fun DS.signPow(right: Double): DS = { val left = this(); abs(left.pow(right)) * left.sign }
infix fun DS.signPow(right: DS): DS = { val left = this(); abs(left.pow(right())) * left.sign }
infix fun Double.signPow(right: DS): DS = { abs(this.pow(right())) * this.sign }

infix fun DS.min(right: Double): DS = { min(this(), right) }
infix fun DS.min(right: DS): DS = { min(this(), right()) }
infix fun Double.min(right: DS): DS = { min(this, right()) }

infix fun DS.max(right: Double): DS = { max(this(), right) }
infix fun DS.max(right: DS): DS = { max(this(), right()) }
infix fun Double.max(right: DS): DS = { max(this, right()) }

infix fun DS.clipAbs(band: Double): DS = { val r = this(); if (abs(r) >= band) band * r.sign else r }
infix fun DS.clipAbs(band: DS): DS = { val r = this(); val br = abs(band()); if (abs(r) >= br) br * r.sign else r }
infix fun Double.clipAbs(band: DS): DS = { val br = abs(band()); if (abs(this) >= br) br * this.sign else this }

infix fun DS.deadBand(band: Double): DS = this.centerAndDeadBand({0.0}, {band})
infix fun DS.deadBand(band: DS): DS = this.centerAndDeadBand({0.0}, band)
infix fun Double.deadBand(band: DS): DS = { this }.centerAndDeadBand({0.0}, band)


val DS.abs: DS
    get() = { abs(this()) }
fun abs(supplier: DS): DS = supplier.abs

val DS.sign: DS
    get() = { this().sign }
fun sign(supplier: DS): DS = supplier.sign

operator fun DS.unaryMinus(): DS = { -(this()) }
fun DS.invert(): DS = { -(this()) }

val DS.ofNow: DS
    get() {
        val save = this()
        return { save }
    }

fun DS.centerAndDeadBand(center: DS, band: DS): DS = {
    val read = this()
    val centerR = center()
    val bandR = abs(band())
    val rs = (read - centerR).sign
    val aRead = abs(read - centerR)
    if (aRead <= bandR) 0.0 else rs * (aRead - bandR) / (1.0 - bandR - centerR * rs)
}
