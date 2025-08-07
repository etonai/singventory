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

## Phase and Development Cycle Completion Rules

**CRITICAL**: Phases and DevCycles cannot be marked as "Completed" without user verification.

- **"In Progress"**: Phase/cycle is actively being worked on
- **"In Verification"**: All tasks completed, awaiting user confirmation
- **"Completed"**: Only after user explicitly verifies and approves completion

**Process**:
1. Complete all phase tasks and mark as "In Verification"
2. Request user verification of completion
3. Only mark as "Completed" after user confirmation
4. Never assume completion without explicit user approval