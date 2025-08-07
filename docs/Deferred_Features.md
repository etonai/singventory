# Deferred Features Tracking

This document tracks features that have been deferred from their original development cycles for implementation in future cycles. This ensures important functionality doesn't get lost as the project progresses through multiple development cycles.

## Active Deferred Features

### Song-Venue Association Screens
**Originally Planned**: DevCycle 3 Phase 2  
**Deferred Date**: 2025-08-07  
**Priority**: Medium-High  
**Suggested Implementation**: DevCycle 4 or later (after core visit management)

**Description**: Advanced relationship management screens for associating songs with venues, enabling users to track venue-specific song catalogs and numbering systems.

**Technical Requirements**:
- Screen to select venues for existing songs (triggered from SongsAdapter "Add venue" button)
- Screen to select songs for existing venues (triggered from VenuesAdapter "Add song" button)
- SongVenueInfo entity relationship management with venue-specific song IDs
- Search and filter functionality for large song/venue lists during association
- Autocomplete and batch operation support for efficient association management

**User Scenarios**:
- Adding venue information to songs discovered during karaoke visits
- Pre-planning song repertoire for known venues before visits
- Managing venue-specific song catalogs and venue numbering systems
- Bulk association operations for efficiency

**Implementation Notes**:
- Requires advanced relationship management UI patterns
- Should integrate with existing search/filter patterns from Phase 2 CRUD screens
- Consider batch selection and multi-select patterns for efficiency
- May benefit from venue song ID input for catalog integration

**Database Dependencies**: 
- SongVenueInfo entity (already implemented in DevCycle 2)
- Repository methods for song-venue associations (partially implemented)

**UI Dependencies**:
- SongsAdapter "Add venue" buttons (implemented but not connected)
- VenuesAdapter "Add song" buttons (implemented but not connected)
- Navigation actions need to be created

**Deferral Reason**: Complex relationship management feature that goes beyond basic CRUD requirements of Phase 2. Core Songs and Venues CRUD functionality was prioritized to enable fundamental app usage.

---

## Instructions for Future Development

When starting new DevCycles:

1. **Review this document** to identify deferred features that should be considered for the new cycle
2. **Update priorities** based on current project needs and user feedback
3. **Move implemented features** to a "Completed Deferred Features" section with implementation details
4. **Add new deferrals** when features are postponed during development

### Association Details Dialog
**Originally Planned**: DevCycle 4 Phase 1  
**Deferred Date**: 2025-08-07  
**Priority**: Medium  
**Suggested Implementation**: DevCycle 5 or later

**Description**: Enhanced dialog for collecting venue-specific details during song-venue association, enabling complete venue catalog integration.

**Technical Requirements**:
- Dialog fragment for association detail collection
- Venue Song ID input field (venue's internal catalog number)
- Venue Key selection with MusicalKey enum integration
- Key Adjustment input with step-based controls (+/- buttons for -3 to +3 steps)
- Venue-specific lyrics text input (optional override of main song lyrics)
- Form validation for musical keys and step adjustments
- Integration with existing association workflows in both fragments

**User Scenarios**:
- Recon visits: Recording venue song catalog numbers and keys during research visits
- Repeat visits: Quick lookup using venue song IDs and pre-configured key adjustments
- Advanced catalog management: Maintaining complete venue-specific song information
- Key optimization: Recording which key adjustments work best at each venue

**Implementation Notes**:
- Should replace current placeholder `showAssociationDetailsDialog()` functions
- Needs to integrate with MusicalKey enum from existing song creation screens
- Consider dual input methods: dropdown key selection AND +/- step adjustment
- Form should support partial entry (users can skip optional fields)
- Validation should prevent invalid key combinations

**Current State**:
- Basic association functionality implemented (users can associate songs with venues)
- Placeholder dialog functions exist in both AssociateSongsWithVenueFragment and AssociateVenuesWithSongFragment
- SongVenueInfo entity supports all required fields (venuesSongId, venueKey, keyAdjustment, lyrics)
- Repository methods for updating associations already implemented

**Deferral Reason**: Core association functionality was prioritized to enable basic song-venue relationship management. The detailed venue-specific information, while valuable for advanced users, is not critical for initial association workflows.

---

## Completed Deferred Features

### Song-Venue Association Screens
**Originally Planned**: DevCycle 3 Phase 2  
**Completed**: DevCycle 4 Phase 1  
**Implementation Date**: 2025-08-07

**Final Implementation**:
- AssociateSongsWithVenueFragment with venue info display and song filtering
- AssociateVenuesWithSongFragment with song info display and venue filtering  
- Multi-select UI with batch association operations
- Smart filtering by association status and performance/visit frequency
- Navigation integration with existing Songs and Venues adapter buttons
- Full MVVM architecture with StateFlow reactive programming

**Notes**: Successfully implemented with Material Design 3 patterns and comprehensive filtering. Association Details Dialog was deferred to future cycle as separate feature.

*Features will be moved here when implemented, with notes about their final implementation*