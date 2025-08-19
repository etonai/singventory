# Screenshot Requirements for Google Play Store

## Technical Specifications

### Phone Screenshots
- **Count:** 2-8 screenshots required, 8 recommended
- **Aspect Ratio:** 16:9 or 9:16 (portrait recommended)
- **Minimum Resolution:** 320px shortest side
- **Maximum Resolution:** 3840px longest side
- **Format:** PNG or JPEG
- **File Size:** Maximum 8MB per image

### Tablet Screenshots (Optional)
- **Count:** 1-8 screenshots
- **Aspect Ratio:** 16:10 landscape recommended
- **Same resolution and format requirements as phone

## Required Screenshots

### 1. Venues List Screen
**File:** `01_venues_list.png`
**Description:** Main venue list showing multiple venues
**Key Elements to Include:**
- App toolbar with "Singventory" title
- List of 4-5 sample venues with names and locations
- Floating Action Button (+) for adding venues
- Bottom navigation bar
- Sample venues: "Karaoke Palace", "Sing Station", "Melody Lounge", "Voice Box", "Harmony Hall"

### 2. Venue Details Screen  
**File:** `02_venue_details.png`
**Description:** Individual venue information page
**Key Elements to Include:**
- Venue name as title (e.g., "Karaoke Palace")
- Venue address and details
- Song count indicator (e.g., "42 songs")
- Recent visits section
- "View Songs" button
- "Edit Venue" option

### 3. Songs List Screen
**File:** `03_songs_list.png`
**Description:** Song list for a specific venue with favorites
**Key Elements to Include:**
- Venue name in toolbar (e.g., "Karaoke Palace Songs")
- Sort dropdown showing "Favorites & Title"
- List of songs with star icons (some filled gold, some outline)
- Mix of favorite and non-favorite songs
- Song titles like "Don't Stop Believin'", "Sweet Caroline", "Bohemian Rhapsody"
- Artist names visible
- Key adjustment indicators (+2, -1, etc.)

### 4. Add/Edit Song Screen
**File:** `04_add_edit_song.png`
**Description:** Song creation or editing interface
**Key Elements to Include:**
- "Add Song" or "Edit Song" title
- Song name input field with sample text
- Artist name input field
- Key adjustment spinner/selector
- Favorite star toggle (large, prominent)
- Save and Cancel buttons
- Clean, material design form layout

### 5. Performance History Screen
**File:** `05_performance_history.png`
**Description:** List of recent performances
**Key Elements to Include:**
- "Recent Performances" title
- List of performances with timestamps
- Song names and venue names
- Key adjustments shown
- Date/time information (e.g., "Today 8:30 PM", "Yesterday 7:15 PM")
- Performance count indicator

### 6. Edit Song Details with Perform Button
**File:** `06_song_details_perform.png`
**Description:** Song details page with perform song functionality
**Key Elements to Include:**
- Song name and artist at top
- Key adjustment selector
- Large "Perform Song" button with music note icon
- Performance count/history
- Save/Delete buttons at bottom
- Current venue context visible

### 7. Favorites View
**File:** `07_favorites_view.png`
**Description:** Song list filtered to show only favorites
**Key Elements to Include:**
- All songs showing filled gold stars
- "Favorites" or filtered view indicator
- Popular karaoke songs in the list
- Clean, organized layout emphasizing the star system
- Sort option showing favorites-first ordering

### 8. App Settings/About Screen
**File:** `08_settings_about.png`
**Description:** Settings page with app information
**Key Elements to Include:**
- App version information (0.1.0-beta)
- Privacy Policy link
- Terms of Service link
- Legal Notices link
- Developer information
- Material design settings layout

## Screenshot Content Guidelines

### Sample Data to Use
**Venues:**
- Karaoke Palace (123 Main St, Downtown)
- Sing Station (456 Oak Ave, Midtown) 
- Melody Lounge (789 Pine Rd, Westside)
- Voice Box (321 Elm St, Eastside)
- Harmony Hall (654 Maple Dr, Uptown)

**Popular Karaoke Songs:**
- "Don't Stop Believin'" by Journey
- "Sweet Caroline" by Neil Diamond
- "Bohemian Rhapsody" by Queen
- "Living on a Prayer" by Bon Jovi
- "Mr. Brightside" by The Killers
- "I Want It That Way" by Backstreet Boys
- "Total Eclipse of the Heart" by Bonnie Tyler
- "Livin' la Vida Loca" by Ricky Martin

### Visual Design Principles
- **Clean Interface:** Minimal clutter, focus on key features
- **Material Design:** Follow Google's design guidelines
- **Readable Text:** Ensure all text is legible at thumbnail size
- **Vibrant Colors:** Use the app's color scheme consistently
- **Real-world Data:** Use realistic venue names and song titles
- **Feature Highlighting:** Make key features (stars, buttons) prominent

### Screenshot Capture Process
1. **Setup Test Data:** Populate app with sample venues and songs
2. **Use Emulator:** Android Studio emulator with high-resolution display
3. **Frame Screenshots:** Use phone/tablet frames if desired
4. **Optimize for Thumbnails:** Ensure visibility when scaled down
5. **A/B Test Options:** Create variations for key screenshots
6. **Review on Mobile:** Check how screenshots appear on phone screens

## Promotional Graphics

### Feature Graphic Specification
**File:** `feature_graphic.png`
**Size:** 1024x500px
**Content Requirements:**
- App logo prominently displayed
- Tagline: "Your Personal Karaoke Companion"
- Background: Music/karaoke themed (microphone, music notes, stage lights)
- Color scheme matching app theme
- Text overlay should be minimal but impactful
- High-quality, professional appearance

### App Icon Requirements
**File:** `ic_launcher_512.png`
**Size:** 512x512px
**Design Elements:**
- Microphone icon as primary element
- Music note or karaoke-themed secondary elements
- App brand colors
- Clean, recognizable design that works at all sizes
- Professional appearance suitable for store listing

## Quality Assurance Checklist

### Before Screenshot Capture
- [ ] App populated with realistic sample data
- [ ] All UI elements properly styled and positioned
- [ ] No debug information or placeholder text visible
- [ ] Consistent theming across all screens
- [ ] Error-free navigation and functionality

### During Screenshot Review
- [ ] All required screenshots captured
- [ ] Images are high resolution and crisp
- [ ] Text is readable at thumbnail size
- [ ] Key features are highlighted and visible
- [ ] Screenshots tell a cohesive story of app functionality
- [ ] No sensitive or inappropriate content visible

### Final Verification
- [ ] File sizes under 8MB limit
- [ ] Correct aspect ratios maintained
- [ ] Professional, polished appearance
- [ ] Screenshots showcase unique value propositions
- [ ] Ready for Google Play Store submission

## Screenshot Alternatives

If unable to generate screenshots immediately:
1. **Wireframe Screenshots:** Create mockup versions showing UI layout
2. **Design Tool Screenshots:** Use Figma/Adobe XD to create representative screens
3. **Placeholder Strategy:** Submit with basic screenshots, update in later releases
4. **Development Priority:** Focus on core functionality, improve screenshots over time

The most critical screenshots are #1 (Venues List), #3 (Songs List), and #6 (Song Details) as they showcase the core functionality that differentiates Singventory from other apps.