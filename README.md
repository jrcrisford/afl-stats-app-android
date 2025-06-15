# KIT305 - Assignment 4 - Cross Platform App (Flutter)

## Author Information
- **Name**: Joshua Crisford
- **Student ID**: 574082
- **Unit**: KIT305 - Mobile Application Development
- **Assignment**: Assignment 2 - Native Android Application

## Overview
This project is an Android application designed to track and manage Australian Football League (AFL) statistics. It allows users to create teams and players, record player actions, compare players and teams, and view match history. The app is built using Kotlin and integrates with Firebase for data storage.

---

## Testing Instructions
To test this application, please use the following device/emulator configuration:
- **Device Type**: Android Smartphone
- **OS Version**: Android 12 or higher
- **Screen Size**: 6.0 inches or larger
- **Emulator**: Samsung Galaxy S25+ (used for testing)

### How to Start a Match

To begin a match, follow these steps:

1. **Create Teams and Players**:
    - Navigate to the Team Manager and create a team with at least two players.
    - For convenience, two pre-filled teams, **Hobart Hawks** and **Launceston Lions**, are available. These can be selected directly or modified in the Team Manager to test adding, removing, and editing players.

2. **Resetting Teams**:
    - If you wish to reset the pre-filled teams, set the `debug` variable in `activity_main` to `true` and restart the app.

3. **Starting a New Match**:
    - Select "New Match" from the main menu.
    - Choose the two teams that will play (you can use the pre-filled teams if desired).
    - Proceed to the Match Tracking screen.

4. **Recording Actions**:
    - On the Match Tracking screen, press the **Start Quarter** button to begin recording player actions.
    - View player and team statistics directly on this screen.

5. **Ending the Match**:
    - Once all quarters are completed, press the **End Match** button at the bottom of the screen.

6. **Viewing Match History**:
    - Access the Match History from the main menu.
    - Select a match to view detailed statistics, including:
      - Player and team stats (expandable cards for detailed data).
      - Player stats comparison.
      - Game scores (overall and by quarter).
      - A list of recorded actions, which can be exported.

### Pre-Filled Match for Testing
For ease of testing, a pre-filled match named **debug_match_001** is available. This match includes players with recorded actions, allowing you to explore the app's features without needing to create new data.

---

## References
### Tutorials
- The Android tutorials and lecture content were referenced during the development of this project to reinforce understanding of key concepts.
- The tutorial 5 starting code was directly used as a base for this project and adapted to guide the initial structure and implementation.

### Code Snippets
None of the code from these sources was directly used; instead, it was adapted to suit the project.
- Simple Android RecyclerView Example
RecyclerView was used to display lists of players and teams dynamically, managed through a custom adapter.
https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example

- How do I display an AlertDialog on Android?
AlertDialogs were used for renaming players and confirming player deletions in a user-friendly way.
https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android

- Capture Image from Camera and Display in Activity
An intent with MediaStore.ACTION_IMAGE_CAPTURE was used to capture player profile photos via the device camera.
https://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity

- Passing a custom Object from one Activity to another Parcelable vs Bundle
Player and Team objects were passed between activities using Parcelable for efficient object transfer.
https://stackoverflow.com/questions/12363503/passing-a-custom-object-from-one-activity-to-another-parcelable-vs-bundle

- How to use View Binding in Android
View Binding was used throughout the app to access UI elements without using findViewById.
https://stackoverflow.com/questions/57117338/how-to-use-view-binding-in-android

- Android: Share plain text using intent (to all messaging apps)
Plain text intents were used to allow sharing match or player statistics across other apps.
https://stackoverflow.com/questions/9948373/android-share-plain-text-using-intent-to-all-messaging-apps

### General Sources
These sources were primarily used to enhance my understanding of Android development concepts and to implement specific features in the app effectively.
- Simple Android RecyclerView Example
RecyclerView was used to display lists of players and teams dynamically, managed through a custom adapter.
https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example

- How do I display an AlertDialog on Android?
AlertDialogs were used for renaming players and confirming player deletions in a user-friendly way.
https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android

- Capture Image from Camera and Display in Activity
An intent with MediaStore.ACTION_IMAGE_CAPTURE was used to capture player profile photos via the device camera.
https://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity

