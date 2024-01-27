package com.gattagdev.nt

import edu.wpi.first.networktables.NetworkTable
import kotlin.reflect.KProperty


interface TableOwner{

    val table: NetworkTable
}

class SmartDashboardDelegate<T>(
    private val default: T,
    private val publish: SmartDashboardDelegate<T>.(value: T) -> Unit,
    private val retrieve: SmartDashboardDelegate<T>.(local: T) -> T
) {
    private var value: T = default

    init {
        this.publish(this, this.value)
    }

    operator fun getValue(owner: Any?, property: KProperty<*>): T{
        val old = this.value
        val new = this.retrieve(this, old)
        this.value = new
        if(new != old){
            this.publish(this, new)
        }
        return new
    }

    operator fun setValue(owner: Any?, property: KProperty<*>, value: T){
        this.value = value
        this.publish(this, value)
    }

}

interface SmartDashboardDelegateProvider<T>{
    operator fun provideDelegate(
        thisRef: TableOwner,
        prop: KProperty<*>
    ): SmartDashboardDelegate<T>
}

inline fun <reified T : Any> internalOnly(default: T, name: String? = null): SmartDashboardDelegateProvider<T> {
    val func: ((TableOwner, KProperty<*>) -> SmartDashboardDelegate<*>) = when(T::class){
        Double::class -> { owner, prop ->
            val topic = owner.table.getDoubleTopic(name ?: prop.name)
            val pub = topic.publish()
            val sub = topic.subscribe(default as Double)
            SmartDashboardDelegate<Double>(
                default,
                { v -> pub.set(v) },
                { l -> sub.get(l) }
            )
        }

        else -> throw IllegalArgumentException()
    }

    return func as SmartDashboardDelegateProvider<T>



}