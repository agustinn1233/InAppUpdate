package com.github.agustinf1233.inappupdate

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity(), InstallStateUpdatedListener {

    // Official Documentation
    // https://developer.android.com/guide/playcore/in-app-updates


    // For testing
    /*:
    Now the last thing is how to test this, this is a tricky part, the following things for testing you need to be followed.
    I divided it into two parts.

    Part1
    Generate a signed APK for Production.
    Go to App-Internal-Sharing and upload the build over there, and share the link with the tester
    Now install that build with the shareable link.

    Part2
    Generate a signed APK for Production with another version code and version name, make sure the version which you upload earlier is lower than this.
    Go to App-Internal-Sharing and upload the build over there, and share the link with the tester.
    Now click to that link and don’t click to the Update button.
    Now just open the application which you installed earlier, you will get the update dialog.

     */

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        checkUpdateViaGooglePlay()
        super.onCreate(savedInstanceState, persistentState)
    }

    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(this).also { it.registerListener(this) }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= 21) {
            appUpdateManager.unregisterListener(this)
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 21) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                // If the update is downloaded but not installed, notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackBarForCompleteUpdate()
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, REQUEST_CODE_UPDATE_APP)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED && requestCode == REQUEST_CODE_UPDATE_APP) {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStateUpdate(state: InstallState) {
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> popupSnackBarForCompleteUpdate()
            InstallStatus.REQUIRES_UI_INTENT -> {
                Snackbar.make(findViewById(R.id.textViewLog),
                        "To perform the installation, a Play Store UI flow needs to be started.",
                        Snackbar.LENGTH_LONG
                ).show()
            }
            else -> {
                val stateString = when (state.installStatus()) {
                    InstallStatus.FAILED -> "failed"
                    InstallStatus.PENDING -> "pending"
                    InstallStatus.DOWNLOADING -> "downloading"
                    InstallStatus.INSTALLING -> "installing"
                    InstallStatus.INSTALLED -> "installed"
                    InstallStatus.CANCELED -> "canceled"
                    else -> null
                }
                if (stateString != null) {
                    Snackbar.make(findViewById(R.id.textViewLog),
                            "An update is $stateString.",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(findViewById(R.id.textViewLog),
                "An update is ready to install.",
                Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("INSTALL") { appUpdateManager.completeUpdate() }
            show()
        }
    }

    /**
     * checkUpdateViaGooglePlay
     * Requires android version >= 5
     */
    @RequiresApi(21)
    fun checkUpdateViaGooglePlay() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo, AppUpdateType.FLEXIBLE, this, REQUEST_CODE_UPDATE_APP)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo, AppUpdateType.IMMEDIATE, this, REQUEST_CODE_UPDATE_APP)
                    }
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    Toast.makeText(this, R.string.no_updates_found, Toast.LENGTH_SHORT).show()
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    TODO()
                }
                UpdateAvailability.UNKNOWN -> {
                    TODO()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, R.string.error_check_update, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val REQUEST_CODE_UPDATE_APP = 8
    }
}