- Passing a custom Object from one Activity to another Parcelable vs Bundle
Player and Team objects were passed between activities using Parcelable for efficient object transfer.
https://stackoverflow.com/questions/12363503/passing-a-custom-object-from-one-activity-to-another-parcelable-vs-bundle

- How to use View Binding in Android
View Binding was used throughout the app to access UI elements without using findViewById.
https://stackoverflow.com/questions/57117338/how-to-use-view-binding-in-android

- Android: Share plain text using intent (to all messaging apps)
Plain text intents were used to allow sharing match or player statistics across other apps.
https://stackoverflow.com/questions/9948373/android-share-plain-text-using-intent-to-all-messaging-apps

- Kotlin Smart Casts
Safe casting (as?) was used when parsing Firestore fields like "quarter" to avoid runtime crashes.
https://kotlinlang.org/docs/typecasts.html#smart-casts

- Firestore Data Types
Understanding native Firestore types (e.g., Long) ensured correct data retrieval and safe casting.
https://firebase.google.com/docs/firestore/manage-data/data-types

- Material Data-Driven Design (Data Visualization Principles)
Dynamic color highlighting in player comparisons followed principles of using color to emphasize key data differences.
https://m3.material.io/foundations/data-driven-design/data-visualization/overview

- Firestore Batched Writes
Batched writes were used to save teams and their players atomically into Firestore.
https://firebase.google.com/docs/firestore/manage-data/transactions#batched-writes

- Handle Activity Results (Intent Result Handling)
The app handled camera intent results correctly using the recommended result management approach.
https://developer.android.com/training/basics/intents/result

- Material Design Components for Android (MDC-Android)
The app used Material components like MaterialCardView, MaterialButton, and MaterialTextInputLayout to create modern UIs.
https://m3.material.io/develop/android/mdc-android

- Material Design Cards
MaterialCardViews were used for cleanly grouping sections like players, team lists, and match stats.
https://m3.material.io/components/cards/overview

- Material Design Text Fields
TextInputLayouts and AutoCompleteTextViews were used for better form inputs, replacing plain spinners and EditTexts.
https://m3.material.io/components/text-fields/overview

- Structure Your Database (Firestore)
Firestore was structured logically with collections and documents, following best practices for scalable data.
https://firebase.google.com/docs/firestore/manage-data/structure-data

- Camera Intents (Official Android Developers Guide)
The app used camera intents to capture images by launching the system camera app instead of building a custom camera UI.
https://developer.android.com/media/camera/camera-intents

- Get Data from Firestore
Data for teams and players was retrieved from Firestore using standard .get() query operations.
https://firebase.google.com/docs/firestore/query-data/get-data

- Add Data to Firestore
Team and player information were saved into Firestore using .set() operations inside collections.
https://firebase.google.com/docs/firestore/manage-data/add-data

- Get Started with Firestore
The project was connected to Firestore using Firebase's setup guides to initialize authentication and database services.
https://firebase.google.com/docs/firestore/quickstart

- Share Simple Data (Official Android Developers Guide)
Match data and player statistics were shared across apps using ACTION_SEND intents with "text/plain" MIME types.
https://developer.android.com/training/sharing/send

- Parcelize (Official Android Developers)
The app used the @Parcelize annotation to simplify making the Player and Team data classes Parcelable.
https://developer.android.com/kotlin/parcelize

- Parcelable (Android Developers Reference)
Parcelable was used to efficiently pass custom Player and Team objects between activities.
https://developer.android.com/reference/android/os/Parcelable

- RecyclerView (Official Android Developers Guide)
RecyclerViews were used to efficiently display dynamic player and team lists.
https://developer.android.com/develop/ui/views/layout/recyclerview

- AlertDialog (Official Android Developers Guide)
AlertDialogs were used to confirm actions like renaming or deleting players, following standard Android UX patterns.
https://developer.android.com/reference/androidx/appcompat/app/AlertDialog

- Kotlin Data Classes
Player and Team were implemented as data classes for cleaner code and automatic generation of useful methods.
https://kotlinlang.org/docs/data-classes.html

