package com.gattagdev.nt

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard.clearPersistent
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard.setPersistent
import edu.wpi.first.wpilibj2.command.Subsystem
import java.util.logging.Logger
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty


inline fun <IN, reified OUT> quickDashboard(
    default: IN,
    mapper: BidirectionalMapper<IN, OUT>,
    name: String? = null,
    ignoreScope: Boolean = false,
    persistent: Boolean = true
) = PropertyDelegateProvider { thisRef: Any?, property: KProperty<*> ->
    val trueName = (when(thisRef){
        {ignoreScope} -> null
        is Subsystem -> thisRef.name
        else -> null
    }?.let { "$it/" } ?: "") + (name ?: property.name)

    @Suppress("UNCHECKED_CAST")
    val builder = when(OUT::class){
        Double::class  -> quickSD(SmartDashboard::getNumber,  SmartDashboard::putNumber)
        Boolean::class -> quickSD(SmartDashboard::getBoolean, SmartDashboard::putBoolean)
        String::class  -> quickSD(SmartDashboard::getString,  SmartDashboard::putString)
        else -> throw UnsupportedOperationException("quickDashboard does not support type ${OUT::class.qualifiedName}")
    } as (String, OUT) -> Property<OUT>
    val outDefault = mapper.toOut(default)
    var willClear = !persistent
    val prop = builder(trueName, outDefault)
    val prevDefaultName = ".default/${trueName}"
    if(persistent){
        setPersistent(trueName)
        setPersistent(prevDefaultName)
        val defProp = builder(prevDefaultName, outDefault)
        val oldDef = defProp.value
        if(oldDef != outDefault) willClear = true
        defProp.value = outDefault
    } else{
        clearPersistent(trueName)
        clearPersistent(prevDefaultName)
    }

    if(willClear || !SmartDashboard.containsKey(trueName)){
        prop.value = outDefault
    }

    if(prop.value != outDefault){
        Logger.getAnonymousLogger().warning("SmartDashboard key (${trueName}) persistent value (${prop.value}) does not match in code default value (${outDefault})")
    }

    quickRW(
        getter = { mapper.toIn(prop.value) },
        setter = { value -> prop.value = mapper.toOut(value) }
    )
}

inline fun <reified T> quickDashboard(
    default: T,
    name: String? = null,
    ignoreScope: Boolean = false,
    persistent: Boolean = true
) = quickDashboard<T, T>(
    default = default,
    mapper = object: BidirectionalMapper<T, T>{
        override fun toOut(inValue: T) = inValue
        override fun toIn(outValue: T) = outValue
    },
    name = name,
    ignoreScope = ignoreScope,
    persistent = persistent
)

fun quickDashboard(
    default: Double,
    name: String? = null,
    ignoreScope: Boolean = false,
    persistent: Boolean = true,
    displayUnits: (Double.() -> Double)
) = quickDashboard<Double, Double>(
    default = default,
    mapper = displayUnitsBuilder(displayUnits),
    name = name,
    ignoreScope = ignoreScope,
    persistent = persistent
)

@PublishedApi
internal fun <T> quickSD(
    getter: (String, T) -> T,
    setter: (String, T) -> Unit
): (String, T) -> Property<T> = { name: String, default: T ->
    Property(
        getter = { getter(name, default) },
        setter = { value -> setter(name, value)}
    )
}

fun <T> quickRW(getter: () -> T, setter: (T) -> Unit) = object: ReadWriteProperty<Any?, T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter()
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setter(value)
}


class Property<T>(
    private val getter: () -> T,
    private val setter: (T) -> Unit
){
    var value: T
        get() = getter()
        set(value) = setter(value)
}

interface BidirectionalMapper<IN, OUT>{
    fun toOut(inValue: IN): OUT
    fun toIn(outValue: OUT): IN
}

fun displayUnitsBuilder(units: Double.() -> Double): BidirectionalMapper<Double, Double>{
    val toInFactor = units(1.0)
    val toOutFactor = 1.0/toInFactor
    return object: BidirectionalMapper<Double, Double>{
        override fun toOut(inValue: Double) = toOutFactor * inValue
        override fun toIn(outValue: Double) = toInFactor * outValue
    }
}

