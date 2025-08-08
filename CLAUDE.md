# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Singventory is an Android application written in Kotlin. This is a fresh Android project with minimal implementation currently containing only example test files.

**Package**: `com.pseddev.singventory`
**Target SDK**: Android API 36
**Min SDK**: Android API 36
**Build System**: Gradle with Kotlin DSL

## Development Commands

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

# Run specific test class
./gradlew test --tests "com.pseddev.singventory.ExampleUnitTest"

# Run all tests
./gradlew check
```

### Development Commands
```bash
# Lint check
./gradlew lint

# Generate lint report
./gradlew lintDebug

# Start Android emulator (if configured)
# Use Android Studio or adb commands
```

## Architecture

### Current Structure
- **Main Package**: `com.pseddev.singventory`
- **Build Configuration**: Uses Gradle Version Catalogs (`gradle/libs.versions.toml`)
- **Testing**: JUnit 4 for unit tests, AndroidX Test for instrumented tests
- **UI Framework**: Material Components with DayNight theme support
- **Minimum Setup**: Currently contains only manifest and example test files

### Key Dependencies
- AndroidX Core KTX
- AppCompat
- Material Design Components
- JUnit 4 (unit testing)
- AndroidX Test (instrumented testing)
- Espresso (UI testing)

### Project Structure
```
app/src/
├── main/
│   ├── AndroidManifest.xml
│   ├── java/com/pseddev/singventory/ (main source - currently empty)
│   └── res/ (resources: themes, strings, icons)
├── test/ (unit tests)
└── androidTest/ (instrumented tests)
```

## Development Notes

- This project uses Kotlin DSL for Gradle configuration
- Version management is centralized in `gradle/libs.versions.toml`
- The app currently has no main activities or UI implementation
- Uses Material Design theming with both light and dark mode support
- Targets modern Android (API 36) exclusively

## Development Process Documentation

**IMPORTANT**: Review `docs/DevCycle_overview.md` - this document defines our structured development cycle process for organizing and tracking focused work periods. It establishes critical rules for:
- Phase planning and implementation workflows
- Progress tracking with TODO items and status management  
- Verification requirements before marking work as complete
- Documentation standards and archival processes
- Integration with other project documentation

All development work should follow the DevCycle methodology outlined in that document.

## Deferred Features Tracking

**CRITICAL**: Always review `docs/Deferred_Features.md` when starting new development cycles or planning future work.

This dedicated document tracks all features that have been deferred from their original development cycles, ensuring important functionality is never forgotten as the project progresses.

**Current Deferred Features** (see `docs/Deferred_Features.md` for full details):
- Song-Venue Association Screens (from DevCycle 3 Phase 2) - Medium-High Priority

**Process**:
1. Review `docs/Deferred_Features.md` at the start of each new DevCycle
2. Update feature priorities based on current project needs
3. Add newly deferred features to the document with full context
4. Move completed deferred features to the "Completed" section

## ⚠️ CRITICAL COMPLETION RULES ⚠️

**ABSOLUTE REQUIREMENT**: You are FORBIDDEN from marking phases or DevCycles as "Completed" without explicit user verification and permission.

### Status Definitions
- **"In Progress"**: Phase/cycle is actively being worked on
- **"In Verification"**: All tasks completed, AWAITING USER CONFIRMATION
- **"Completed"**: ONLY after user explicitly verifies and grants permission

### Mandatory Process
1. Complete all phase/cycle tasks 
2. **MUST** mark status as "In Verification" 
3. **MUST** explicitly request user verification
4. **WAIT** for user confirmation before proceeding
5. **ONLY** mark as "Completed" after receiving explicit user permission
6. **NEVER** assume or auto-complete without user approval

### ENHANCED ENFORCEMENT AFTER REPEATED VIOLATIONS
**Given the pattern of 4+ violations, additional restrictions apply:**
- **TRIPLE CHECK**: Before marking anything as complete, ask yourself "Did the user explicitly say this is complete?"
- **IMMEDIATE CORRECTION**: If you accidentally mark something as complete, immediately correct it to "In Verification"
- **EXPLICIT ACKNOWLEDGMENT**: Always acknowledge when work is ready for verification rather than assuming completion

### What This Means
- ❌ **DO NOT** mark anything as "Completed" on your own
- ❌ **DO NOT** assume the user approves completion 
- ❌ **DO NOT** mark as "Completed" just because you documented deferrals
- ❌ **DO NOT** assume that deferring items means the phase is complete
- ✅ **ALWAYS** use "In Verification" when work is done
- ✅ **ALWAYS** ask for explicit verification
- ✅ **WAIT** for user permission before marking "Completed"
- ✅ **EVEN WITH DEFERRALS** - user must still explicitly approve phase completion

### Deferral vs Completion
- **Deferring individual items**: Acceptable and should be documented in Deferred_Features.md
- **Completing a phase with deferrals**: Still requires user verification and approval
- **Key principle**: Phase completion is about user satisfaction with delivered work, not completion of every originally planned item

**VIOLATION OF THIS RULE IS NOT ACCEPTABLE**

### Enforcement and Accountability
- **If you violate these rules**: Immediately correct the status to "In Verification" and acknowledge the mistake
- **No exceptions**: These rules apply regardless of how thorough or complete the implementation appears
- **User control**: Only the user has authority to determine when work meets their satisfaction standards
- **Trust and reliability**: Following these rules is essential for maintaining user trust in the AI assistant

**HISTORICAL VIOLATIONS**: Claude has violated these completion rules at least 4 times already, demonstrating a pattern of premature completion marking without user verification. This persistent violation undermines user trust and project control.

**VIOLATION #4 (2025-01-08)**: During DevCycle 5 Phase 1 implementation, Claude marked Phase 1 as "✅ COMPLETED" without user verification, despite finding existing implementation. Even when discovering pre-existing functionality, completion status requires user verification and approval.

**CRITICAL REMINDER**: Even if all technical tasks are complete, comprehensive documentation is written, builds are successful, and functionality appears perfect - you MUST still wait for explicit user verification before marking anything as "Completed".

**VIOLATION PREVENTION**: If you find yourself about to mark something as "Completed", STOP and ask: "Did the user explicitly tell me this is complete?" If the answer is no, use "In Verification" instead.

## ⚠️ CRITICAL GIT COMMIT RULES ⚠️

**ABSOLUTE REQUIREMENT**: You are FORBIDDEN from attempting to commit changes unless explicitly requested by the user.

### Git Commit Policy
- ❌ **DO NOT** run `git commit` commands on your own initiative
- ❌ **DO NOT** assume the user wants changes committed
- ❌ **DO NOT** automatically commit after completing tasks
- ✅ **ONLY** commit when the user explicitly asks you to commit
- ✅ **WAIT** for explicit user request before any git operations

### What This Means
- Complete all requested work without committing
- Mark phases/tasks as completed when verified by user
- **NEVER** automatically commit as part of completion process
- User controls when and what gets committed to version control

**VIOLATION OF THIS RULE IS NOT ACCEPTABLE**

## ⚠️ CRITICAL IMPLEMENTATION AUTHORIZATION RULES ⚠️

**ABSOLUTE REQUIREMENT**: You are FORBIDDEN from beginning implementation of any phase, DevCycle, or significant development work without explicit user authorization.

### Implementation Authorization Policy
- ❌ **DO NOT** start implementing phases or DevCycles automatically
- ❌ **DO NOT** assume continuation from one phase to the next
- ❌ **DO NOT** begin coding just because a phase is marked as "TODO"
- ❌ **DO NOT** begin implementation when asked to "add a phase" to documentation
- ✅ **ONLY** begin implementation when the user explicitly requests it
- ✅ **ALWAYS** wait for clear user direction before starting work
- ✅ **ALWAYS** check with user before beginning any phase implementation
- ✅ **RESEARCH and PLANNING** are allowed without explicit permission

### What This Means
- You can read, analyze, and understand the codebase freely
- You can research requirements and plan approaches
- You can answer questions and provide guidance
- You can add phases to documentation when requested
- **NEVER** start actual implementation (writing code, creating files, making changes) without explicit user request
- **ALWAYS** ask "Should I implement this phase?" before beginning any implementation work
- User controls when development work begins, not the todo status in documentation
- Adding documentation ≠ permission to implement

### Approved Activities Without Permission
- Reading files and documentation
- Analyzing existing code patterns
- Researching technical approaches
- Answering user questions
- Planning and design discussions
- Reviewing project status
- Adding phases to documentation (documentation only)

### Requires Explicit User Permission AND Confirmation
- Writing new code files
- Modifying existing code
- Implementing new features
- Starting phase or DevCycle work
- Making any changes to the codebase
- ANY implementation work, even after adding documentation

**VIOLATION OF THIS RULE IS NOT ACCEPTABLE**