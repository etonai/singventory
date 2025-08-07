# Development Cycles Overview

This document explains the development cycle process used for organizing and tracking focused work periods in the Singventory project.

## ⚠️ CRITICAL RULE: COMPLETION VERIFICATION ⚠️

**ABSOLUTE PROHIBITION**: Claude Code is FORBIDDEN from marking any phase or DevCycle as "Completed" without explicit user verification and permission.

### Required Process:
1. Complete all work and mark as **"In Verification"** 
2. **EXPLICITLY REQUEST** user verification
3. **WAIT** for user confirmation  
4. **ONLY** mark as "Completed" after user grants permission
5. **NEVER** assume completion or mark as "Completed" autonomously

**This rule is non-negotiable and must be followed without exception.**

## Purpose

Development cycles provide a structured approach to:
- Focus on completing specific features or improvements
- Track progress with clear TODO items and phase breakdowns
- Document implementation details and decisions
- Ensure thorough testing and verification before completion
- Maintain historical record of development work

## Cycle Structure

### Naming Convention
Development cycles follow the format: `DevCycle_YYYY_####.md`
- **YYYY**: Year (e.g., 2025)
- **####**: Sequential 4-digit cycle number (e.g., 0001, 0002, 0003)

**Examples:**
- `DevCycle_2025_0001.md` - First development cycle of 2025
- `DevCycle_2025_0002.md` - Second development cycle of 2025

### File Organization
- **Active cycles**: Stored in `/docs/` directory during development
- **Completed cycles**: Moved to `/docs/devcycles/` directory for archival
- **Current cycle**: Always the active cycle document in `/docs/`

### Document Template Structure

Each development cycle document should include:

```markdown
# Development Cycle YYYY-####

**Status:** [In Progress/Completed/On Hold]  
**Start Date:** YYYY-MM-DD  
**Target Completion:** YYYY-MM-DD or TBD  
**Focus:** [Brief description of cycle focus]

## Overview
[2-3 sentences describing the cycle's purpose and goals]

## Current Work Items
[List of features, bugs, or tickets being addressed]

### [Feature/Bug/Ticket Name]
**Status:** [Emoji and status]  
**Priority:** [Critical/High/Medium/Low]  
**Description:** [Brief description]

#### Phase Progress
**Phase [Name]:**
- ✅ [Completed item]
- ❌ TODO: [Pending item]

#### Technical Implementation Details
[Code changes, dependencies, configurations, etc.]

#### Next Steps
[Immediate next actions]

## Cycle Notes
[Important observations, decisions, or context]

## Future Cycles
[What might be addressed in subsequent cycles]

## Cycle Completion Summary
*[Added when cycle is completed]*

**Final Build Version:** [e.g., v1.0.0 (code 1) - debug]  
**Completion Date:** YYYY-MM-DD  
**Git Commit Status:** [All changes committed / Commit hash if applicable]

**Accomplishments:**
- [Major feature/bug/ticket completed]
- [Key technical improvements made]
- [Important milestones reached]

**Metrics:**
- [Files modified count]
- [Features implemented]
- [Bugs fixed]
- [Tests added/updated]

**Notes:**
- [Key learnings or insights]
- [Challenges encountered and solutions]
- [Impact on app functionality or performance]
- [Version control status and commit details]
```

## Cycle Management Process

### 1. Cycle Initiation
- Create new DevCycle document when starting focused work
- Define clear scope and objectives
- Identify primary work items (features, bugs, tickets)
- Set realistic target completion dates

### 2. Phase Planning and Implementation
- **CRITICAL RULE**: When adding a new phase to a DevCycle, NEVER immediately begin implementing it
- New phases must be added to documentation first and marked as "Open" status
- Allow time for phase planning, refinement, and clarification before implementation begins
- Initial phase descriptions are often incomplete and need further discussion/planning
- Wait for explicit approval or further direction before moving to implementation
- Once phase is fully planned and approved, then begin implementation work

### 3. Active Development
- Update TODO items in real-time as work progresses
- Mark completed items with ✅
- Mark pending items with ❌ TODO:
- Document technical decisions and implementation details
- Add cycle notes for important context or changes

### 4. Progress Tracking
- Regularly review and update cycle status
- Adjust scope if needed (document changes in cycle notes)
- Ensure all work aligns with cycle objectives
- Maintain clear next steps for continuation

