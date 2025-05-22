# Android Aviation Checklist and Virtual Copilot App

## Project Overview

* **Project Title:** Android Aviation Checklist and Virtual Copilot App
* **Target Audience:** Pilots (General Aviation, potentially others)
* **Core Concept:** To create an interactive, digital checklist and virtual copilot Android application that enhances safety and efficiency during all phases of flight by replacing or augmenting traditional paper checklists with a dynamic, hands-free, and intelligent system.

## Key Functionality

* **Checklist Display and Interaction:**
    * Present digital checklists with various item types: tasks, notes, warnings, and information.
    * Support different checklist item types such as checkboxes, Yes/No, text input, pickers, labels, and rich content like images, videos, and links.
    * Allow users to navigate and mark checklist items as complete.
    * Implement different layout views like vertical scrolling and horizontal sections.
    * Visually indicate completion status for individual items, sections, and entire lists.
    * Highlight mandatory items that must be completed before proceeding.
    * Allow users to select and load specific aircraft checklists from their phone (sample data is in `assets/checklist.json`).
* **Voice Control:**
    * Enable hands-free interaction using voice commands.
    * Allow the app to read checklist items aloud (noted as a challenge).
    * Recognize user voice responses or commands to mark items as complete, skip items, navigate to specific sections, or query information.
* **Wake Word/Keyword Detection:**
    * Implement on-device keyword spotting using a pre-trained Google LiteRT model.
    * Continuously listen to audio from the device's microphone and process the audio data.
    * Use the liteRT audio model for the 'hey jarvis' wake word. When enabled in the GUI, it should listen for the wake word and then initiate native Speech-to-Text (STT) to navigate to a checklist.
    * Include the ability to "Transfer Learn" the 'hey jarvis' wake word to tune it to the user's voice. This will involve the user providing voice samples to refine the model's recognition for their specific voice.
    * Identify if a specific keyword is spoken using the LiteRT model.
    * Upon detection of a designated keyword, the app will trigger the native STT and a predefined action (e.g., mark item complete, search for the closet matching check list, provide flight information, etc.)
    * Designed for efficient, on-device machine learning for always-on audio processing with minimal latency.
* **User Experience Features:**
    * Optimize the interface for a cockpit environment with clear visuals and potentially a night mode.
    * Include a flight timer function.
    * Provide a history or logbook feature to review completed checklist sessions.

## Interaction Flow

* **Initial Flow:**
    * Upon the first app opening, request the user to "Transfer Learn" the wake word with their voice (allow skipping and doing this later).
    * Proceed to a main screen.
    * Allow users to import a JSON checklist file created from a website.
* **Typical Flow:**
    * User opens the app and selects an aircraft checklist.
    * The app displays a vertical list with the first item highlighted.
    * The "virtual copilot" voice reads the first item highlighted.
    * The user performs the action or verifies the condition.
    * If enabled in setting, the app will listen for the response using native STT.
    * The user marks the item complete via voice command by the expected response or "Check" or "ok" or presses the check button.
    * The app automatically moves to the next item reading it out loud.
    * This continues through sections and lists for the phases of flight.
    * Users can navigate manually, skip items (with or without confirmation), or jump to emergency procedures via voice or touch.
    * Users can jump to checklist by a horizontal bar at the bottom.

## Screen layout

* **Main screen:**
    * **Top Ribbon first level:** flight plan, local time, UTC time.
    * **Top Ribbon second level:** settings button.
    * **List of checklist:** A list of checklists. Ability to select new or return.
* **After selecting the checklist:**
    * **Top Ribbon first level:** flight plan, local time, UTC time.
    * **Bottom Ribbon (Checklist Screen):** Home, Check (larger), Skip (larger, greyed out if not permitted), Mic for listen again (change color if listening), Repeat item.
    * **Bottom Ribbon One Level Up (Checklist Selection):** Grey out checklists requiring previous ones to be completed. Different colors for normal, abnormal, emergency, reference checklists.
    * **Top Ribbon first level:** Hamburger menu for settings, flight plan, local time, UTC time.
    * **Top Ribbon second level:** Indicate the current phase of flight.

## Technical Requirements for Wake Word Detection

