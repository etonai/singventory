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

## Completed Deferred Features

*Features will be moved here when implemented, with notes about their final implementation*