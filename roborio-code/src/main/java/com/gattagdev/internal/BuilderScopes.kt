package com.gattagdev.internal

import java.lang.IllegalStateException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class BuilderScopes
class BuilderScope {

    @PublishedApi
    internal var closed = false


    @OptIn(ExperimentalContracts::class)
    inline fun <reified T> tryRun(body: (@BuilderScopes BuilderScope).() -> T): T{
        contract { callsInPlace(body, InvocationKind.EXACTLY_ONCE) }
        if(closed) throw IllegalStateException("This method can not be called after the builder closure has completed")
        return body()
    }

    @PublishedApi
    internal fun close(){
        closed = true
    }

}

@OptIn(ExperimentalContracts::class)
inline fun <reified T> builderScope(body: (@BuilderScopes BuilderScope).() -> T): T{
    contract { callsInPlace(body, InvocationKind.EXACTLY_ONCE) }
    val builderScope = BuilderScope()
    try{
        return body(builderScope)
    }finally {
        builderScope.close()
    }
}