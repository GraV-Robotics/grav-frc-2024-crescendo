package com.gattagdev.units

// --------------- DISTANCE ---------------

inline val Number.inches get() = this.toDouble() * 0.0254
inline val Number.toInches get() = this.toDouble() / 1.0.inches

inline val Number.feet get() = this.toDouble() * 0.3048
inline val Number.toFeet get() = this.toDouble() / 1.0.feet


// --------------- ROTATION ---------------

inline val Number.rotations get() = this.toDouble() * (2 * Math.PI)
inline val Number.toRotations get() = this.toDouble() / 1.0.rotations

inline val Number.degrees get() = this.toDouble() * (2 * Math.PI / 360.0)
inline val Number.toDegrees get() = this.toDouble() / 1.0.degrees


// --------------- SI ---------------

inline val Number.milli get() = this.toDouble() / 1000.0
inline val Number.toMilli get() = this.toDouble() * 1000.0

inline val Number.kilo get() = this.toDouble() * 1000.0
inline val Number.toKilo get() = this.toDouble() / 1000.0

// --------------- PERCENT ---------------

inline val Number.percent get() = this.toDouble() / 100.0
inline val Number.toPercent get() = this.toDouble() * 100.0


// --------------- TIME ---------------

inline val Number.minutes get() = this.toDouble() * 60.0

inline val Number.toMinutes get() = this.toDouble() / 1.0.minutes