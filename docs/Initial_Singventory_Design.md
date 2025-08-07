Note to PlayStreak:  This is the initial design of a new app, Singventory. I copied this here so that Claude could reference all of PlayStreak and then comment on how Singventory can be designed following PlayStreak's paterns.

# Initial Singventory Design

## Ed's initial notes:
August 6, 2025 9:38PM PDT

Singventory is an android app designed to keep track of the songs I, the primary user, likes to sing at karaoke venues. Sometimes I sing in places other than karaoke venues.

Some of this design is based on what I learned in the PlayStreak project.
C:\dev\agentEval\pianotrack-opus

### What should it do?

It will track songs at different karaoke venues. The name of the song and artis may be the same, but various elements of the karaoke recording may be different at each venue.

For each song:
- The name of the song
- Where the song is from
- Artist of the song (there may be multiple versions)
- List of venues where the song is available
- My preferred key for the song
- Key for my reference recording of the song
- Optional lyrics
- Number of times I've performed the song
- Last time I performed the song

For each location where the song is available
- Song Code #
- Key of song. It may be different than what I consider to be the reference key of the song.
- How many steps up or down I like to adjust the key
- Number of times I've performed the song in that location
- Last time I performed the song at that location

For each venue:
- Venue name
- Venue address
- Cost
- Private rooms or public area or both
- Operating Hours
- track each time I've visited
- Other notes
- Number of times I've visited the venue
- Last time I visited the venue
- Songs available at that venue

For each visit:
- Date and time of arrival
- List of songs I performed
- Notes so I can keep track of how long I stayed, if I was there with other people, food/drinks I purchased
- How much I spent

For each performance of a single song
- my ID for the song
- the name of the song
- Key adjustment for the song
- Any notes I have on the song


Import/Export (button available on the settings screen) - Similar to PlayStreak feature
- Import or Export to JSON files

Purge Data (button available on the settings screen) - Similar to PLayStreak feature
- Remove the 5 oldest visits on the app
- When removing old visit data, keep the stats that are associated with the song (e.g. number of times I've performed the song and last time I performed it) or vanue (e.g. number of times I've visited the venue and last time I visited it)
- The limit should be 500 visits. The app should not let users create a new visit without purging old visits first
- The Purge Data screen should have a button to go to the Import/Export screen, and should recommend exporting data before purging

Configuration (button available on the settings screen) - Similar to PlayStreak feature
- Enable/Disable Purge Data button (this is just for added security against accidentally purging data)
- Other buttons will be added

The Settings Screen - Similar to PlayStreak feature
- Should have the list of how many songs, venues, visits, and performances are actively stored in the system. This data will go down if data is purged.


Future Implementation:
- Banner ads for free users
- Pro users can pay $2 to become pro users, and use the app ad-free


### Let's imagine a sample impromptu visit

I'm out with some friends, and we go to a karaoke venue called Ozzie's.

- I create a new visit in the app, it asks me for the venue and I select new venue. It asks me for venue information and I only enter in the name, Ozzie's.  (I plan on entering more later)
- As I look through Ozzie's song list, I might spot a song that is already in my Singventory (my inventory of songs I like to sing). I go to my song list, pick out that song's page, and select the add venue icon. Let's say the song is On My Own from Les Miserables.
  - A page comes up that lets me enter in the venue's song id for that song, but I leave information about the key or key adjustment blank.
- I sing the song
- I may enter in a note to the song, like too high or too low, or just right
- I repeat for more songs

- If I go back to Ozzie's later, I know which songs are good for me, and which I should avoid or try to adjust.

### Let's imagine a recon visit

I decide to go to RockBox alone so that I can investigate what songs I might want to sing on a future visit

