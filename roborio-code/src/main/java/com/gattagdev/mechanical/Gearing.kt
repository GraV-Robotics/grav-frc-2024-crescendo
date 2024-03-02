package com.gattagdev.mechanical

import kotlin.math.min
import kotlin.reflect.KProperty0



interface GearStage {
    val ratio:      Double
    val inverted:   Boolean
    val oneWay:     Boolean
    val slop:       Double
    val maxRate:    Double
    val maxTorque:  Double
    val moment:     Double
    val efficiency: Double
    val name:       String

    companion object {
        operator fun invoke(
            ratio:      Double,
            inverted:   Boolean = false,
            oneWay:     Boolean = false,
            slop:       Double  = 0.0,
            maxRate:    Double  = Double.POSITIVE_INFINITY,
            maxTorque:  Double  = Double.POSITIVE_INFINITY,
            moment:     Double  = 0.0,
            efficiency: Double  = 1.0,
            name:       String  = "Unnamed Stage"
        ): GearStage = GearStageImpl(
            ratio      = ratio,
            inverted   = inverted,
            oneWay     = oneWay,
            slop       = slop,
            maxRate    = maxRate,
            maxTorque  = maxTorque,
            moment     = moment,
            efficiency = efficiency,
            name       = name
        )
    }
}

interface GearSystem: GearStage {
    val stages: List<GearStage>
    companion object{
        operator fun invoke(name: String = "Unnamed System", builder: GearSystemBuilder.() -> Unit): GearSystem{
            val stages = mutableListOf<GearStage>()
            val bo = object: GearSystemBuilder{
                override fun add(gearStage: GearStage) {
                    stages.add(gearStage)
                }
            }
            return GearSystemImpl(stages, name)
        }
    }
}

class GearStageImpl(
    override val ratio: Double,
    override val inverted: Boolean,
    override val oneWay: Boolean = false,
    override val slop: Double = 0.0,
    override val maxRate: Double = Double.POSITIVE_INFINITY,
    override val maxTorque: Double = Double.POSITIVE_INFINITY,
    override val moment: Double,
    override val efficiency: Double,
    override val name: String
): GearStage{
    init {
        propertyValidation(::ratio,      pvNonNan, pvPositive)
        propertyValidation(::slop,       pvNonNan, pvNonNegative)
        propertyValidation(::maxRate,    pvNonNan, pvPositive)
        propertyValidation(::maxTorque,  pvNonNan, pvPositive)
        propertyValidation(::moment,     pvNonNan, pvNonNegative)
        propertyValidation(::efficiency, pvNonNan, pvPositive,    pvNoGreaterThanOne)
    }
}

class GearSystemImpl(stages: Iterable<GearStage>, override val name: String = "Unnamed System"): GearSystem {
    override val stages = stages.toList()
    override val ratio: Double
    override val inverted: Boolean
    override val oneWay: Boolean
    override val slop: Double
    override val maxRate: Double
    override val maxTorque: Double
    override val moment: Double
    override val efficiency: Double

    init {
        var ratio      = 1.0
        var inverted   = false
        var oneWay     = false
        var slop       = 0.0
        var maxRate    = Double.POSITIVE_INFINITY
        var maxTorque  = Double.POSITIVE_INFINITY
        var moment     = 0.0
        var efficiency = 1.0

        this.stages.forEach { stage ->
            moment     += stage.moment * ratio
            ratio      *= stage.ratio
            inverted   = inverted xor stage.inverted
            oneWay     = oneWay || stage.oneWay
            slop       = (slop / stage.ratio) * stage.slop
            maxRate    = min(maxRate / stage.ratio, stage.maxTorque)
            maxTorque  = min(maxTorque * stage.ratio, stage.maxTorque)
            efficiency *= stage.efficiency
        }

        this.ratio      = ratio
        this.inverted   = inverted
        this.oneWay     = oneWay
        this.slop       = slop
        this.maxRate    = maxRate
        this.maxTorque  = maxTorque
        this.moment     = moment
        this.efficiency = efficiency
    }
}

interface GearSystemBuilder{
    fun add(gearStage: GearStage)
    operator fun GearStage.unaryPlus() = add(this)
}

fun gearSystem(builder: GearSystemBuilder.() -> Unit, name: String = "Unnamed System"): GearSystem {
    val stages = mutableListOf<GearStage>()
    val bo = object: GearSystemBuilder{
        override fun add(gearStage: GearStage) {
            stages.add(gearStage)
        }
    }
    return GearSystemImpl(stages, name)
}


typealias DPV = (propName: String, stageName: String, value: Double) -> String?

internal fun GearStage.propertyValidation(property: KProperty0<Double>, vararg validators: DPV): Unit {
    validators.forEach {
        it(property.name, this.name, property.get()).let {
            if(it != null) throw IllegalArgumentException(it)
        }
    }
}

internal val pvNoGreaterThanOne: DPV = { p, s, v -> if(v >= 1.0)  "$p of $s must not be greater than 1.0" else null }
internal val pvPositive: DPV         = { p, s, v -> if(v <= 0)    "$p of $s must be positive" else null }
internal val pvNonNegative: DPV      = { p, s, v -> if(v < 0)     "$p of $s must be non negative" else null }
internal val pvNonNan: DPV           = { p, s, v -> if(v.isNaN()) "$p of $s must not be NaN" else null }