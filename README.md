<h1 align="center">Herow SDK for Android</h1>

<p align="center">
  <a href="https://android-arsenal.com/api?level=21">
	<img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/>
  </a>
</p>

# SDK Setup - GitHub Packages

In order to use our SDK you need to configure your Github account.

- Go to [https://github.com](url) and log in.
- In the top right of your screen, click on your profile and go to "Settings"
- In the left menu, go to "Developer Settings" and then go to "Personal access tokens"
- Click on the button "Generate new token". Choose a note and select in scopes "read:packages"
- Generate token

Warning:

A token is displayed. You will only see it once. You need to save it somewhere because you will never be able to fetch it another time. If you lose or forget it you will need to generate a new token.


# Installation with Android Studio

- Create or open your project
- Into your build.gradle (project level), into all project > repositories section, add the following code:

```
maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/herowio/herow-sdk-android")
    credentials {
        username = "YOUR_GITHUB_USERNAME"
        password = "YOUR_GITHUB_TOKEN"
    }
}
```

Replace YOUR_GITHUB_USERNAME with the username you usually use to connect to Github.
Replace YOUR_GITHUB_TOKEN by the generated token that you can only see once.

- Sync your project
- Go to your build.gradle (app level) and in your dependencies add the following code:

``` 
implementation 'io.herow.sdk:detection:7.1.0'
```

- Sync your project


# Configure the SDK

- Create a new class and make it extend Application

```
import android.app.Application

class MainApplication: Application() { }

```

- The SDK uses a WorkManager to make some call to our platform. In order for your application to avoid a "WorkManager is not initialized properly" error, your MainApplication class needs to implement Configuration.Provider as followed:

```
import androidx.work.Configuration

class MainApplication: Application(), Configuration.Provider { }

```

- It will ask you to override the getWorkManagerConfiguration() method. Use the following code:

```
override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
```


- You will need your SDK credentials and your platform. Override onCreate() method and configure your initialization as followed:

```
override fun onCreate() {
        super.onCreate()

        val herowInitializer = HerowInitializer.getInstance(context)
        herowInitializer
            .configPlatform(HerowPlatform.PROD) //or HerowPlatform.PRE_PROD
            .configApp(sdkId = "YOUR_SDK_ID", sdkKey = "YOUR_SDK_KEY")

        herowInitializer.synchronize()
    }
```

Note 1:

The synchronize method allows to set up the SDK with a configuration file downloaded from the Herow platform. The SDK will start the zone detection process only when the file download is complete.

This configuration file is saved in cache, and the SDK checks for updates at regular intervals.

Note 2:

The HerowInitializer allows you to configure your access to the HEROW platform. HEROW gives you access to one or several of the following environments:

- preProd: The pre-production platform of the HEROW platform
- prod: The production platform of the HEROW platform

Warning:

You will get one access key:

- An access key to use with our mobile SDK. This access key is composed of an SDK ID and SDK Key on Herow. Please make sure you use the good credentials to connect the SDK to the correct Herow Platform, otherwise your application won't be able to detect zones.


# GDPR Opt-ins

The HEROW SDK only works if the GDPR opt-in is given by the end-user. The SDK can share some informations as user datas.

**Update the opt-ins permissions**

- You can accept or refuse. Use these methods:

`herowInitializer.acceptOptin()`

`herowInitializer.refuseOptin()`


Note 1:

If the user refuses the optin the sdk will not work.
<br />


# Setting a Custom ID

To set a custom ID, make the following call as soon as the user logs in. You can set the custom ID before the synchronize() method, in your class extending Application.

`HerowInitializer.getInstance(context).setCustomId("YOUR_CUSTOM_ID")`

If the user logs out, you can use the removeCustomID() method. 

`HerowInitializer.getInstance(context).removeCustomID()`



# Click and Collect

To enable the HEROW SDK to continue tracking end-users location events (such as geofences' detection and position statements) happening in a Click & Collect context when the app is in the background.

These methods are to be called during an active session (app in Foreground) which will enable the SDK to continue tracking the end-user when the app is put in background.

Note 1:

This method only works if the end-user's runtime location permission is at least set to While in use.

Starting from Android 11, Google prevents apps requesting the Background Location Runtime Permission from collecting background location data before the user manually grants the background location permission. You need to specifically ask the user to enable this background permission directly in the settings of the phone.

Note 2:

By default, the Click & Collect background service will timeout after 2 hours. This is meant to preserve the initial objective of this feature to locate end-users in the background for Click & Collect scenarios and not provide continuous background tracking.



**How to start Click and Collect**

Call the following method to enable the background service:

`HerowInitializer.getInstance(context).launchClickAndCollect()`

**How to stop Click and Collect**

Call the following method to disable the background service:

`HerowInitializer.getInstance(context).stopClickAndCollect()`


# Location permissions

The sdk needs some permissions to work. To be able to take background position readings by activating the **ClickAndCollect** feature you will need the while-in-use permission. If you want to take readings permanently the always permissions will be required.

Before **Android 10** (API 28 and before) you need to ask for these permissions at runtime:

- android.Manifest.permission.ACCESS_FINE_LOCATION
- android.Manifest.permission.ACCESS_COARSE_LOCATION


For **Android 10** (API 29) you need to ask for these permissions at runtime:

- android.Manifest.permission.ACCESS_FINE_LOCATION
- android.Manifest.permission.ACCESS_COARSE_LOCATION
- android.Manifest.permission.ACCESS_BACKGROUND_LOCATION


Starting from **Android 11** (Api 30 and above) you need to ask for these permissions at runtime:

- android.Manifest.permission.ACCESS_FINE_LOCATION
- android.Manifest.permission.ACCESS_COARSE_LOCATION

You also need to specifically ask the user to enable Background location permission directly in the settings of the phone. To do so, Google advices to prompt an AlertDialog to explain why you need the background location permission.

Note 1:

For Android 11, if you choose to ask for the background location permission and the user enables it, you will also benefit from the Android 11 native geofencing service.
<br />

# Debug mode

You can now follow the execution logs with this method.

`GlobalLogger.shared.debug()`

You can stop this mode with this command:

`GlobalLogger.shared.stopDebug()`

If you want to save this logs locally or on remote storage system, you can by implementing this interface: **ILogger** and register it on the GlobalLogger.
In this exemple HerowLogger is a class which implements the ILogger interface:

`GlobalLogger.shared.registerLogger(HerowLogger())`
