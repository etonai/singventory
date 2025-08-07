package com.pseddev.singventory.data.entity

/**
 * Utility class for musical key operations and validation
 */
object KeyUtils {
    
    /**
     * Validate and normalize a key string to a MusicalKey enum
     * Accepts various formats like "C", "C#", "Db", "F#/Gb", etc.
     */
    fun parseKey(keyString: String?): MusicalKey? {
        if (keyString.isNullOrBlank()) return null
        
        val normalized = keyString.trim().uppercase()
        
        return when (normalized) {
            "C" -> MusicalKey.C
            "C#", "DB", "C#/DB", "DB/C#" -> MusicalKey.C_SHARP
            "D" -> MusicalKey.D
            "D#", "EB", "D#/EB", "EB/D#" -> MusicalKey.D_SHARP
            "E" -> MusicalKey.E
            "F" -> MusicalKey.F
            "F#", "GB", "F#/GB", "GB/F#" -> MusicalKey.F_SHARP
            "G" -> MusicalKey.G
            "G#", "AB", "G#/AB", "AB/G#" -> MusicalKey.G_SHARP
            "A" -> MusicalKey.A
            "A#", "BB", "A#/BB", "BB/A#" -> MusicalKey.A_SHARP
            "B" -> MusicalKey.B
            else -> null
        }
    }
    
    /**
     * Format a key adjustment as a user-friendly string
     * @param adjustment Number of semitone steps (can be negative)
     * @return Formatted string like "Up 3 steps", "Down 2 steps", or "No adjustment"
     */
    fun formatKeyAdjustment(adjustment: Int): String {
        return when {
            adjustment == 0 -> "No adjustment"
            adjustment > 0 -> "Up $adjustment step${if (adjustment == 1) "" else "s"}"
            else -> "Down ${-adjustment} step${if (adjustment == -1) "" else "s"}"
        }
    }
    
    /**
     * Create a display string showing both keys and adjustment
     * @param venueKey The key the song is in at the venue
     * @param userKey The key the user prefers to sing in
     * @return String like "Venue: F# → You: D (down 4 steps)"
     */
    fun formatKeyComparison(venueKey: MusicalKey?, userKey: MusicalKey?): String {
        if (venueKey == null && userKey == null) {
            return "No key information"
        }
        
        if (venueKey == null) {
            return "Your key: ${userKey?.displayName}"
        }
        
        if (userKey == null) {
            return "Venue key: ${venueKey.displayName}"
        }
        
        if (venueKey == userKey) {
            return "Key: ${venueKey.displayName} (no adjustment needed)"
        }
        
        val adjustment = MusicalKey.calculateKeyAdjustment(venueKey, userKey)
        val adjustmentText = formatKeyAdjustment(adjustment).lowercase()
        
        return "Venue: ${venueKey.displayName} → You: ${userKey.displayName} ($adjustmentText)"
    }
    
    /**
     * Calculate the key that results from applying an adjustment
     * @param baseKey The starting key
     * @param adjustment Number of semitones to adjust (positive = up, negative = down)
     * @return The resulting key after adjustment
     */
    fun applyAdjustment(baseKey: MusicalKey, adjustment: Int): MusicalKey {
        return MusicalKey.transpose(baseKey, adjustment)
    }
    
    /**
     * Get all musical keys as a list for dropdown selection
     */
    fun getAllKeys(): List<MusicalKey> {
        return MusicalKey.values().toList()
    }
    
    /**
     * Get adjustment options for step-based input (-6 to +6)
     */
    fun getAdjustmentOptions(): List<Int> {
        return (-6..6).toList()
    }
    
    /**
     * Validate that a key adjustment is within reasonable bounds
     */
    fun isValidAdjustment(adjustment: Int): Boolean {
        return adjustment in -6..6
    }
    
    /**
     * Convert user-friendly adjustment text to numeric value
     * Parses strings like "Up 3 steps", "Down 2", "+4", "-1", etc.
     */
    fun parseAdjustment(adjustmentText: String?): Int? {
        if (adjustmentText.isNullOrBlank()) return null
        
        val normalized = adjustmentText.trim().lowercase()
        
        // Try direct numeric parsing first
        try {
            val value = normalized.toInt()
            return if (isValidAdjustment(value)) value else null
        } catch (e: NumberFormatException) {
            // Continue with text parsing
        }
        
        // Remove "steps", "step", "semitones", "semitone"
        val cleaned = normalized.replace(Regex("\\s*(steps?|semitones?)\\s*$"), "")
        
        // Parse patterns like "up 3", "down 2", "+4", "-5"
        val upMatch = Regex("up\\s+(\\d+)").find(cleaned)
        if (upMatch != null) {
            val value = upMatch.groupValues[1].toIntOrNull()
            return if (value != null && isValidAdjustment(value)) value else null
        }
        
        val downMatch = Regex("down\\s+(\\d+)").find(cleaned)
        if (downMatch != null) {
            val value = downMatch.groupValues[1].toIntOrNull()
            return if (value != null && isValidAdjustment(-value)) -value else null
        }
        
        // Try parsing with + or - prefix
        if (cleaned.startsWith("+") || cleaned.startsWith("-")) {
            try {
                val value = cleaned.toInt()
                return if (isValidAdjustment(value)) value else null
            } catch (e: NumberFormatException) {
                return null
            }
        }
        
        return null
    }
}