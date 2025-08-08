package com.pseddev.singventory.ui.songs

enum class PerformanceFilter(val displayName: String) {
    ALL("All Songs"),
    NEVER_PERFORMED("Never Performed"),
    RARELY_PERFORMED("Rarely Performed (1-4 times)"),
    FREQUENTLY_PERFORMED("Frequently Performed (5+ times)"),
    RECENTLY_PERFORMED("Recently Performed (Last 30 days)")
}