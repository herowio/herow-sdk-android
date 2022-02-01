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

>Warning:
>
>A token is displayed. You will only see it once. You need to save it somewhere because you will never be able to fetch it another time. If you lose or forget it you will need to generate a new token.


# Installation with Android Studio

- Create or open your project

- Into your build.gradle (project level), into all project > repositories section, add the following code:

```
maven {
    google()
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/herowio/herow-sdk-android")
    credentials {
        username = "YOUR_GITHUB_USERNAME"
        password = "YOUR_GITHUB_TOKEN"
    }
}
```

Replace YOUR_GITHUB_USERNAME with the username you usually used to connect to Github.
Replace YOUR_GITHUB_TOKEN by the generated token that you can only see once.

- Sync your project
- Go to your build.gradle (app level) and in your dependencies add the following code:

``` 
implementation 'io.herow.sdk:detection:7.2.2'
```

- Sync your project


# Configure the SDK

- Create a new class and extend it to the Application

```
import android.app.Application

class MainApplication: Application() { }

```

- You will need your SDK Access Key (SDK ID & Key) and your platform (PRE_PROD or PROD) type. Override the onCreate() method and configure your herowInitializer as follows:

```
override fun onCreate() {
        super.onCreate()

        val herowInitializer = HerowInitializer.getInstance(context)
        herowInitializer
            .configPlatform(HerowPlatform.PROD) // or HerowPlatform.PRE_PROD
            .configApp(sdkId = "YOUR_SDK_ID", sdkKey = "YOUR_SDK_KEY")

        herowInitializer.synchronize()
    }
```

>Note 1:
>
>The synchronize method allows the SDK set up with a configuration file downloaded from the Herow platform. The SDK will start the place detection process only when the file download is complete.
>
>This configuration file is saved in cache, and the SDK will check for updates at regular intervals.

>Note 2:
>
>The HerowInitializer allows you to configure your access to the HEROW platform. HEROW gives you access to the following environments:
>
>- PRE_PROD: pre-production environment used for tests
>- PROD: production environment used for release

>Warnings:
>
>- You will get one Access Key: This Access Key is composed of an SDK ID & an SDK Key and is used to configure your SDK.
>
>- Please make sure you use the right platform depending on your objective (test or release). Otherwise your SDK won't load/be synced with the right content.


# GDPR Opt-ins

The HEROW SDK has an in-built mandatory GDPR method.

**Update the opt-ins permissions**

Use these methods:

`herowInitializer.acceptOptin()` // Opt-in accepted

`herowInitializer.refuseOptin()` // Opt-in refused

Note: The HEROW SDK will only work if the GDPR opt-ins are given by the end-users.

# Location permissions

The HEROW SDK requires access to the Operating System permissions to work.

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

Regarding BACKGROUND_LOCATION, you now need to explicitely ask your user to modify his/her preferences in the app's settings. To do so, Google advices to prompt an AlertDialog to explain why you need the background location permission.

</br>

>Note 1:
>
>For Android 11, if you choose to ask for the background location permission and the user enables it, you will also benefit from the Android 11 native geofencing service.

<br />

>Note 2: 
>
>HEROW has an in-built **ClickAndCollect** method enabling you to perform temporary background location services with access to foreground permission only. More information in our **ClickAndCollect** section.


# Setting a Custom ID

To set a customID, make the following call as soon as the user logs in. You can set the customID before the synchronize() method, in your class extending Application.

`HerowInitializer.getInstance(context).setCustomId("YOUR_CUSTOM_ID")`

If the user logs out, you can use the removeCustomID() method. 

`HerowInitializer.getInstance(context).removeCustomID()`

>Note: 
>
>**Setting a customID is crucial if you want to reconcile your HEROW user data in your backend or with third-party partners**. The customID is used as cross-solution identification system.

# Setting a CustomUrl

To set a CustomUrl, make the following call.

`HerowInitializer.getInstance(context).setProdCustomURL("YOUR_CUSTOM_PROD_URL")`
`HerowInitializer.getInstance(context).setPreProdCustomURL("YOUR_CUSTOM_PREPROD_URL")`

To remove the custom url. 

`HerowInitializer.getInstance(context).removeCustomURL()`

To check the current url. 

`HerowInitializer.getInstance(context).getCurrentURL()`

# ClickAndCollect

To enable the HEROW SDK to temporarly continue tracking end-users location events (geofence detection or standard location events) happening in a Click & Collect context when the app is in the background (but not closed).

This **method is to be called during an active session (app opened)** which will enable the SDK to continue tracking the end-user when the app is put in background.

>Note 1:
>
>This method **only works if the end-user's runtime location permission is at least set to While in use**.
>
>From Android 11+ (API 30 and above), Google prevents apps from requesting the Background Location Permission upfront and collecting background location data before the user manually grants the background location permission is his/her app settings.

>Note 2:
>
>A Click & Collect context is defined by **specific app scenarios which legitimates a temporary use of the background location to perform a specific task** (ex: pickup scenarios for F&B where location is used to estimate order preparation lead time).

>Note 3: 
>
>By default, the Click & Collect background service will timeout after 2 hours. This is meant to preserve the initial objective of this feature to locate end-users in the background for Click & Collect scenarios and not provide continuous background tracking.

**How to start Click and Collect**

Call the following method to enable the background service:

`HerowInitializer.getInstance(context).launchClickAndCollect()`

**How to stop Click and Collect**

Call the following method to disable the background service:

`HerowInitializer.getInstance(context).stopClickAndCollect()`

# DeepLink
To implement deeplink from campaigns please follow this: 
[https://developer.android.com/training/app-links/deep-linking](https://developer.android.com/training/app-links/deep-linking)

# Debug mode

You can now follow the execution logs with this method.

`GlobalLogger.shared.debug()`

You can stop this mode with this command:

`GlobalLogger.shared.stopDebug()`

If you want to save these logs locally or on remote storage system, you can by implementing this interface: **ILogger** and register it on the GlobalLogger.
In this exemple HerowLogger is a class which implements the ILogger interface:

`GlobalLogger.shared.registerLogger(HerowLogger())`
