package com.gattagdev.defered

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.button.Trigger

typealias BS = ()->Boolean


operator fun BS.not(): BS = { !(this()) }

infix fun BS.or(right: Boolean): BS = { this() || right }
infix fun BS.or(right: BS): BS = { this() || right() }
infix fun Boolean.or(right: BS): BS = { this || right() }

operator fun BS.plus(right: Boolean): BS = { this() || right }
operator fun BS.plus(right: BS): BS = { this() || right() }
operator fun Boolean.plus(right: BS): BS = { this || right() }

infix fun BS.and(right: Boolean): BS = { this() && right }
infix fun BS.and(right: BS): BS = { this() && right() }
infix fun Boolean.and(right: BS): BS = { this && right() }

operator fun BS.times(right: Boolean): BS = { this() && right }
operator fun BS.times(right: BS): BS = { this() && right() }
operator fun Boolean.times(right: BS): BS = { this && right() }

infix fun BS.xor(right: Boolean): BS = { this().xor(right) }
infix fun BS.xor(right: BS): BS = { this().xor(right()) }
infix fun Boolean.xor(right: BS): BS = { this.xor(right()) }

fun BS.toDouble(trueCase: Double, falseCase: Double): DS = { if (this()) trueCase else falseCase }
fun BS.toDouble(trueCase: DS, falseCase: Double): DS = { if (this()) trueCase() else falseCase }
fun BS.toDouble(trueCase: Double, falseCase: DS): DS = { if (this()) trueCase else falseCase() }
fun BS.toDouble(trueCase: DS, falseCase: DS): DS = { if (this()) trueCase() else falseCase() }

infix fun BS.toDouble(trueCase: Double): DS = { if (this()) trueCase else 0.0 }
infix fun BS.toDouble(trueCase: DS): DS = { if (this()) trueCase() else 0.0 }

val BS.normal: DS
    get() = { if (this()) 1.0 else 0.0 }


infix fun BS.whileTrue(command: Command){
    Trigger(this).whileTrue(command)
}

infix fun BS.onTrue(command: Command){
    Trigger(this).onTrue(command)
}
