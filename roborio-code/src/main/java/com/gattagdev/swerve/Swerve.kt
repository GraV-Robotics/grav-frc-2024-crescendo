package com.gattagdev.swerve


fun <S: SwerveSystem> buildSwerve(builder: SwerveBuilder<S>): SwerveController<S> {
    return TODO()
}



interface SwerveController<S: SwerveSystem>

interface SwerveBuilder<S: SwerveSystem>{
    fun system(builder: SwerveSystemBuilder<S>.() -> Unit): Unit
    fun module(builder: Swerve)
}

interface SwerveSystemBuilder<S: SwerveSystem>


interface SwerveModuleBuilder<S: SwerveSystem>{

}
interface SwerveModuleHardwareBuilder<S: SwerveSystem>{

}


interface SwerveSimulationBuilder<S: SwerveSystem>


interface SwerveSystem