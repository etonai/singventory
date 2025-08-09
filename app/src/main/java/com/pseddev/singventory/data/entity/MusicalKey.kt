package com.pseddev.singventory.data.entity

enum class MusicalKey(val displayName: String, val semitonesFromC: Int, val isMajor: Boolean = true) {
    // Major keys
    C("C", 0, true),
    C_SHARP("C#/Db", 1, true),
    D("D", 2, true),
    D_SHARP("D#/Eb", 3, true),
    E("E", 4, true),
    F("F", 5, true),
    F_SHARP("F#/Gb", 6, true),
    G("G", 7, true),
    G_SHARP("G#/Ab", 8, true),
    A("A", 9, true),
    A_SHARP("A#/Bb", 10, true),
    B("B", 11, true),
    
    // Minor keys (same semitone relationships but marked as minor)
    A_MINOR("Am", 9, false),
    A_SHARP_MINOR("A#m/Bbm", 10, false),
    B_MINOR("Bm", 11, false),
    C_MINOR("Cm", 0, false),
    C_SHARP_MINOR("C#m/Dbm", 1, false),
    D_MINOR("Dm", 2, false),
    D_SHARP_MINOR("D#m/Ebm", 3, false),
    E_MINOR("Em", 4, false),
    F_MINOR("Fm", 5, false),
    F_SHARP_MINOR("F#m/Gbm", 6, false),
    G_MINOR("Gm", 7, false),
    G_SHARP_MINOR("G#m/Abm", 8, false);
    
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
        
        /**
         * Get all major keys in chromatic order
         */
        fun getMajorKeys(): List<MusicalKey> {
            return values().filter { it.isMajor }
        }
        
        /**
         * Get all minor keys in chromatic order (starting with Am)
         */
        fun getMinorKeys(): List<MusicalKey> {
            return values().filter { !it.isMajor }
        }
        
        /**
         * Get all keys organized as major keys first, then minor keys
         */
        fun getAllKeysOrganized(): List<MusicalKey> {
            return getMajorKeys() + getMinorKeys()
        }
    }
}