- Before my visit, I enter in RockBox as a new venue. I fill out the information about RockBox
- Before my visit, I add some candidates for songs I'd like to sing
- Before my visit, I see RockBox's song list is online, so I add the RockBox song IDs to my song candidates
- I go to RockBox. I create a new visit in the app.
- I should be able to go through my app to see which songs are available at RockBox, and which songs aren't filled out enough for me to sing
- I pick a song
- Outside of the app, I record a portion of the song so I can later figure out what key it is.
- I may make notes about the song in the app
- I repeat until I'm done.

- When I get home, I go through the songs and the recordings to put accurate information about what key they are in, and maybe what key I'm most comfortable singing the song in

### Let's imagine a 2nd visit to RockBox

I go to RockBox
- In the app, I create a new visit and select RockBox as the venue
- I see the list of songs I've already researched are available at RockBox. Some of the songs may have key information, some may not. Some of the songs may have stars, indicating that they are favorites.
- I pick a song that I have researched
- I see the RockBox ID# of the song, along with the key adjustment (it will say adjust by +# or -# steps).
- I enter in the song
- I sing it.
- I note in the app that I performed the song, and may add notes
- When I am done singing, I close the visit, and may note information there (e.g. cost, other people there)

### Scenario - singing with a random musician

Let's assume I've already entered in the lyrics to a song

I see a musician on the street.
- I look up a song on my app, let's say Autumn Leaves.
- The app has information about Autumn Leaves, that I like singing it in Eb.
- I ask the musician to play Autumn Leaves in Eb.  I have the lyrics
- Musician plays the song. I sing.
- I add a visit in the app, with the venue being Street Musician or something. I add the song performance.


## ChatGPT Notes

Your design notes for **Singventory** are thoughtful, structured, and rooted in practical use cases. Here's a critique with strengths and suggestions for improvement:

---

### ✅ Strengths

#### 1. **User-centered perspective**

* The scenarios (impromptu visit, recon visit, etc.) clearly demonstrate real-world usage patterns, which is *excellent* for guiding both development and UI/UX decisions.
* You’ve anticipated different user contexts (e.g. impromptu singing, data collection visits, singing with a musician), which adds robustness to your concept.

#### 2. **Data Modeling is well thought out**

* You’ve separated **song**, **venue**, **visit**, and **performance** cleanly.
* Handling venue-specific song data (e.g. different keys, song IDs) is realistic and often overlooked in early app planning.

#### 3. **Practical features**

* **Purge logic** with stats preservation is clever and resource-aware.
* **JSON Import/Export** and **configurable settings** are solid carryovers from PlayStreak and will help with longevity and data safety.

#### 4. **Scalability and Monetization**

* Including a free/pro tier with ads and a \$2 upgrade path is simple and achievable.
* The 500-visit cap (with purging) helps control database bloat in early versions.

---

### 🛠️ Suggestions & Refinements

#### 🔹 Clarify data relationships

Some terminology and structure could use more definition to support coding and database work later. For example:

* Is a "performance" always tied to a "visit"?  EDNOTE: Yes
* Can a "performance" exist outside of a visit (e.g., solo practice, street musician)? EDNOTE: No, I will consider singing with a street musician a visit.
* Are lyrics tied to the global song, or can they differ per venue? EDNOTE: They can differ per venue, but will default to be tied to the global song
* Do you plan to normalize the “artist” into its own entity? EDNOTE: No

You could sketch an **Entity-Relationship Diagram (ERD)** to clarify this before building.

---

#### 🔹 Improve naming consistency and fix typos

* `vanue` → `venue`
* `artis` → `artist`
* “PLayStreak” typo
* You use both “Song Code #” and “venue’s song id” — consider standardizing as **Venue Song ID** EDNOTE: I like this idea

---

#### 🔹 Add sorting/filtering/search capabilities (even if not in v1)

While not critical for MVP, it will eventually be very useful to:

* Sort or filter songs by **key**, **last performed**, **venue**, or **favorite** EDNOTE: I like sorting by Last performed and favorite
* Search by song name or artist EDNOTE: Please add this as Future Implementation

