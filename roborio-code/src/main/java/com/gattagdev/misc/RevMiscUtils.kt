package com.gattagdev.misc

import com.revrobotics.CANSparkLowLevel
import com.revrobotics.CANSparkMax

fun BrushedCANSparkMax(id: Int) = CANSparkMax(id, CANSparkLowLevel.MotorType.kBrushed)
fun BrushlessCANSparkMax(id: Int) = CANSparkMax(id, CANSparkLowLevel.MotorType.kBrushless)