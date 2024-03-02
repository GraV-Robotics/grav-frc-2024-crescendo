package com.gattagdev.mechanical

import com.gattagdev.units.percent

fun GearSystemBuilder.meshedGears(vararg gearTeeth: Int, name: String = "Simple Gears"){
    add(GearSystem(name = name){
        val iter = gearTeeth.iterator()
        var prev = iter.next()
        var count = 1
        iter.forEach {
            count++
            add(GearStage(
                ratio = (1.0 * it)/prev,
                inverted = true,
                efficiency = 98.percent
            ))
            prev = it
        }
        if(count < 2) throw IllegalArgumentException("There must be at least two gears")
    })
}

internal fun GearSystemBuilder.chainLikeSystem(
    inputCount: Double,
    outputCount: Double,
    name: String
){
    add(GearStage(
        ratio = outputCount.toDouble()/inputCount,
        name = name
    ))
}

fun GearSystemBuilder.chain(
    inputSprocket: Int,
    outputSprocket: Int,
    name: String = "Simple Chain"
){
    this.chainLikeSystem(inputSprocket.toDouble(), outputSprocket.toDouble(), name)
}


fun GearSystemBuilder.inverter(name: String = "Inverter"){
    add(GearStage(
        ratio = 1.0,
        inverted = true,
        name = name
    ))
}

// ------------------------------ REV ROBOTICS ------------------------------
fun GearSystemBuilder.ultraPlanetary(vararg ratios: Int, name: String = "UltraPlanetary System"): Unit {
    add(GearSystem(name = name){
        ratios.forEach {
            when(it){
                3 -> add(ULTRA_PLANETARY_CARTRIDGE_3)
                4 -> add(ULTRA_PLANETARY_CARTRIDGE_4)
                5 -> add(ULTRA_PLANETARY_CARTRIDGE_5)
                else -> throw IllegalArgumentException("$it is not a valid UltraPlanetary ratio")
            }
        }
    })
}

val ULTRA_PLANETARY_CARTRIDGE_3 = GearStage(
    ratio     = 84.0/29.0,
    maxTorque = 40.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name      = "UltraPlanetary Cartridge 3:1"
)
val ULTRA_PLANETARY_CARTRIDGE_4 = GearStage(
    ratio = 76.0/21.0,
    maxTorque = 40.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name="UltraPlanetary Cartridge 4:1"
)
val ULTRA_PLANETARY_CARTRIDGE_5 = GearStage(
    ratio = 68.0/13.0,
    maxTorque = 40.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name="UltraPlanetary Cartridge 5:1"
)


fun GearSystemBuilder.maxPlanetary(vararg ratios: Int, hexOutput: Boolean = false): Unit {
    add(GearSystem(name = "MaxPlanetary System"){
        ratios.forEach {
            when(it){
                3 -> add(MAX_PLANETARY_CARTRIDGE_3)
                4 -> add(MAX_PLANETARY_CARTRIDGE_4)
                5 -> add(MAX_PLANETARY_CARTRIDGE_5)
                else -> throw IllegalArgumentException("$it is not a valid MaxPlanetary ratio")
            }
        }
        if(hexOutput) add(MAX_PLANETARY_HEX_OUTPUT)
    })
}

val MAX_PLANETARY_CARTRIDGE_3 = GearStage(
    ratio = 3.0,
    maxTorque = 290.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "MaxPlanetary Cartridge 3:1"
)
val MAX_PLANETARY_CARTRIDGE_4 = GearStage(
    ratio = 4.0,
    maxTorque = 270.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "MaxPlanetary Cartridge 4:1"
)
val MAX_PLANETARY_CARTRIDGE_5 = GearStage(
    ratio = 3.0,
    maxTorque = 240.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "MaxPlanetary Cartridge 5:1"
)

val MAX_PLANETARY_HEX_OUTPUT = GearStage(
    ratio = 1.0,
    maxTorque = 250.0,
    name = "MaxPlanetary Hex Output"
)

// ------------------------------ VEX ROBOTICS ------------------------------

fun GearSystemBuilder.versaPlanetary(vararg ratios: Int): Unit {
    add(GearSystem(name = "VersaPlanetary System"){
        ratios.forEach {
            when(it){
                3 -> add(VERSA_PLANETARY_CARTRIDGE_3)
                4 -> add(VERSA_PLANETARY_CARTRIDGE_4)
                5 -> add(VERSA_PLANETARY_CARTRIDGE_5)
                7 -> add(VERSA_PLANETARY_CARTRIDGE_7)
                9 -> add(VERSA_PLANETARY_CARTRIDGE_9)
                10 -> add(VERSA_PLANETARY_CARTRIDGE_10)
                else -> throw IllegalArgumentException("$it is not a valid VersaPlanetary ratio")
            }
        }
    })
}


val VERSA_PLANETARY_CARTRIDGE_3 = GearStage(
    ratio = 3.0,
    maxTorque = 154.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "VersaPlanetary Cartridge 3:1"
)
val VERSA_PLANETARY_CARTRIDGE_4 = GearStage(
    ratio = 4.0,
    maxTorque = 154.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "VersaPlanetary Cartridge 4:1"
)
val VERSA_PLANETARY_CARTRIDGE_5 = GearStage(
    ratio = 5.0,
    maxTorque = 154.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "VersaPlanetary Cartridge 5:1"
)
val VERSA_PLANETARY_CARTRIDGE_7 = GearStage(
    ratio = 7.0,
    maxTorque = 100.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "VersaPlanetary Cartridge 7:1"
)
val VERSA_PLANETARY_CARTRIDGE_9 = GearStage(
    ratio = 9.0,
    maxTorque = 100.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "VersaPlanetary Cartridge 9:1"
)
val VERSA_PLANETARY_CARTRIDGE_10 = GearStage(
    ratio = 10.0,
    maxTorque = 100.0,
    efficiency = 97.percent, // THIS IS A GUESS
    name = "VersaPlanetary Cartridge 10:1"
)
