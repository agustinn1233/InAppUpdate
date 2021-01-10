## Documentation
https://developer.android.com/guide/playcore/in-app-updates

## Installation
Add this in your app's **build.gradle** file:
```
dependencies {
    ....
    implementation 'com.google.android.play:core-ktx:1.8.1'
    ....
}
```
## Test

-Now the last thing is how to test this, this is a tricky part, the following things for testing you need to be followed.
-I divided it into two parts.

## Part1
-Generate a signed APK for Production.
-Go to App-Internal-Sharing and upload the build over there, and share the link with the tester
-Now install that build with the shareable link.

## Part2
-Generate a signed APK for Production with another version code and version name, make sure the version which you upload earlier is lower than this.
-Go to App-Internal-Sharing and upload the build over there, and share the link with the tester.
-Now click to that link and don’t click to the Update button.
-Now just open the application which you installed earlier, you will get the update dialog.


