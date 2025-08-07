package com.pseddev.singventory.data.entity

enum class MusicalKey(val displayName: String, val semitonesFromC: Int) {
    C("C", 0),
    C_SHARP("C#/Db", 1),
    D("D", 2),
    D_SHARP("D#/Eb", 3),
    E("E", 4),
    F("F", 5),
    F_SHARP("F#/Gb", 6),
    G("G", 7),
    G_SHARP("G#/Ab", 8),
    A("A", 9),
    A_SHARP("A#/Bb", 10),
    B("B", 11);
    
    companion object {
        /**
         * Calculate the number of semitone steps between two keys
         * @param fromKey The starting key
         * @param toKey The target key
         * @return Number of steps up (positive) or down (negative) from fromKey to toKey
         */
        fun calculateKeyAdjustment(fromKey: MusicalKey, toKey: MusicalKey): Int {
            val steps = toKey.semitonesFromC - fromKey.semitonesFromC
            // Normalize to [-6, 6] range (prefer smaller adjustments)
            return when {
                steps > 6 -> steps - 12
                steps < -6 -> steps + 12
                else -> steps
            }
        }
        
        /**
         * Get a MusicalKey from its display name
         */
        fun fromDisplayName(displayName: String): MusicalKey? {
            return values().find { it.displayName == displayName }
        }
        
        /**
         * Transpose a key by a given number of semitones
         * @param key The starting key
         * @param semitones Number of semitones to transpose (can be negative)
         * @return The transposed key
         */
        fun transpose(key: MusicalKey, semitones: Int): MusicalKey {
            val newSemitones = (key.semitonesFromC + semitones + 12) % 12
            return values().find { it.semitonesFromC == newSemitones } ?: key
        }
    }
}