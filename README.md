Spotz Android SDK
=================

[Spotz](todo) is a user engagement platform that utilizes Bluetooth Low Energy. You can create any 'Spot' you want - an exhibit, a room, an event, or even an entire street. The Spotz Android SDK allows your Android app to detect when it is in range of your Spotz and receive associated metadata - e.g. detailed information about an exhibit, promotional offers, media, it can be anything!

Changelog
=========

**1.3.0**
* Initial public release.

What does the sample app do?
============================

The app simply tells you if you are in range, or out of range of a Spot.

When a Spot is in range, the app will also display any data associated with that Spot.

How to run the sample app
=========================

  1. Import the project:
    
    If you're using Android Studio, clone the repository, and then simply open the project.
    Note you need to have internet connection for access public libraries.

    If you're using Eclipse, clone the repository, then in your workspace do File -> Import -> General -> "Existing Project into Workspace".
    
  2. Define a Spot using the [Spotz console](todo). Don't forget to add a beacon to your Spot. If you don't have a real beacon, don't worry, you can use the [iBeacon Toolkit](todo)!
    
  3. Insert your Spotz application ID and client key into MainActivity.java. Spotz application ID and client key is shown in the Spotz console under your application. Be sure to use the Android client key:

        ...
        Spotz.getInstance().initialize(this,
                "your-application-id", // Your application ID goes here
                "your-client-key", // Your client key goes here
        ...
  

  4. Run it! (Note: you need a device running Android 4.3 or newer)


How to add the SDK to your own Project
======================================

To use the SDK library, both the library and your project must be compiled with Android 4.3 (API level 18) or newer, as it utilizes Bluetooth Low Energy.

If you're a Gradle user you can easily include the library by specifying it as
a dependency in your build.gradle script:

    allprojects {
        repositories {
            maven { url "http://localz.github.io/mvn-repo" }
            mavenCentral()
        }
    }
    ...
    dependencies {
        ...
        compile 'com.localz.spotz.sdk:spotz-sdk-android:1.3.0@aar'
        compile 'com.localz.spotz.sdk:spotz-sdk-api:1.1.0'
        compile 'com.localz.proximity.ble:ble-sdk-android:1.1.1@aar'
        ...
    }

If you're a Maven user you can include the library in your pom.xml:

    ...
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-android</artifactId>
      <version>1.3.0</version>
      <type>aar</type>
    </dependency>
    
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-api</artifactId>
      <version>1.1.0</version>
    </dependency>
    
    <dependency>
      <groupId>com.localz.proximity.ble</groupId>
      <artifactId>ble-sdk-android</artifactId>
      <version>1.1.1</version>
      <type>aar</type>
    </dependency>

    ...

    <repositories>
        ...
        <repository>
            <id>Localz mvn repository</id>
            <url>http://localz.github.io/mvn-repo</url>
        </repository>
        ...
    </repositories>
    ...

Otherwise, if you are old school, you can manually copy all the JARs in the libs folder and add them to your project's dependencies.
You libs folder will have at least the following jars:

- ble-sdk-android-1.1.1.jar
- google-http-client-1.19.0.jar
- google-http-client-gson-1.19.0.jar
- gson-2.3.jar
- spotz-sdk-android-1.3.0.jar
- spotz-sdk-api-1.1.0.jar

How to use the SDK
==================

*Refer to the sample app code for a working implementation of the SDK.*

###Initialize the SDK

  1. Ensure your AndroidManifest.xml has these permissions:

        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

  2. Define the following service in your AndroidManifest.xml:

        <service android:name="com.localz.proximity.ble.services.BleHeartbeat" />
        
  3. Initialize the SDK by providing your application ID and client key (as shown on Spotz console):
  
        Spotz.getInstance().initialize(context, // Your context
                "your-application-id",          // Your application ID goes here
                "your-client-key",              // Your client key goes here
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        // Now that we're initialized, we can start scanning for Spotz here 
                    }
                }
        );
  
Your project is now ready to start using the Spotz SDK!

---

###Scan for Spotz

  To start scanning for Spotz, use one of these:
  
        // Normal scanning - ideal for general use 
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.NORMAL);

        // Eager scanning - for when fast Spotz engagement response is required, or if devices are expected to move in and out of range in short time
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.EAGER);
        
        // Passive scanning - use if battery conservation is more important than engagement, or if devices are expected to remain in your Spotz for longer periods
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.PASSIVE);
        
        // Customize your own scan parameters
        // scanIntervalMs - millisecs between each scan
        // scanDurationMs - millisecs to scan for
        Spotz.getInstance().startScanningForSpotz(context, scanIntervalMs, scanDurationMs);
  
  To stop scanning for Spotz:
  
        Spotz.getInstance().stopScanningBeacons(context);

  To conserve battery, always stop scanning when not needed.

---

###Listen for Events
  
#### On Spot Enter

  To listen for when the device enters a Spot, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ_ON_SPOT_ENTER</code> e.g. <code>com.foo.myapp.SPOTZ_ON_SPOT_ENTER</code>
    You can find your package name in AndroidManifest.xml file:

        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.foo.myapp" >
        
  Here's an example (don't forget to unregister your BroadcastReceiver when no longer needed):

        BroadcastReceiver enteredSpotBroadcastReceiver = new OnEnteredSpotBroadcastReceiver();
        registerReceiver(enteredSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_ENTER"));

        ...

        public class OnEnteredSpotBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
                
                // Do something with the Spot here!    
            }
        }

  Or if you prefer, you can define your BroadcastReceiver in AndroidManifest.xml:

        <receiver android:name="com.foo.app.services.OnEnteredSpotBroadcastReceiver" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SPOT_ENTER" />
            </intent-filter>
        </receiver>
        
#### On Spot Exit
        
  To listen for when the device exits a Spot, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ_ON_SPOT_EXIT</code> e.g. <code>com.foo.myapp.SPOTZ_ON_SPOT_EXIT</code>

  Here's an example (don't forget to unregister your BroadcastReceiver when no longer needed):

        BroadcastReceiver exitedSpotBroadcastReceiver = new OnExitedSpotBroadcastReceiver();
        registerReceiver(exitedSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_EXIT"));

        ...

        public class OnExitedSpotBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
    
                // Do something with the Spot here!
            }
        }

  Or if you prefer, here is the AndroidManifest.xml version:

        <receiver android:name="com.foo.app.services.OnExitedSpotBroadcastReceiver" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SPOT_EXIT" />
            </intent-filter>
        </receiver>

Contribution
============

For bugs, feature requests, or other questions, [file an issue](https://github.com/localz/spotz-sdk-android/issues/new).

License
=======

Copyright 2014 Localz Pty Ltd

 