* **Android Development Environment:** Android Studio with the latest SDK.
* **Programming Language:** Kotlin
* **Minimum Android Version:** API level 34 or higher.
* **TensorFlow Lite Library:** Include the necessary LiteRT dependencies for Android using the guide from https://ai.google.dev/edge/mediapipe/solutions/setup_android
* **Audio Recording APIs:** Utilize Android's `AudioRecord` class for capturing microphone input.
* **Permissions:** Declare `<uses-permission android:name="android.permission.RECORD_AUDIO" />` in the AndroidManifest.xml and handle runtime permission requests.
* **LiteRT Model:** A pre-trained or custom-trained LiteRT model specifically designed for keyword spotting or audio event classification, optimized for on-device inference. Model is located in `assets/jarvis_model.tflite`

## High-Level Architecture for Wake Word Detection

* **User Interface (Activity/Fragment):** Provides controls to start and stop the keyword spotting service and display feedback to the user.
* **Audio Capture Module:**
    * Uses `AudioRecord` to access the microphone.
    * Manages audio recording parameters (sample rate, channel configuration, etc.) based on the LiteRT model's requirements.
    * Handles necessary runtime permissions for audio recording.
* **Audio Preprocessing Module:**
    * Receives raw audio data from the Audio Capture Module.
    * Implement algorithms to transform the audio data into the input format expected by the LiteRT model (e.g., generating spectrograms, MFCCs).
* **LiteRT Inference Module:**
    * Loads the LiteRT model (.tflite file) from the app's assets folder.
    * Initializes and configures the LiteRT interpreter.
    * Feeds the preprocessed audio data (as tensors) to the interpreter for inference.
    * Retrieves the output tensors containing classification probabilities or scores.
* **Keyword Detection Module:**
    * Analyzes the output from the LiteRT Inference Module.
    * Applies thresholds to the output scores to determine if a keyword has been detected.
    * Implements logic for handling consecutive detections or smoothing results over time if needed.
* **Event Handling Module:**
    * Receives detection events from the Keyword Detection Module.
    * Triggers the predefined action associated with the detected keyword.
* **Background Service (Optional but Recommended for Continuous Listening):**
    * A Service (potentially a ForegroundService for newer Android versions) to manage the audio capture, preprocessing, and inference even when the app's UI is not visible.
    * Communicates results back to the UI if the app is in the foreground.

## Workflow for Wake Word Detection

1.  The user starts the keyword spotting feature via the UI.
2.  The app requests audio recording permission if not already granted.
3.  The Audio Capture Module starts recording audio from the microphone.
4.  The Audio Preprocessing Module continuously processes the incoming audio data.
5.  The preprocessed data is passed to the LiteRT Inference Module.
6.  The LiteRT model runs inference on the data.
7.  The Keyword Detection Module analyzes the model's output.
8.  If a keyword is detected with sufficient confidence, the Event Handling Module is notified.
9.  The Event Handling Module performs the designated action.
10. This process repeats until the user stops the keyword spotting feature.

## Future

* Add notification if altitude state is changing.
* Implement reading the checklist aloud and allowing screen taps to pause reading and check items.
* Add airport information, GPS information/geo tracking, Top of Climb (TOC), Top of Descent (TOD), and flight level.
* Add history information, possibly with tags for geo tracking.
* Start the flight timer when speed exceeds a specified value in the settings.
* Consider robust error handling for data loading, voice recognition failures, and unexpected states.
* Implement unit and integration testing, especially for critical features like wake word detection and checklist logic.

##################

### Stage 1: Setting up the Basic Android Project Structure

This stage is foundational. Breaking it down can help ensure all initial configurations are correctly handled.

* **Priority:** High - Essential for starting development.
* **Basic UI Shell:**
    * Design the initial main screen layout, including the top ribbons (flight plan, time) and the area for listing checklists as described in "Screen layout".
    * Set up basic navigation between initial screens (if any beyond the main screen are immediately necessary).
* **Permissions:**
    * Declare the `<uses-permission android:name="android.permission.RECORD_AUDIO" />` permission in the `AndroidManifest.xml`.
    * Implement the runtime permission request flow for audio recording.
* **Asset Integration (Initial):**
    * Include the sample checklists `cl_*.json` in the `assets` folder.

### Stage 2: Implementing the "Checklist Display and Interaction" Feature

This stage focuses on the core functionality of displaying and interacting with checklists.

* **Priority:** High - Forms the core user interaction.
* **Core Dependencies:**
    * Integrate the TensorFlow Lite library for Android. Follow the setup guide from the provided Google AI Edge link: https://ai.google.dev/edge/mediapipe/solutions/setup_android.
    * Add any other essential libraries you anticipate needing early on (e.g., for UI elements, navigation).