You might note these as “Future Implementation” even if you defer them.

---

#### 🔹 Add guidance for the “notes” fields

You have several open-ended “notes” fields. That’s great, but you may want to:

* Set character limits EDNOTE: 30 characters for a song title, 256 characters for notes
* Support formatting or categories (e.g., “Key too high” vs “Mic was bad”) EDNOTE: Future implementation
* Consider if any of these should be structured later

---

#### 🔹 Metrics and Progress Tracking

You’ve tracked number of times sung per venue/song — excellent. Later, consider metrics such as:

* Top 10 most-performed songs EDNOTE: I like this
* Timeline view of song performances EDNOTE: Maybe
* Frequency heatmap of venue visits (if helpful) EDNOTE: Future implementation

---

#### 🔹 User Experience Considerations

* **Adding a new visit**: Should the app auto-fill the current date/time and let the user edit it? Or is it always "log now"? EDNOTE: Auto-fill and let the user edit it
* **Performance entry**: Can users mark a performance as a *test*, *rehearsal*, or *actual* performance? EDNOTE: Probably. I will consider this
* **Song key handling**: Will the app support previewing or playing a reference note? EDNOTE: I'd like to, but the technical difficulty may be too high.

---

#### 🔹 Backup and Sync (Future)

* Would you want optional **Google Drive sync**, like in PlayStreak? EDNOTE: No
* Add to “Future Implementation” section: backup strategy (or note “manual only” for now) EDNOTE: No

---

### 📋 Summary

Your notes reflect a well-scoped, thoughtful project rooted in real user needs. If you clean up terminology, define relationships slightly more formally, and clarify a few data edge cases, you’ll have an extremely strong foundation for development.

Would you like help turning this into:

* a structured spec document?
* an initial data model / database schema?
* a Kotlin/Jetpack Compose scaffold like you did with PlayStreak?

Let me know how you'd like to move forward.

## Claude in PlayStreak notes (August 5, 2025, 10:47PM)

### 🏗️ **Architecture Recommendations Based on PlayStreak**

**Core Architecture Pattern:**
Follow PlayStreak's proven MVVM + Repository pattern:
- **Entities** (`@Entity` Room database classes)
- **DAOs** (`@Dao` interfaces for database operations)
- **Repository** (single source of truth, combines local + remote data)
- **ViewModels** (UI logic and LiveData/Flow management)
- **Fragments** with ViewBinding (not DataBinding - simpler and more reliable)

**Key PlayStreak Files to Reference:**
- `PianoRepository.kt` - Shows repository pattern with complex relationships
- `AddActivityViewModel.kt` - Shows proper ViewModel lifecycle management
- `MainActivity.kt` + Navigation - Bottom navigation with fragment management

### 📊 **Data Model Architecture (Based on PlayStreak Experience)**

