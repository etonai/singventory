package com.pseddev.singventory.data.entity

/**
 * Helper class for dual key input methods - supports both dropdown selection 
 * and step-based adjustment input as required by design
 */
data class KeyInputHelper(
    val venueKey: MusicalKey?,
    val userPreferredKey: MusicalKey?,
    val stepAdjustment: Int = 0
) {
    
    /**
     * Calculate the resulting key from step adjustment
     */
    val calculatedKey: MusicalKey?
        get() = venueKey?.let { MusicalKey.transpose(it, stepAdjustment) }
    
    /**
     * Get the effective key adjustment between venue and user preference
     */
    val effectiveAdjustment: Int
        get() = if (venueKey != null && userPreferredKey != null) {
            MusicalKey.calculateKeyAdjustment(venueKey, userPreferredKey)
        } else {
            stepAdjustment
        }
    
    /**
     * Create display string showing both input methods
     */
    fun getDisplayString(): String {
        return when {
            venueKey != null && userPreferredKey != null -> {
                KeyUtils.formatKeyComparison(venueKey, userPreferredKey)
            }
            venueKey != null && stepAdjustment != 0 -> {
                val resultKey = calculatedKey
                "Venue: ${venueKey.displayName} → Adjusted: ${resultKey?.displayName} (${KeyUtils.formatKeyAdjustment(stepAdjustment).lowercase()})"
            }
            venueKey != null -> {
                "Venue key: ${venueKey.displayName}"
            }
            userPreferredKey != null -> {
                "Your preferred key: ${userPreferredKey.displayName}"
            }
            stepAdjustment != 0 -> {
                KeyUtils.formatKeyAdjustment(stepAdjustment)
            }
            else -> {
                "No key information"
            }
        }
    }
    
    companion object {
        /**
         * Create KeyInputHelper from dropdown selection (Method 1)
         * Auto-calculates adjustment between venue and user keys
         */
        fun fromDropdownSelection(venueKey: MusicalKey?, userKey: MusicalKey?): KeyInputHelper {
            return KeyInputHelper(
                venueKey = venueKey,
                userPreferredKey = userKey,
                stepAdjustment = if (venueKey != null && userKey != null) {
                    MusicalKey.calculateKeyAdjustment(venueKey, userKey)
                } else 0
            )
        }
        
        /**
         * Create KeyInputHelper from step adjustment (Method 2)
         * User specifies number of steps up/down from venue key
         */
        fun fromStepAdjustment(venueKey: MusicalKey?, steps: Int): KeyInputHelper {
            val calculatedUserKey = venueKey?.let { MusicalKey.transpose(it, steps) }
            return KeyInputHelper(
                venueKey = venueKey,
                userPreferredKey = calculatedUserKey,
                stepAdjustment = steps
            )
        }
        
        /**
         * Create KeyInputHelper for recon visit scenario
         * When testing songs to determine keys and preferences
         */
        fun forReconVisit(venueKey: MusicalKey? = null): KeyInputHelper {
            return KeyInputHelper(
                venueKey = venueKey,
                userPreferredKey = null,
                stepAdjustment = 0
            )
        }
        
        /**
         * Create KeyInputHelper for impromptu visit scenario
         * Quick entry with minimal information
         */
        fun forImpromptuVisit(quickAdjustment: Int = 0): KeyInputHelper {
            return KeyInputHelper(
                venueKey = null,
                userPreferredKey = null,
                stepAdjustment = quickAdjustment
            )
        }
    }
    
    /**
     * Validate that all key information is consistent and valid
     */
    fun isValid(): Boolean {
        // Check step adjustment is in valid range
        if (!KeyUtils.isValidAdjustment(stepAdjustment)) return false
        
        // If both venue and user keys are specified, verify consistency with step adjustment
        if (venueKey != null && userPreferredKey != null) {
            val calculatedAdjustment = MusicalKey.calculateKeyAdjustment(venueKey, userPreferredKey)
            // Allow small discrepancies due to different input methods
            val adjustmentDiff = kotlin.math.abs(calculatedAdjustment - stepAdjustment)
            if (adjustmentDiff > 1) return false
        }
        
        return true
    }
    
    /**
     * Convert to SongVenueInfo fields for database storage
     */
    fun toSongVenueInfoFields(): Triple<String?, Int, String?> {
        // Return (venueKey, keyAdjustment, notes about the key setup)
        val venueKeyString = venueKey?.displayName
        val adjustmentForStorage = effectiveAdjustment
        val keyNotes = if (venueKey != null && userPreferredKey != null && venueKey != userPreferredKey) {
            "Prefer ${userPreferredKey.displayName} (${KeyUtils.formatKeyAdjustment(adjustmentForStorage).lowercase()})"
        } else null
        
        return Triple(venueKeyString, adjustmentForStorage, keyNotes)
    }
}