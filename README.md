Device Location Tracker
Device Location Tracker is an Android application designed to continuously monitor the device's geographical location and send the latitude and longitude to a server. The application efficiently sends the data every 30 seconds and handles various scenarios like loss of internet connection. It is specifically tailored to work with a WampServer, utilizing PHP for server-side operations.

Features
-Real-time Location Monitoring: Tracks the device's latitude and longitude and sends them to the server every 30 seconds.
-Robust Server Interaction: Designed to work with WampServer, featuring PHP scripts for database connectivity.
-Local Database Handling: In case of loss of internet connection, all data will be saved locally on the device and sent to the server when the connection is restored.
-Server-Side Scripts: Includes "dbConnect.php" for handling database connections and "dbFunctions.php" for additional database functionalities.

Requirements
-Android device
-WampServer running

Installation
-Clone the Repository: [https://github.com/username/DeviceLocationTracker.git](https://github.com/D-tube/Android-Tracker.git)
-Set Up WampServer: Ensure that WampServer is running and PHP is configured properly.
-copy the php files to www folder in the wampserver directory. 
-replace the URL in the realtimeService with your local ip address.
-Build and Run: Build the application and run it on an Android device or emulator.

Usage
After installation, the application will automatically start tracking the device's location and send the data to the server every 30 seconds. In case of loss of internet connectivity, it will store the data locally and sync it back once the connection is restored.

Contribution
If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.
