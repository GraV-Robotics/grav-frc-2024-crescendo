package com.gattagdev.misc

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// TODO This implementation will eventually be replaced when we migrate to JDK 21

class Context<T> (private val default: T){
    private var threadLocal = ThreadLocal.withInitial{ default }

    @OptIn(ExperimentalContracts::class)
    fun <R>runWith(value: T, block: () -> R): R{
        contract { callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE) }
        val previous = threadLocal.get()
        threadLocal.set(value)
        try {
            return block()
        } finally {
            threadLocal.set(previous)
        }
    }

    val value: T get() = threadLocal.get()

}