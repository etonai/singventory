package com.pseddev.singventory.ui.venues

enum class VenueFilter(val displayName: String) {
    ALL("All Venues"),
    NEVER_VISITED("Never Visited"),
    RECENTLY_VISITED("Recently Visited (Last 30 days)"),
    FREQUENTLY_VISITED("Frequently Visited (3+ visits)"),
    PRIVATE_ROOMS("Private Rooms Only"),
    HAS_COST_INFO("Has Cost Information"),
    NO_COST_INFO("No Cost Information")
}