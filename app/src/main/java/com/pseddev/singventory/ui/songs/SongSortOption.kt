package com.pseddev.singventory.ui.songs

enum class SongSortOption(val displayName: String) {
    TITLE("Title"),
    FAVORITES_AND_TITLE("Favorites & Title"),
    ARTIST("Artist"),
    PERFORMANCE_COUNT("Performance Count"),
    LAST_PERFORMANCE("Last Performance")
}