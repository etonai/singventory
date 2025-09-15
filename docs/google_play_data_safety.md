# Google Play Data Safety Disclosure

This document provides the data safety information required for Google Play Console submission.

## Data Safety Declaration Summary

**Data Sharing:** No user personal data is shared with third parties  
**Data Collection:** Limited analytics data is collected  
**Data Storage:** All user data is stored locally on device  
**Data Encryption:** Local data is stored in encrypted SQLite database  

## Data Types and Collection

### Data Collected

#### App Activity
- **Type:** App interactions, app performance, crash logs, diagnostics
- **Purpose:** Analytics, app functionality 
- **Collected:** Yes
- **Shared:** No (anonymous analytics only)
- **Optional:** No (required for app functionality and improvement)
- **Ephemeral Processing:** No

#### Device or Other IDs
- **Type:** Analytics identifiers (Firebase Analytics ID)
- **Purpose:** Analytics
- **Collected:** Yes (automatically by Firebase)
- **Shared:** No (processed by Google Firebase only)
- **Optional:** No (automatically generated)
- **Ephemeral Processing:** No

### Data NOT Collected

- ❌ **Personal Info:** Name, email, address, phone, etc.
- ❌ **Financial Info:** Credit cards, payment methods, purchase history
- ❌ **Health and Fitness:** Health metrics, fitness data
- ❌ **Messages:** SMS, email, chat messages
- ❌ **Photos and Videos:** User media files
- ❌ **Audio Files:** Voice recordings, music files
- ❌ **Files and Docs:** User documents, files
- ❌ **Calendar:** Calendar events, reminders
- ❌ **Contacts:** Contact list, address book
- ❌ **Location:** Precise or approximate location
- ❌ **Web Browsing:** Browser history, bookmarks

## Data Sharing

**No user personal data is shared with third parties.**

- **Karaoke Data:** Venues, songs, performances are stored locally only
- **Analytics:** Only anonymous usage analytics via Firebase (Google)
- **Third-party Services:** Only Google Firebase Analytics (anonymous data)

## Data Security

### Encryption
- **Data in Transit:** HTTPS encryption for analytics transmission
- **Data on Device:** SQLite database with Android's built-in encryption

### Data Access
- **Local Data:** Only accessible by the app and user
- **Analytics Data:** Anonymous, processed by Google Firebase
- **No Human Access:** Developers cannot access individual user data

## Data Retention and Deletion

### Local Data
- **Retention:** Indefinite (until app uninstall or user deletion)
- **User Control:** Complete control via app interface
- **Deletion:** Permanent deletion when app is uninstalled

### Analytics Data
- **Retention:** According to Google Firebase Analytics policy (typically 14 months)
- **Control:** Cannot be deleted individually (anonymous/aggregated)
- **Policy:** Governed by Google's data retention policies

## User Rights and Controls

### Data Access
- **Local Data:** Full access via app interface
- **Export:** Available via Android backup system
- **Viewing:** All data visible within the app

### Data Deletion
- **Individual Records:** Can delete venues, songs, performances individually
- **Complete Deletion:** Uninstall app to remove all local data
- **Analytics Opt-out:** Feature planned for future version

### Data Correction
- **Local Data:** Full editing capabilities within the app
- **Analytics:** Cannot be corrected (anonymous/aggregated)

## GDPR Compliance (EU Users)

### Legal Basis
- **Local Data Processing:** Legitimate interest (app functionality)
- **Analytics:** Consent (implied by app usage)

### User Rights
- **Access:** Full access to all local data
- **Portability:** Android backup system provides data export
- **Deletion:** Complete data deletion via app uninstall
- **Rectification:** Full editing capabilities in app
- **Object:** Can uninstall app or disable analytics (future feature)

### Data Controller
- **Local Data:** User is data controller
- **Analytics:** Google Firebase (Google LLC)

## Children's Privacy

- **Target Age:** 13+ (not directed at children)
- **COPPA Compliance:** No data knowingly collected from children under 13
- **Age Verification:** None required (general audience app)
- **Parental Controls:** Standard Android parental controls apply

## Contact Information

For data safety inquiries:
- **Email:** PseudonymousEd@gmail.com
- **Developer:** PseudonymousEd
- **Privacy Policy:** Available in app and at [URL when available]

## Last Updated

**Date:** August 18, 2025  
**Version:** 0.1.0-beta  
**Review Status:** Ready for Google Play Console submission