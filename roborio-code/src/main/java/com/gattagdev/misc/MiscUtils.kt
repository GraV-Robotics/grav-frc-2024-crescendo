package com.gattagdev.misc

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.util.WPILibVersion


fun reportKotlinUsage(){
    HAL.report(FRCNetComm.tResourceType.kResourceType_Language, FRCNetComm.tInstances.kLanguage_Kotlin, 0, WPILibVersion.Version)
}

val TELEOPERATED = DriverStation::isTeleopEnabled
val AUTONOMOUS   = DriverStation::isAutonomousEnabled
val always = { true }

val words = listOf(
    "about", "alert", "argue", "beach", "abuse", "alive", "array", "begin",
    "acute", "alone", "asset", "adult", "among", "avoid", "after", "anger",
    "award", "agent", "angry", "blame", "agree", "apart", "baker", "blind",
    "ahead", "apple", "block", "alarm", "basic", "blood", "album", "arena",
    "board", "boost", "china", "cover", "cable", "chose", "craft", "bound",
    "civil", "crash", "brand", "catch", "class", "crime", "bread", "cause",
    "clean", "cross", "bring", "chase", "close", "cheap", "coach", "broke",
    "check", "coast", "dance", "brown", "chest", "dated", "built", "child",
    "court", "doubt", "every", "frame", "guest", "drama", "extra", "fresh",
    "drawn", "front", "heart", "dream", "fruit", "heavy", "dress", "fault",
    "hence", "drill", "funny", "night", "drink", "field", "giant", "horse",
    "drive", "given", "hotel", "drove", "glass", "house", "eager", "final",
    "going", "ideal", "fixed", "grade", "index", "enjoy", "green", "juice",
    "judge", "metal", "media", "known", "local", "might", "noise", "label",
    "logic", "minor", "large", "loose", "noted", "laser", "lower", "mixed",
    "novel", "later", "lucky", "model", "nurse", "learn", "magic", "offer",
    "lease", "major", "often", "least", "maker", "order", "level", "match",
    "movie", "paint", "light", "mayor", "paper", "limit", "meant", "never",
    "party", "peace", "radio", "round", "phone", "pride", "rapid", "piece",
    "print", "reach", "pilot", "prior", "ready", "scope", "pitch", "prize",
    "refer", "score", "place", "right", "sense", "plain", "proud", "serve",
    "plant", "queen", "quick", "plate", "roman", "share", "point", "rough",
    "sharp", "pound", "stand", "shape", "power", "speak", "sugar", "tired",
    "shelf", "spend", "super", "today", "shell", "sweet", "topic", "shift",
    "split", "table", "total", "shock", "sport", "taste", "shoot", "staff",
    "tower", "sight", "start", "treat", "since", "state", "trend", "sleep",
    "still", "truck", "small", "stone", "trust", "solve", "story", "undue",
    "sound", "stuck", "unity", "space", "stuff", "tight", "upper", "upset",
    "whole", "waste", "wound", "urban", "whose", "watch", "write", "usage",
    "woman", "water", "wrong", "usual", "train", "wheel", "wrote", "valid",
    "world", "where", "yield", "value", "worry", "which", "young", "video",
    "worse", "while", "virus", "worst", "white", "visit", "vital", "voice"
)

fun generateIdentifier(seed: Int): String {
    return ByteArray(4) { ((seed shr (it * 8)) and 0xFF).toByte() }
        .let { bytes -> List(4) { words[bytes[it].toInt() and 0xFF % words.size] } }
        .joinToString("-")
}

class ReversiblePRNG(private val seed: Long) {
    private var state = seed
    private val multiplier = 6364136223846793005L
    private val increment = 1442695040888963407L

    fun next(): Long {
        state = state * multiplier + increment
        return state
    }

    fun prev(): Long {
        state = (state - increment) * multiplier.inv() % Long.MAX_VALUE
        return state
    }
}