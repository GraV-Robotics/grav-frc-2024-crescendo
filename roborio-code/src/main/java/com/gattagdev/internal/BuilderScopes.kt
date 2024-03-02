package com.gattagdev.internal

import java.lang.IllegalStateException


@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class BuilderScopes
class BuilderScope {

    @PublishedApi
    internal var closed = false

    inline fun <reified T> tryRun(body: (@BuilderScopes BuilderScope).() -> T): T{
        if(closed) throw IllegalStateException("This method can not be called after the builder closure has completed")
        return body()
    }

    @PublishedApi
    internal fun close(){
        closed = true
    }

}

inline fun <reified T> builderScope(body: (@BuilderScopes BuilderScope).() -> T): T{
    val builderScope = BuilderScope()
    try{
        return body(builderScope)
    }finally {
        builderScope.close()
    }
}