* **Data Loading and Parsing:**
    * **Priority:** Critical - This must work before UI rendering.
    * Implement logic to load and parse the `checklist.json` file from the assets.
    * Develop data models (e.g., Kotlin data classes) to represent checklists, sections, and individual checklist items with their various types (tasks, notes, warnings, information, checkboxes, Yes/No, text input, pickers, labels).
* **Checklist UI Rendering:**
    * **Priority:** Critical - Displays the loaded data.
    * Develop the UI to display the parsed checklist data.
    * Implement different layout views like vertical scrolling and horizontal sections for checklist items.
    * Visually indicate completion status for items, sections, and lists.
    * Highlight mandatory items.
* **Basic Interaction:**
    * **Priority:** Critical - Enables basic user control.
    * Allow users to select a specific aircraft checklist.
    * Enable users to manually mark checklist items as complete (e.g., via button press).
    * Implement the bottom ribbon for the checklist screen with "Home," "Check," "Skip," "Mic," and "Repeat item" buttons.
    * Implement the checklist selection ribbon, greying out checklists that require prerequisites and using different colors for checklist types.
* **Rich Content (Initial Pass - Optional, can be deferred):**
    * **Priority:** Medium - Can be enhanced later.
    * If simpler item types are working, you can start exploring how to integrate support for rich content like images, videos, and links within checklist items. This might be a good point to assess token usage before adding more complex features.

### Stage 3: Tackling the "Wake Word Detection" Functionality

This is a complex feature involving machine learning and audio processing. Breaking it down is crucial.

* **Priority:** High - Core differentiator of the app.
* **Audio Capture Module:**
    * **Priority:** Critical - Foundation for audio processing.
    * Implement the `AudioRecord` class to access the microphone.
    * Manage audio recording parameters (sample rate, channel configuration) according to the LiteRT model's requirements.
* **LiteRT Model Loading and Initialization:**
    * **Priority:** Critical - Enables ML inference.
    * Implement the logic to load the `jarvis_model.tflite` model from assets.
    * Initialize and configure the TensorFlow Lite interpreter.
* **Audio Preprocessing Module (Stub/Basic):**
    * **Priority:** High - Required input format for the model.
    * Initially, you might focus on getting raw audio data to the model.
    * Later, implement the necessary algorithms to transform audio data into the input format expected by the LiteRT model (e.g., spectrograms, MFCCs). This step can be iterative.
* **LiteRT Inference Module:**
    * **Priority:** High - Executes the ML model.
    * Feed preprocessed audio data (as tensors) to the interpreter.
    * Retrieve output tensors.
* **Keyword Detection Module (Basic):**
    * **Priority:** High - Core detection logic.
    * Analyze the model's output.
    * Implement a basic thresholding mechanism to determine if the "hey jarvis" wake word has been detected.
* **Event Handling (Basic):**
    * **Priority:** High - Verifies detection.
    * Upon wake word detection, trigger a simple action (e.g., log a message, display a toast) to verify functionality.
* **UI for Wake Word Control:**
    * **Priority:** Medium - User control for the feature.
    * Add UI elements to start and stop the keyword spotting service.
* **Background Service (Iterative Improvement):**
    * **Priority:** Medium - Improves user experience for continuous listening.
    * Once basic detection works, consider moving the audio capture, preprocessing, and inference to a background Service (potentially a ForegroundService) for continuous listening. This will likely require careful management of resources and battery life.
* **Native Speech-to-Text (STT) Integration:**
    * **Priority:** High - Enables voice commands post-wake word.
    * After wake word detection is reliable, integrate native STT to process commands following the wake word.
* **Transfer Learning (Advanced - Likely Later):**
    * **Priority:** Low - Enhancement for personalization.
    * The ability to "Transfer Learn" the 'hey jarvis' wake word to tune it to the user's voice is a more advanced feature. This would likely come after the core wake word detection is functional and stable. This process would involve the user providing multiple voice samples of the wake word, which would then be used to fine-tune the LiteRT model's recognition for their unique voice characteristics.

Remember to consult the "High-Level Architecture for Wake Word Detection" and "Workflow for Wake Word Detection" sections in your `jarvisDescription.md` document for detailed guidance on that specific feature.