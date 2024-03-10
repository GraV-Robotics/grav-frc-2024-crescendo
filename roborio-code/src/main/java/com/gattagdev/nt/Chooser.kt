package com.gattagdev.nt

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard


interface ChooserBuilder{

    fun choice(name: String, default: Boolean = false, onSelection: () -> Unit)
    fun default(name: String, onSelection: () -> Unit) = choice(name, default = true, onSelection)
}

fun chooser(name: String, builder: ChooserBuilder.() -> Unit){
    val chooserName = name
    val chooser = SendableChooser<Int>()
    var nextId = 1
    val selectionHandlers = mutableMapOf<Int, () -> Unit>()
    var defaultId: Int? = null

    builder(object: ChooserBuilder {
        override fun choice(name: String, default: Boolean, onSelection: () -> Unit) {
            val id = nextId++
            if(default){
                if(defaultId != null) throw IllegalArgumentException("Chooser (${chooserName}) can only have one default")
                defaultId = id
                chooser.setDefaultOption(name, id)
            }else{
                chooser.addOption(name, id)
            }
            selectionHandlers[id] = onSelection
        }
    })
    if(defaultId == null) throw IllegalArgumentException("Chooser (${chooserName}) must have one default")

    chooser.onChange { selectionHandlers[it ?: defaultId!!]?.invoke() }
    SmartDashboard.putData(name, chooser)
}