**Entity Relationships (Mirror PlayStreak's approach):**
```kotlin
// Similar to PlayStreak's PieceOrTechnique
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,        // Normalized with TextNormalizer
    val artist: String,      // Store as string (don't over-normalize)
    val referenceKey: String,
    val preferredKey: String, 
    val lyrics: String? = null,
    val totalPerformances: Int = 0,  // Calculated field
    val lastPerformed: Long? = null  // Timestamp
)

// New entity - no direct PlayStreak equivalent
@Entity(tableName = "venues")
data class Venue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,        // Normalized
    val address: String? = null,
    val cost: String? = null,
    val roomType: String? = null, // "private", "public", "both"
    val hours: String? = null,
    val notes: String? = null,
    val totalVisits: Int = 0,     // Calculated field
    val lastVisited: Long? = null  // Timestamp
)

// Similar to PlayStreak's Activity
@Entity(tableName = "visits")
data class Visit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val venueId: Long,       // Foreign key
    val timestamp: Long,     // Start time
    val notes: String? = null,
    val amountSpent: Double? = null
)

// Junction table - similar to PlayStreak's activity tracking
@Entity(
    tableName = "song_venue_info",
    foreignKeys = [
        ForeignKey(entity = Song::class, parentColumns = ["id"], childColumns = ["songId"]),
        ForeignKey(entity = Venue::class, parentColumns = ["id"], childColumns = ["venueId"])
    ],
    indices = [Index("songId"), Index("venueId")]
)
data class SongVenueInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val venueId: Long,
    val venuesSongId: String? = null,  // The venue's internal song code
    val venueKey: String? = null,      // Key at this venue
    val keyAdjustment: Int = 0,        // Steps up/down from venue key
    val lyrics: String? = null,        // Venue-specific lyrics override
    val performanceCount: Int = 0,     // At this venue
    val lastPerformed: Long? = null    // At this venue
)

// Similar to PlayStreak's Activity but more specific
@Entity(
    tableName = "performances",
    foreignKeys = [
        ForeignKey(entity = Visit::class, parentColumns = ["id"], childColumns = ["visitId"]),
        ForeignKey(entity = Song::class, parentColumns = ["id"], childColumns = ["songId"])
    ]
)
data class Performance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val visitId: Long,       // Must belong to a visit
    val songId: Long,
    val keyAdjustment: Int = 0,
    val notes: String? = null,
    val timestamp: Long      // When during the visit
)
```

### 🛠️ **Import/Export Implementation (Direct from PlayStreak)**

**Leverage PlayStreak's JSON Import/Export System:**
- Copy `JsonExporter.kt` and `JsonImporter.kt` patterns exactly
- Copy `ImportExportViewModel.kt` structure - it handles all edge cases
- Copy `ImportExportFragment.kt` UI patterns - proven user experience
- Use PlayStreak's `JsonValidationResult` pattern for pre-import validation

**Key PlayStreak Import/Export Features to Copy:**
- **Atomic transactions** - all-or-nothing imports
- **Validation before import** - check limits and format
- **Progress indicators** with `_isLoading` LiveData
- **Error handling** with detailed user messages
- **File picker integration** with proper URI handling

**Singventory-Specific Adaptations:**
```kotlin
// Mirror PlayStreak's JsonExporter structure
class SingventoryJsonExporter(private val repository: SingventoryRepository) {
    suspend fun exportToJson(writer: Writer) {
        val exportData = SingventoryExportData(
            songs = repository.getAllSongs(),
            venues = repository.getAllVenues(),
            visits = repository.getAllVisits(),
            performances = repository.getAllPerformances(),
            songVenueInfo = repository.getAllSongVenueInfo(),
            exportVersion = "1.0",
            exportDate = System.currentTimeMillis()
        )
        // Use PlayStreak's Gson serialization approach
    }
}
```

### 🗑️ **Purge Data Implementation (Enhanced from PlayStreak)**

**PlayStreak's Purge System as Base:**
- Copy `PruneDataViewModel.kt` structure and UI patterns
- Copy `PruneDataFragment.kt` - the confirmation dialogs work well
- Use PlayStreak's `PruningResult` sealed class pattern

**Singventory Enhancements Over PlayStreak:**
```kotlin
// Enhanced from PlayStreak's simple activity deletion
sealed class SingventoryPruningResult {
    data class Success(
        val deletedVisits: Int,
        val deletedPerformances: Int,
        val updatedSongs: Int,      // Songs with updated stats
        val updatedVenues: Int      // Venues with updated stats
    ) : SingventoryPruningResult()
    
    object NoVisitsToPrune : SingventoryPruningResult()
    data class Error(val message: String) : SingventoryPruningResult()
}

// More complex than PlayStreak because we preserve aggregated stats
suspend fun pruneOldestVisits(count: Int = 5): SingventoryPruningResult {
    // 1. Get oldest visits
    // 2. Delete performances for these visits
    // 3. Delete visits  
    // 4. Recalculate song.totalPerformances and venue.totalVisits
    // 5. Update lastPerformed/lastVisited dates
    // This is more complex than PlayStreak's simple activity deletion
}
```

### 🏗️ **Settings Screen Architecture (Copy PlayStreak Pattern)**

**Mirror PlayStreak's Settings Structure:**
- Copy `SettingsFragment.kt` layout and navigation patterns
- Use PlayStreak's data counter display approach
- Copy the CardView + button layout - it's clean and accessible

**Settings Display for Singventory:**
```kotlin
// Mirror PlayStreak's activity/piece counter approach
private fun updateDataCounts() {
    binding.songsCount.text = "Songs: ${songCount}"
    binding.venuesCount.text = "Venues: ${venueCount}"  
    binding.visitsCount.text = "Visits: ${visitCount}"
    binding.performancesCount.text = "Performances: ${performanceCount}"
    // Show current usage vs limits (500 visits)
}
```

### 📱 **UI/UX Patterns from PlayStreak**

**Navigation Structure:**
- **Copy PlayStreak's bottom navigation** - it scales well to 4-5 main sections
- Main tabs: Songs, Venues, Visits, Analytics(?), Settings
- Use PlayStreak's fragment management and deep linking patterns

**List Management Patterns:**
- **Copy PlayStreak's RecyclerView + ViewHolder patterns** from `PiecesFragment`
- **Copy the floating action button approach** for adding new items
- **Copy PlayStreak's search and filter UI** - users expect consistent patterns

**Form Input Patterns:**
- **Copy AddActivityFragment's form validation** and error display
- **Copy the date/time picker integrations** - they handle edge cases well
- **Copy PlayStreak's dropdown/spinner patterns** for venue selection

### 🎯 **Key Management (Unique to Singventory)**

**Musical Key Handling (New requirement not in PlayStreak):**
```kotlin
// Singventory needs key management - PlayStreak doesn't have this
enum class MusicalKey(val displayName: String, val semitonesFromC: Int) {
    C("C", 0), C_SHARP("C#/Db", 1), D("D", 2), 
    // ... complete chromatic scale
    
    companion object {
        fun calculateKeyAdjustment(fromKey: MusicalKey, toKey: MusicalKey): Int {
            return (toKey.semitonesFromC - fromKey.semitonesFromC + 12) % 12
        }
    }
}

// Key adjustment UI component - new for Singventory
class KeyAdjustmentView : View {
    // Custom UI component for +/- key adjustment
    // Could show: "Original: F# → Your Key: D (down 4 steps)"
}
```

### 📊 **Analytics Integration (Learn from PlayStreak's Implementation)**

**Copy PlayStreak's Analytics Architecture:**
- **Use PlayStreak's `AnalyticsManager.kt` as template** - it handles privacy and Firebase integration correctly
- **Copy the event parameter structure** - consistent naming and data types
- **Copy the debug/release configuration** - prevents test data pollution

**Singventory-Specific Analytics Events:**
```kotlin
// Based on PlayStreak's analytics event structure
fun trackSongPerformed(songId: Long, venueId: Long, keyAdjustment: Int) {
    firebaseAnalytics.logEvent("song_performed") {
        param("song_type", if (keyAdjustment == 0) "original_key" else "adjusted_key")
        param("key_adjustment_steps", keyAdjustment.toLong())
        param("venue_type", getVenueType(venueId)) // "karaoke_bar", "private_room", etc.
    }
}

// Track visit patterns - similar to PlayStreak's activity tracking
fun trackVenueVisit(venueId: Long, songsPerformed: Int, duration: Long?) {
    // Similar to PlayStreak's activity_logged event
}
```

### 🔧 **Configuration System (Enhanced from PlayStreak)**

**Base on PlayStreak's ConfigurationManager:**
- Copy PlayStreak's `ConfigurationManager.kt` singleton pattern
- Copy the SharedPreferences integration and type-safe accessors
- Copy the initialization and migration patterns

**Singventory-Specific Configurations:**
```kotlin
class SingventoryConfigurationManager private constructor(context: Context) {
    // Copy PlayStreak's singleton pattern
    
    // New settings specific to Singventory
    fun isPurgeDataEnabled(): Boolean = prefs.getBoolean("purge_data_enabled", true)
    fun getVisitLimit(): Int = prefs.getInt("visit_limit", 500)
    fun getDefaultKeyDisplayFormat(): String = prefs.getString("key_display_format", "sharps") ?: "sharps"
    
    // Copy PlayStreak's migration pattern for settings updates
    fun migrateSettings(currentVersion: Int) {
        // Handle settings schema changes
    }
}
```

### 🚨 **Critical Architecture Decisions Based on PlayStreak Learning**

1. **Use Room Database** - PlayStreak's SQLite + Room approach handles complex relationships well
2. **Single Repository Pattern** - Don't create separate repositories per entity; PlayStreak's unified approach works better
3. **ViewBinding over DataBinding** - PlayStreak switched to ViewBinding for better performance and debugging
4. **Kotlin Coroutines + LiveData** - PlayStreak's async pattern is proven and testable
5. **Navigation Component** - PlayStreak's navigation setup handles deep linking and state management correctly

### 🎨 **Visual Design Consistency**

**Copy PlayStreak's Visual Elements:**
- **Color scheme and Material Design 3** - already tested for accessibility
- **Card-based layouts** - clean, modern, and touch-friendly
- **Typography and spacing** - consistent with Android design guidelines
- **Icon choices** - PlayStreak uses Material Icons consistently

### 📋 **Development Phase Recommendations**

**Phase 1 (MVP)** - Copy PlayStreak's core architecture:
1. Database entities and relationships
2. Basic CRUD operations for songs, venues, visits
3. Simple performance tracking
4. Settings screen with data counts

**Phase 2** - Import/Export (direct copy from PlayStreak):
1. Copy JSON import/export system exactly
2. Copy purge data implementation with Singventory-specific stats preservation
3. Copy configuration management

**Phase 3** - Enhanced Features:
1. Key management and adjustment features
2. Analytics integration (copy PlayStreak's Firebase setup)
3. Search and filtering (enhance PlayStreak's basic patterns)

**Phase 4** - Monetization (copy PlayStreak's planned approach):
1. Pro user management system
2. Ad integration for free users
3. Feature gating

### 🔍 **Testing Strategy (Learn from PlayStreak)**

**Copy PlayStreak's Testing Approach:**
- **Manual testing cycles** with real-world scenarios (like PlayStreak's DevCycles)
- **Database migration testing** - critical for user data preservation
- **Import/export testing** with various data sizes and edge cases
- **Performance testing** with large datasets (500 visits vs PlayStreak's activity limits)

**Singventory-Specific Test Cases:**
- Key adjustment calculations and display
- Complex relationship updates (venue stats after visit deletion)
- Multi-venue song availability tracking

This architecture leverages PlayStreak's proven patterns while addressing Singventry's unique requirements for venue management, key tracking, and more complex data relationships. The import/export and purge data features can be almost directly copied from PlayStreak with minimal modifications.

## Claude in Singventory Notes (August 6, 2025 10:57PM)

### 🎯 **Design Phase Priorities for Android Development**

Based on your excellent foundation and the PlayStreak architecture analysis, here are my recommendations for Phase 1 - Design Singventory:

**Primary Design Focus:**
1. **User Flow Design** - Map out the core user journeys (impromptu visit, recon visit, repeat visit)
2. **Screen Hierarchy** - Define main navigation and screen relationships
3. **Data Entry UX** - Design forms that work well on mobile (especially song/venue creation)
4. **Key Management Interface** - This is unique to Singventory and needs careful UX consideration

### 📱 **Mobile-First Design Considerations**

**Screen Size & Touch Targets:**
- Song lists will need efficient scrolling with good search/filter options
- Key adjustment controls need large, precise touch targets (+ / - buttons)
- Quick-add performance during visits (minimal taps while at venue)

**Offline-First Design:**
- Assume users will be in venues with poor connectivity
- All core features (adding performances, viewing song lists) must work offline
- Sync/backup features are secondary to local functionality

### 🎵 **Musical Key Interface Design Challenges**

**Key Display Standards:**
- Decide on sharp vs flat display preference (C# vs Db)
- Consider if you'll show both original key AND your preferred key prominently
- Key adjustment UI should show clear direction: "Sing 3 steps DOWN from venue key"

**Key Input Methods:**
- Dropdown picker vs custom wheel vs +/- adjustment buttons
- Consider musicians think in "steps up/down" more than absolute keys
- Need clear visual indication of adjustment direction

### 🏗️ **Recommended Screen Architecture**

**Bottom Navigation (4-5 tabs max for thumb reach):**
1. **Songs** - Your song inventory with search/filter
2. **Venues** - Venue list and details  
3. **Visits** - Current/recent visit logging
4. **Stats** - Performance analytics (later phase)
5. **Settings** - Config, import/export, purge

**Critical User Flows to Design First:**
1. **Quick Performance Logging** - Must be fast during live visits
2. **Song-Venue Association** - Adding venue availability to existing songs
3. **New Visit Creation** - Venue selection with auto-complete
4. **Key Adjustment Workflow** - From discovering a song to setting preferences

### 🔄 **State Management for Complex Relationships**

**Design for Data Consistency:**
- When a performance is logged, update both song stats AND venue stats
- When visits are purged, preserve calculated statistics
- Handle partial data entry (venue added but song details incomplete)

**Real-time Updates:**
- Performance counts should update immediately in UI
- Last performed dates should be accurate across all views
- Venue song lists should reflect newly added songs instantly

### 🎨 **Visual Design System Recommendations**

**Adopt Material Design 3 with Music-Themed Adaptations:**
- Use PlayStreak's proven color scheme as base
- Add music-specific iconography (treble clef, sharp/flat symbols)
- Card-based layout works well for song/venue information display

**Information Hierarchy:**
- Song name and artist most prominent
- Venue-specific info (key, venue ID) secondary but easily scannable
- Performance history and notes tertiary

### 🚀 **Development Strategy Alignment**

**Design for Progressive Enhancement:**
- Core features (logging performances) should work in v1.0.0
- Advanced features (analytics, key reference audio) can be added later
- Import/export from day one (copy PlayStreak's approach)

**Database Design Implications:**
- Your entity relationships are solid, design UI that makes these relationships clear
- Consider how to handle "partial" data (song exists but no venue info yet)
- Design for the 500-visit limit - how to communicate storage usage to users

### 🤔 **Questions for Design Clarification**

**User Interface Questions:**
1. **Song Discovery Workflow** - When at a venue looking through their catalog, do you want to search your existing songs first, or browse the venue's list first?
EDNOTE: My existing songs first

2. **Key Adjustment Display** - Would you prefer to see "Venue plays in F#, you sing in D" or "Adjust down 4 steps" or both?
EDNOTE: Both

3. **Performance Logging Speed** - How many seconds maximum should it take to log a performance during a live visit?
EDNOTE: The limit should be pretty long, as long as the next person's song.

4. **Venue Information Priority** - What venue details are most important to see at-a-glance vs in detailed view?
EDNOTE: Just the venue name and address

**Data Management Questions:**
1. **Duplicate Song Handling** - If you accidentally create the same song twice, should the app detect and offer to merge?
EDNOTE: Yes, but we should not enforce merging

2. **Historical Data** - When visits are purged, do you want any way to see "you've performed this song X times historically" vs just current data?
EDNOTE: When visits are purged the record of the number of times a song has been performed should not change. That information should be stored with the song.

3. **Song Variants** - Some songs have multiple versions (original vs karaoke vs acoustic) - treat as separate songs or variants of one song?
EDNOTE: Separate songs

**Technical Implementation Questions:**
1. **Reference Audio** - You mentioned wanting key reference playback but noted technical difficulty. Should we design the UI with space for this feature to be added later?
EDNOTE: No

2. **Backup Integration** - Should we design for manual JSON export only, or leave room for cloud sync features in the future?
EDNOTE: JSON export only

These design decisions will directly impact the development architecture and should be resolved before moving to implementation phases.

### 🔄 **Follow-up Questions Based on Your Responses**

**Song Management Questions:**
1. **Duplicate Detection Scope** - Should duplicate detection look at just song name + artist, or also consider venue availability? (e.g., if "Bohemian Rhapsody by Queen" exists but only has RockBox data, and you add it again for Ozzie's, should that trigger duplicate detection?)
EDNOTE: It should trigger duplicate detection

2. **Song Search Priority** - When searching your existing songs at a venue, should the search results prioritize songs you've never performed at that venue, or songs you perform frequently?
EDNOTE: Songs I perform frequently

**Performance Statistics Clarification:**
3. **Stats Preservation Logic** - You want song performance counts preserved during purging. Should this also apply to venue visit counts? (e.g., if 5 oldest visits are purged but 3 were to RockBox, does RockBox still show the total visit count including those purged visits?)
EDNOTE: Yes

4. **"Last Performed" Dates** - When visits are purged, should "last performed" dates update to the most recent non-purged performance, or stay frozen at the actual last performance even if that data is purged?
EDNOTE: Stay frozen at the actual last performance date. Purging data is for the purpose of removing data if we have too much data stored, even if we don't want to get rid of the data. We should keep the essential information.

**User Interface Flow Questions:**
5. **Quick Performance Entry** - During a live visit, after you perform a song, do you want to immediately add notes about the performance, or should that be optional/later? 
EDNOTE: It is optional to add immediately, I can add it later

6. **Key Adjustment Input** - For the key display showing both formats ("Venue: F#, You: D" + "Adjust down 4 steps"), should the user input be via key selection dropdowns, or +/- step adjustments, or both methods available?
EDNOTE: Both options should be available. Some venues allow one, and some allow the other

7. **Visit State Management** - If you start logging a visit but don't formally "end" it (app gets closed, phone dies, etc.), should the app auto-save the partial visit data or discard it?
EDNOTE: A visit should be saved after entering each performance. If the phone dies in the middle of entering performance, that data is gone.

### 🎯 **Final Technical Clarifications**

**Data Persistence Questions:**
1. **Essential Data for Purging** - You want to preserve "essential information" when purging. Should this include: total performance counts, last performed dates, venue visit counts, last visited dates? And should venue-specific song information (keys, venue song IDs) be preserved even if all performances at that venue are purged?
EDNOTE: Yes to all

2. **Duplicate Song Merging** - When duplicate detection triggers, should the merge combine all venue-specific data (song IDs, key adjustments, performance counts) from both song entries, or should the user manually choose which data to keep?
EDNOTE: User manually chooses


**Performance Entry Workflow:**
3. **Key Adjustment Discovery** - During a recon visit when you're testing songs, should the app auto-calculate key adjustments based on your input (e.g., "venue plays in F#, I prefer D") or should you manually enter the step count?
EDNOTE: Auto-calculate key adjustments

4. **Visit Completion Indicators** - Since visits auto-save after each performance, how should the app distinguish between an "active/ongoing visit" vs a "completed visit"? Should there be an explicit "End Visit" action, or does a visit automatically close after X hours of inactivity?
EDNOTE: There should be an explicit end visit action, but the user should be able to reopen a visit to add more performances.


These final answers will help define the exact database schema and ensure the purging logic preserves exactly what you consider "essential" while removing the bulk data storage.
