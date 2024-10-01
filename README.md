To run a Gradle-based Android app with Java in Android Studio

  Open Android Studio:
    Launch Android Studio on your computer.
    Import the project:
      Click on "File" > "Open" or "Open an Existing Project"
      Navigate to your project's root directory and select it
      Click "OK" to open the project


  Wait for Gradle sync:
    Android Studio will automatically start syncing the project with Gradle. This process may take a few minutes, especially for the first time.
    Configure Android SDK:
    If you haven't set up the Android SDK yet:
      Go to "File" > "Project Structure" > "SDK Location"
      Set the path to your Android SDK or let Android Studio download and install it for you


  Check Gradle configuration:
    Open the build.gradle file in the root project folder
    Ensure that the Android Gradle plugin version and Gradle version are compatible
    If needed, update the versions in the buildscript block


  Sync project with Gradle files: 
    Click on "File" > "Sync Project with Gradle Files"
    Wait for the sync to complete


  Set up an Android Virtual Device (AVD) or connect a physical device:
    Go to "Tools" > "AVD Manager"
    Click "Create Virtual Device"
    Choose a device definition, select a system image, and configure the AVD
    Click "Finish" to create the AVD

  Build the project:
    Click on "Build" > "Make Project" or use the shortcut (Ctrl+F9 on Windows/Linux, Cmd+F9 on Mac)
    Wait for the build to complete successfully

  
  Run the app:
    Click on the "Run" button (green play icon) in the toolbar
    Or go to "Run" > "Run 'app'"
    Select the target device (AVD or physical device) if prompted


  Debug (if needed):
    Set breakpoints in your code by clicking on the left margin of the code editor
    Click on the "Debug" button (bug icon) instead of the "Run" button
    Use the debugging tools to step through your code and inspect variables


  View logs:
    Open the "Logcat" tab at the bottom of Android Studio to view app logs and debug information


  Make changes and hot reload:
    For minor changes, use "Apply Changes and Restart Activity" (lightning bolt icon) for faster deployment
    For major changes, you'll need to rebuild and rerun the app


  Clean and rebuild (if facing issues):
    Go to "Build" > "Clean Project"
    After cleaning, go to "Build" > "Rebuild Project"


