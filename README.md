# [Addressbook](https://github.com/dredwardhyde/addressbook) Android Client Application

[![medium](https://aleen42.github.io/badges/src/medium.svg)](https://medium.com/geekculture/how-to-make-a-login-activity-with-biometrics-support-on-android-62185f19cda1)  [![medium](https://aleen42.github.io/badges/src/medium.svg)](https://medium.com/geekculture/how-to-build-sign-and-publish-android-application-using-github-actions-aa6346679254)


### Features

- **[Biometrics support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/LoginActivity.kt#L338)**
- **[Instrumentation Testing with Espresso](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/androidTest/kotlin/com/deepschneider/addressbook/WorkflowTest.kt)**
- **[Dark/Light Theme support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/res/values/themes.xml) with [Dynamic Switching](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/LoginActivity.kt#L60)**
- **[Dynamic Dark/Light Icon Switching](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/LoginActivity.kt#L99)**
- **[Material Components](https://github.com/dredwardhyde/addressbook-android-app#demo)**
- **[Rich Text Editor support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/res/layouts/activities/layout/activity_create_or_edit_person.xml#L179)**
- **[User Certificates support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/res/xml/network_security_config.xml#L5)**
- **[Building](https://github.com/dredwardhyde/addressbook-android-app/blob/master/.github/workflows/android.yml#L31), [Signing](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/build.gradle#L20) and [Publishing Release](https://github.com/dredwardhyde/addressbook-android-app/blob/master/.github/workflows/android.yml#L44) using Github Actions**
- **[Custom Requests with JWT Authentication](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/network/FilteredListRequest.kt) using [Android Volley](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/AbstractListActivity.kt#L96)**
- **[Server Side Pagination and Filtering](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/src/main/kotlin/com/deepschneider/addressbook/activities/OrganizationsActivity.kt#L82)**
- **[ProGuard support](https://github.com/dredwardhyde/addressbook-android-app/blob/master/app/proguard-rules.pro)**

### Light Theme
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="70"/>  
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/screenshots/all_panels_light.png" width="1000"/>  

### Dark Theme
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher_dark.png" width="70"/>  
<img src="https://raw.githubusercontent.com/dredwardhyde/addressbook-android-app/master/screenshots/all_panels_dark.png" width="1000"/>  

### Demo

https://user-images.githubusercontent.com/8986329/218852948-db444a83-367f-4f31-aa33-d28fd7091767.mp4

https://user-images.githubusercontent.com/8986329/218852961-8bb59d63-0a49-45c4-82e7-2dc669b9dc1b.mp4

https://user-images.githubusercontent.com/8986329/218852985-eae19aa3-a17a-4ba0-9d41-b72773075fbd.mp4