### Use of ChatGPT and Copilot
- **GitHub Copilot**: 
    - Assisted in generating helper functions for JSON parsing in `MainActivity.kt`.
    - Suggested logic for dynamically resizing the `ListView` in `MatchHistoryActivity.kt`.
    - Provided the structure for handling `onActivityResult` in `TeamManagementActivity.kt`.
    - Recommended efficient Firestore query patterns for fetching player and team data in `MatchTrackingActivity.kt`.
    - Helped implement the `@Parcelize` annotation for data classes in `Player.kt` and `Team.kt`.
    - Suggested improvements to RecyclerView adapter implementation in `TeamManagementActivity.kt`.
    - Provided code snippets for handling camera intents in `NewMatchActivity.kt`.
    - Assisted in creating a custom dialog for renaming players in `TeamManagementActivity.kt`.
    - Suggested optimizations for batched writes in Firestore in `MatchTrackingActivity.kt`.
    - Helped with the implementation of dynamic color highlighting in `PlayerComparisonActivity.kt`.
    - Specific uses are noted in code.
    - Copilot autocompletion was used regularly.
    - This list was generated by Copilot! 
- **GitHub Copilot (Windows PC Version)**: 
    - Assisted with written tasks related to the project.
    - Helped identify and reference online resources to enhance understanding and implement features effectively (as listed above).

---

## Activities and Their Relationships
### MainActivity
- **Purpose**: The entry point of the app. Provides navigation to other activities such as `TeamManagementActivity` and `MatchTrackingActivity` via `NewMatchActivity`.
- **Relationships**: 
    - Launches `NewMatchActivity` for creating a new match.
    - Launches `MatchHistoryActivity` for viewing match history.
    - Launches `TeamManagementActivity` for managing teams.
    - Serves as a hub for navigating to other features.

### TeamManagementActivity
- **Purpose**: Allows users to manage teams and players and allows adding, removing, and modifying any team's players.
- **Relationships**: 
    - Returns data to `MainActivity` via `onActivityResult` (firestore).

### MatchTrackingActivity
- **Purpose**: Enables users to record player actions during a match.
- **Relationships**: 
    - Launches `PlayerComparisonActivity` for comparing two players.
    - Sends recorded data to firestore.

### MatchHistoryActivity
- **Purpose**: Displays a history of recorded matches and their associated actions.
- **Relationships**: 
    - Independent activity but interacts with Firebase to fetch match data.

### PlayerComparisonActivity
- **Purpose**: Compares the statistics of two selected players.
- **Relationships**: 
    - Launched from `MatchTrackingActivity` or `MatchHistoryActivity` when two players are selected.

### NewMatchActivity
- **Purpose**: Facilitates the creation of a new match by allowing users to select teams.
- **Relationships**: 
    - Launches `MatchTrackingActivity` to record match data, which is then stored in Firestore.

### TeamComparisonActivity (Unused)
- **Purpose**: Compares statistics between two teams.
- **Relationships**: 
    - Currently unused but could be integrated with `TeamManagementActivity` in the future.

### Adapter
- `PlayerAdapter.kt`: Handles the display of player data in RecyclerViews.
- `PlayerStatsAdapter.kt`: Manages the display of player statistics in RecyclerViews.

### Data Class
- `Player.kt`: Defines the Player data class with properties such as name, stats, and Parcelable implementation.
- `Team.kt`: Defines the Team data class with properties like team name, players, and Parcelable implementation.

### Dialog
- `PlayerAddEditDialog.kt`: Custom dialog for adding or editing player details.
- `PlayerSelectionDialog.kt`: Custom dialog for selecting players for specific actions or comparisons.

---

### Custom Feature: Most Valuable Player (MVP) Tracking
A custom feature added to the app is automatic MVP tracking during a match. As players score goals or behinds, the app continuously calculates which player has the highest total points (goals worth 6, behinds worth 1) live and highlights them as the MVP in the Player Stats within Match Tracking and Match History.  

While this feature is fairly simple, it was chosen due to tight time constraints and fits naturally within the existing functionality of the app without introducing bugs or usability issues.  

In future development, the MVP system could be expanded to consider a wider range of stats (e.g., disposals, marks, tackles) with a weighted formula to better reflect overall player performance, not just scoring.

---

## Additional Notes
- The app uses Firebase Firestore for data storage and retrieval.
- View Binding is enabled for easier UI management.
- The app is optimized for Android 12 and above.
- The app's layout mostly uses Material3 components for customisability.

---

## License

This prototype is part of a university assessment and not licensed for commercial use. All original work © Joshua Crisford.