### 5. Cycle Completion
- Verify all work items are complete or properly transitioned
- **Ensure all code changes are committed to version control**
- **⚠️ MANDATORY**: Mark cycle/phase as "In Verification" when all tasks are complete
- **⚠️ MANDATORY**: Request user verification before marking as "Completed"
- **⚠️ ABSOLUTELY FORBIDDEN**: NEVER mark cycles or phases as "Completed" without explicit user confirmation
- **⚠️ MANDATORY**: Update final status to "Completed" ONLY after user verification and permission
- **⚠️ CRITICAL**: Even if some individual TODO items are marked as deferred, the PHASE/CYCLE itself must still go through "In Verification" → User Approval → "Completed" process
- **⚠️ CRITICAL**: Documentation of deferred items does NOT constitute completion - user must explicitly verify and approve phase completion despite deferrals
- Document cycle accomplishments summary
- Record final build version (versionName and versionCode) for the cycle
- Document lessons learned in cycle notes
- Move completed cycle document to `devcycles/` directory for archival
- Plan subsequent cycle if needed

### 6. Cycle Handoff
- When switching between developers or sessions
- Current cycle status provides clear continuation point
- All context and progress preserved in documentation
- New work can start immediately from documented state

## Integration with Existing Documentation

### Relationship to Other Documents
- **features.md**: Individual features referenced in cycles maintain their own status
- **bugs.md**: Bug fixes implemented during cycles update bug status
- **tickets.md**: Tickets addressed in cycles update ticket status
- **CLAUDE.md**: Verification rules apply to all cycle work items

### Status Synchronization
- Work items in cycles must maintain consistent status with source documents
- Completed cycle work triggers status updates in features.md, bugs.md, or tickets.md
- Verification requirements apply: items move to verification status until user confirms

### Documentation Flow
1. Work identified in features.md, bugs.md, or tickets.md
2. Work planned and tracked in DevCycle document
3. Implementation progresses with TODO tracking
4. Completion updates both cycle and source document status
5. User verification required before final completion marking

## Benefits

### For Developers
- Clear focus and scope for work sessions
- Detailed progress tracking prevents lost work
- Historical context for implementation decisions
- Easy handoff between sessions or team members

### For Project Management
- Visibility into current development focus
- Progress tracking across multiple work items
- Historical record of development cycles
- Clear completion criteria and verification requirements

### For Quality Assurance
- Comprehensive documentation of changes
- Clear testing requirements and verification steps
- Implementation details support thorough testing
- Progress tracking ensures nothing is missed

## Best Practices

### Scope Management
- Keep cycles focused (typically 1-3 major work items)
- Break large features into multiple cycles if needed
- Document scope changes and rationale
- Don't let cycles grow beyond manageable size

### Documentation Quality
- Update TODO items frequently during development
- Include relevant technical details and code snippets
- Document decisions and their rationale
- Maintain clear next steps for continuity
- Record build version numbers (versionName/versionCode) at key milestones

### Progress Tracking
- Be honest about completion status
- Mark items complete only when fully implemented
- **⚠️ MANDATORY**: Use "In Verification" status when tasks are complete but awaiting user confirmation
- **⚠️ MANDATORY**: Follow verification requirements before final completion
- Update source documents consistently
- **⚠️ ABSOLUTELY FORBIDDEN**: NEVER mark as "Completed" without explicit user verification and approval
- **⚠️ CRITICAL REMINDER**: Individual TODO items can be deferred, but phases/cycles require explicit user approval to mark as "Completed" regardless of deferrals
- **⚠️ PROCESS VIOLATION ALERT**: If you mark a phase as "Completed" without user permission, you have violated the core process - immediately mark as "In Verification" and request user approval

### Cycle Transitions
- Complete current cycle documentation before starting new cycle
- **Ensure all code changes are committed before marking cycle complete**
- **Confirm completion date/time with user before finalizing cycle**
- Move completed cycle to `devcycles/` directory for archival
- Ensure clear handoff documentation
- Plan subsequent cycles based on previous cycle outcomes
- Maintain continuity in development focus

### Archival Process
- Never create `/docs/devcycles/` directory if it doesn't exist. If the directory doesn't exist either you are looking in the wrong location or it was deleted accidentally and you should alert me.
- Move completed `DevCycle_YYYY_####.md` file to `/docs/devcycles/`
- Completed cycles serve as historical record and reference
- Archival keeps active `/docs/` directory focused on current work