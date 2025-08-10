# Singventory

**A Karaoke Performance Tracking App for Android**

Singventory is an Android application designed to help karaoke enthusiasts track their song performances across different venues. Keep detailed records of your favorite songs and optimize your karaoke experience with personalized key adjustments that you'll never forget.

> **Development Note**: This app was developed using [Claude Code](https://claude.ai/code) by an experience software developer with no Android development experience prior to July 2025. Claude Code's AI-assisted development capabilities made it possible to create a fully functional Android app.

## 🎤 What is Singventory?

Singventory helps you maintain an inventory of songs you like to sing at various karaoke venues. Each venue may have different versions of songs with different keys, song codes, and audio quality. This app helps you track all these venue-specific details so you can have the best possible karaoke experience every time.

## ✨ Key Features

### 📋 Song Management
- **Personal Song Library**: Maintain your collection of favorite karaoke songs
- **Artist & Title Tracking**: Organize songs by name and artist
- **Reference Keys**: Store your preferred singing key and the song's reference key
- **Performance Statistics**: Track how many times you've performed each song and when
- **Lyrics Storage**: Optional lyrics storage for each song

### 🏢 Venue Management
- **Venue Database**: Keep detailed information about karaoke venues
- **Location Details**: Store address, cost, hours, and room types (private/public)
- **Visit History**: Track all your visits to each venue with dates and notes
- **Venue-Specific Song Data**: Different song codes and keys for each venue

### 🎵 Song-Venue Associations
- **Venue Song Codes**: Store the venue's internal song identification numbers
- **Key Adjustments**: Track how many steps up or down to adjust from the venue's key
- **Venue-Specific Keys**: Record the key each venue plays a song in
- **Performance Counts**: Track performances per song per venue
- **Custom Lyrics**: Override global lyrics with venue-specific versions

### 🎯 Visit & Performance Tracking
- **Visit Logging**: Create visits for each karaoke session
- **Performance Recording**: Log each song you perform during a visit
- **Key Adjustment Notes**: Record how the key adjustments worked out
- **Spending Tracking**: Optional tracking of money spent per visit
- **Session Notes**: Add notes about your karaoke sessions

### 🎼 Musical Key Management
- **Never Forget Your Keys**: Store your optimal key adjustments on your personal device so you never have to remember "was that +2 or +3 steps?"
- **Comprehensive Key Support**: Full 24-key support (12 major + 12 minor keys)
- **Automatic Calculations**: Auto-calculate key adjustments between venue key and your preferred key
- **Dual Input Methods**: Choose keys from dropdowns or use +/- step adjustments
- **Visual Key Display**: Clear display showing both venue key and your adjusted key

## 🛠️ Technical Details

### Architecture
- **MVVM Pattern**: Model-View-ViewModel architecture with Repository pattern
- **Room Database**: Local SQLite database for offline-first functionality
- **Kotlin Coroutines**: Asynchronous operations and reactive UI updates
- **Navigation Component**: Modern Android navigation with bottom navigation
- **Material Design 3**: Clean, accessible user interface following Android design guidelines

### Database Schema
- **Songs**: Core song information with performance statistics
- **Venues**: Venue details and visit statistics
- **Visits**: Individual karaoke sessions
- **Performances**: Specific song performances during visits
- **Song-Venue Info**: Junction table linking songs to venues with venue-specific data

### Data Management
- **Import/Export**: JSON-based backup and restore functionality
- **Data Purging**: Smart purging system that removes old visit details while preserving essential statistics
- **Offline First**: All core functionality works without internet connection
- **Data Consistency**: Automatic statistics updates across all related entities

## 📱 User Experience Scenarios

### 🎪 Impromptu Visit
You're out with friends and decide to hit up a new karaoke venue:
1. Create a new visit and add the venue information
2. Browse your existing song library to find songs available at this venue
3. Associate songs with the venue, noting song codes and key information
4. Perform songs and log each performance with optional notes
5. Build your knowledge base for future visits

### 🔍 Reconnaissance Visit
Planning to explore a new venue's song catalog:
1. Research the venue beforehand and add detailed venue information
2. Pre-populate song candidates you want to check
3. During your visit, test songs and record key information
4. Make notes about song quality and key adjustments needed
5. Return home and update song details with accurate key information

### 🎵 Regular Visit
Returning to a venue you know well:
1. Create a new visit for the familiar venue
2. Browse songs you've researched with known key adjustments
3. Perform songs using your optimized key settings
4. Log performances to maintain accurate statistics
5. Add notes about how the performances went

## 🔧 Development Status

Singventory is currently in active development following a structured DevCycle methodology. Recent completed phases include:

- ✅ **Core Architecture**: MVVM + Repository pattern with Room database
- ✅ **Basic CRUD Operations**: Full song, venue, visit, and performance management
- ✅ **Smart Associations**: Song-venue linking with automatic pre-population
- ✅ **Statistics Management**: Comprehensive performance tracking and analytics
- ✅ **UI Consistency**: Material Design 3 implementation with consistent branding
- ✅ **Data Integrity**: Proper statistics updates and cascade deletion handling

### Current Phase: Bug Fixes & Polish
Recent critical fixes include:
- Performance deletion workflow improvements
- Visit performance count accuracy
- Statistics consistency during visit deletion
- Async operation handling and navigation timing

### Upcoming Features
- Duplicate song detection and merging
- Enhanced search and filtering capabilities
- Data purging with statistics preservation
- JSON import/export functionality

## 🏗️ Build Requirements

- **Target SDK**: Android API 36
- **Minimum SDK**: Android API 36  
- **Build System**: Gradle with Kotlin DSL
- **Language**: 100% Kotlin
- **Architecture Components**: Room, ViewModel, LiveData, Navigation Component

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Clean build
./gradlew clean

# Build and install on connected device
./gradlew installDebug
```

### Testing Commands

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run all tests
./gradlew check

# Lint check
./gradlew lint
```

## 📊 Data Model Overview

### Core Entities
- **Song**: Name, artist, keys, lyrics, performance statistics
- **Venue**: Name, address, details, visit statistics  
- **Visit**: Date/time, venue, notes, spending
- **Performance**: Song performed, key adjustment, notes, timestamp
- **SongVenueInfo**: Venue-specific song data (codes, keys, adjustments)

### Key Relationships
- Songs can be associated with multiple venues (many-to-many)
- Visits belong to one venue and contain multiple performances
- Performances link songs to specific visits
- Statistics are maintained at song, venue, and song-venue levels

## 🎯 Design Philosophy

### Offline-First
Singventory is designed for use in karaoke venues where internet connectivity may be poor or unreliable. All core features work completely offline, with optional data backup through JSON export.

### Performance-Focused
The app prioritizes quick data entry during live karaoke sessions. Performance logging is optimized for speed and minimal taps while maintaining comprehensive data capture.

### Memory-Friendly
Never forget your key adjustments again! Store your optimal settings on your personal device so you don't have to remember whether "Yesterday" sounds better 2 steps up or 3 steps down at different venues. Key management features support both absolute key selection and relative step adjustments to match how musicians think about transposition.

### Data Preservation
The purging system is designed to remove bulk data (detailed visit records) while preserving essential statistics, ensuring long-term usage doesn't lose valuable performance insights.

## 📝 Development Notes

This project follows a structured development methodology with detailed phase planning and documentation. Each development cycle is thoroughly documented with:

- Phase-by-phase implementation tracking
- Comprehensive technical implementation details
- User workflow verification
- Bug fix documentation and resolution tracking

For detailed development progress, see the `docs/DevCycle_*` files in the repository.

## 🎤 Perfect For

- **Regular Karaoke Enthusiasts**: Track your performances across multiple venues
- **Serious Singers**: Optimize key adjustments for better vocal performance  
- **Multi-Venue Singers**: Build knowledge about song availability across different locations
- **Consistent Performers**: Never forget your optimal key adjustments across venues
- **Musicians**: Manage key transpositions and maintain performance records

---

**Start building your karaoke performance database with Singventory!**