# [Addressbook](https://github.com/dredwardhyde/addressbook) Android Client Application

[![medium](https://aleen42.github.io/badges/src/medium.svg)](https://medium.com/geekculture/how-to-make-a-login-activity-with-biometrics-support-on-android-62185f19cda1)  [![medium](https://aleen42.github.io/badges/src/medium.svg)](https://medium.com/geekculture/how-to-build-sign-and-publish-android-application-using-github-actions-aa6346679254)
[![Android CI](https://github.com/dredwardhyde/addressbook-android-app/actions/workflows/android.yml/badge.svg)](https://github.com/dredwardhyde/addressbook-android-app/actions/workflows/android.yml)

### Features

- **[Biometrics support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/LoginActivity.kt#L328)**
- **[Instrumentation testing with Espresso](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/androidTest/kotlin/com/deepschneider/addressbook/WorkflowTest.kt)**
- **[Dark/Light Theme support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/res/values/themes.xml) with [dynamic switching](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/LoginActivity.kt#L60)**
- **[Dynamic Dark/Light icon switching](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/LoginActivity.kt#L106)**
- **[Material components](https://github.com/dredwardhyde/addressbook-android-app#demo)**
- **[Rich Text Editor support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/res/layouts/activities/layout/activity_create_or_edit_person.xml#L179)**  
- **[User certificates support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/res/xml/network_security_config.xml#L5)**  
- **[Building](https://github.com/dredwardhyde/addressbook-android-app/blob/master/.github/workflows/android.yml#L31), [signing](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/build.gradle#L20) and [publishing release](https://github.com/dredwardhyde/addressbook-android-app/blob/master/.github/workflows/android.yml#L44) using Github Actions**  
- **[Custom requests with JWT authentication](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/network/FilteredListRequest.kt) using [Android Volley](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/AbstractListActivity.kt#L100)**  
- **[Server-side pagination and filtering](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/OrganizationsActivity.kt#L86)**  
- **[Custom list animation](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/AbstractListActivity.kt#L119)**  
- **[Custom gesture recognition](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/listeners/OnSwipeTouchListener.kt)**  
- **[ProGuard support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/proguard-rules.pro)**  

### Light Theme
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="70"/>  
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/screenshots/all_panels_light.png" width="1000"/>  

### Dark Theme
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher_dark.png" width="70"/>  
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/screenshots/all_panels_dark.png" width="1000"/>  

### Demo

https://user-images.githubusercontent.com/8986329/236672505-e448ab4b-4c9f-4f2b-951e-eea0f523ac69.mp4

https://user-images.githubusercontent.com/8986329/236672510-30881cfe-eedc-4505-a732-48f135dc636b.mp4

