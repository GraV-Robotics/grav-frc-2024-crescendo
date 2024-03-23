package com.gattagdev.newcommands

import com.gattagdev.defered.BS
import com.gattagdev.defered.DS
import com.gattagdev.pid.PIDConfig
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.controller.ProfiledPIDController
import edu.wpi.first.math.trajectory.TrapezoidProfile
import kotlin.math.absoluteValue

context(EventLoopContext)
inline val <reified T> (() -> T).checkpoint: (() -> T) get(){
    var savedValue: T? = null
    val provider = this
    onPeriodic{ savedValue = provider() }
    return { savedValue!! }
}

context(EventLoopContext)
val <T> (() -> T).hasChanged: BS get(){
    var first = true
    var lastValue: T? = null
    var hasChanged = false
    val provider = this
    onStart { first = true }
    onPeriodic {
        val current = provider()
        hasChanged = current != lastValue || first
        lastValue = current
        first = false
    }
    return { hasChanged }
}


context(EventLoopContext)
fun DS.pid(
    measurement: DS,
    kp: DS = {0.0},
    kd: DS = {0.0},
    ki: DS = {0.0},
    iZone: DS = { Double.MAX_VALUE },
    tolerance: DS = { 0.05 }
): DS {
    val goal = this@pid
    var output = 0.0
    lateinit var pidController : PIDController
    onStart {
        pidController = PIDController(
            0.0,
            0.0,
            0.0,
            period
        )
    }
    onPeriodic {
        pidController.setPID(kp(), kd(), ki())
        pidController.iZone = iZone()
        pidController.setTolerance(tolerance())
        output = pidController.calculate(measurement(), goal())
    }
    return { output }
}
context(EventLoopContext)
fun (() -> TrapezoidProfile.State).profiledPID(
    kp: DS = {0.0},
    kd: DS = {0.0},
    ki: DS = {0.0},
    constraints: () -> TrapezoidProfile.Constraints,
    measurement: DS
): DS {
    val goal = this@profiledPID
    var output = 0.0
    lateinit var pidController : ProfiledPIDController
    onStart {
        pidController = ProfiledPIDController(
            0.0,
            0.0,
            0.0,
            TrapezoidProfile.Constraints(0.0, 0.0),
            period
        )
    }
    onPeriodic {
        pidController.setPID(kp(), kd(), ki())
        output = pidController.calculate(measurement(), goal(), constraints())
    }
    return { output }
}

context(EventLoopContext)
inline fun <reified T> (() -> T).maintainHistory(
    timeSaved: Double,
    crossinline timeSupplier: EventLoopExecutable.() -> Double = { recognizedTime },
    crossinline interpolate: (older: T, newer: T, bias: Double) -> T
): ((Double) -> T){
    var period: Double = 0.02
    lateinit var queue: ArrayDeque<Pair<Double, T>>
    val provider = this
    onStart {
        period = this.period
        queue = ArrayDeque((timeSaved/period).toInt() + 2)
    }
    onPeriodic {
        val time = timeSupplier(this)
        val lastTime = time - timeSaved
        queue.addFirst(time to provider())
        while(queue.lastOrNull()?.let { it.first < lastTime } == true) queue.removeLast()
    }

    return {requestedTime ->
        if(queue.isEmpty()) throw IllegalStateException("No history available")
        val mostRecent = queue.first()
        val leastRecent = queue.last()
        if(mostRecent.first <= requestedTime || queue.size == 1) mostRecent.second
        else if(leastRecent.first >= requestedTime) leastRecent.second
        else {
            val start = (((mostRecent.first - requestedTime)/period).toInt() - 1).coerceAtLeast(0)
            var result = 0
//            for (i in start..<queue.size - 1) {
//                if(requestedTime in queue[i + 1].first..queue[i].first) {
//                    result = i
//                    break
//                }
//            }
//            if(result == -1) throw IllegalStateException("Internal error in history function")
            val newer = queue[result]
            val older = queue[result + 1]
            if(newer.first == requestedTime) newer.second
            else if(older.first == requestedTime) older.second
            else {
                val bias = (requestedTime - older.first)/(newer.first - older.first)
                interpolate(older.second, newer.second, bias)
            }
        }
    }
}

context(EventLoopContext)
infix fun DS.maintainHistory(timeSaved: Double): (Double) -> Double = this.maintainHistory(timeSaved, interpolate = MathUtil::interpolate)



context(EventLoopContext)
fun DS.makeContinuous(lower: Double, upper: Double, onStartValue: () -> Double? = { null }): DS {
    val maxJumpDistance = (upper - lower)/2
    val midpoint = lower + maxJumpDistance
    val inputStream = this
    var accumulation = 0.0
    var lastReading: Double? = null
    onStart {
        lastReading = null
        onStartValue()?.also { accumulation = it }
    }
    onPeriodic {
        val current = inputStream()
        if(lastReading == null) lastReading = current;
        val last = lastReading!!
        lastReading = current
        val diff = (current - last)
        accumulation += diff +
                if(diff.absoluteValue <= maxJumpDistance) 0.0
                else if(last < midpoint) (lower - upper)
                else (upper - lower)
    }
    return { accumulation }
}


