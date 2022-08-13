package com.life360.locationpermissiontestapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.life360.locationpermissiontestapp.databinding.ActivityMainBinding
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            updateStatusText()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openLocationPermissionSettings.setOnClickListener { openLocationPermissionSettings() }
        binding.buttonForegroundPermission.setOnClickListener { requestForegroundLocation() }
        binding.buttonBackgroundPermission.setOnClickListener { requestBackgroundLocation() }
    }

    override fun onResume() {
        super.onResume()

        updateStatusText()
    }

    private fun updateStatusText() {
        var html = ""

        if (isLocationPermissionGranted()) {
            html += getForegroundLocationPermissionMessage()
            html += "<br><br>"
            html += getBackgroundLocationMessage()
        } else {
            html += getForegroundLocationPermissionMessage()
            html += "<br><br>"
            html += "We are not checking for Background permission if Foreground Permission Denied"
        }

        if (isAndroidSOrAbove()) {
            html += "<br><br>"
            html += getApproximateLocationMessage()
        }

        binding.info.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun getBackgroundLocationMessage(): String {
        return if (isBackgroundLocationGranted()) {
            "Background Location (Allow All The Time) Enabled? - <b><font color='green'>TRUE</font></b>"
        } else {
            "Background Location (Allow All The Time) Enabled? - <b><font color='red'>FALSE</font></b>"
        }
    }

    private fun getForegroundLocationPermissionMessage(): String {
        return if (isLocationPermissionGranted()) {
            "Foreground Location Permission Enabled? - <b><font color='green'>TRUE</font></b>"
        } else {
            "Foreground Location Permission Enabled? - <b><font color='red'>FALSE</font></b>"
        }
    }

    private fun isBackgroundLocationGranted(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun getApproximateLocationMessage(): String {
        return if (isApproximateLocationGranted()) {
            "Precise Location Enabled? - <b><font color='red'>FALSE</font></b>"
        } else {
            "Precise Location Enabled? - <b><font color='green'>TRUE</font></b>"
        }
    }

    private fun isApproximateLocationGranted(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isAndroidSOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    private fun openLocationPermissionSettings() {
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + applicationContext.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        startActivity(i)
    }

    private fun requestBackgroundLocation() {
        if (isBackgroundLocationGranted()) {
            Toast.makeText(applicationContext,"Background Location Granted", Toast.LENGTH_SHORT).show()
            return
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            val builder =
                AlertDialog.Builder(this)
            builder.setTitle("This app needs background location access")
            builder.setMessage("Please grant location access so this app can detect beacons in the background.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            }
            builder.show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val builder =
                    AlertDialog.Builder(this)
                builder.setTitle("Functionality limited")
                builder.setMessage("Since background location access has not been granted. \n\nPlease go to Settings -> Applications -> Permissions and grant background location access to this app.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    // This will take the user to a page where they have to click twice to drill down to grant the permission
                    startActivity(intent)
                }
                builder.show()
            }
        }
    }

    private fun requestForegroundLocation() {
        if (isLocationPermissionGranted()) {
            Toast.makeText(applicationContext,"Foreground Location Granted", Toast.LENGTH_SHORT).show()
            return
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val builder =
                AlertDialog.Builder(this)
            builder.setTitle("This app needs foreground location access")
            builder.setMessage("Please grant location access so this app")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            builder.show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val builder =
                    AlertDialog.Builder(this)
                builder.setTitle("Functionality limited")
                builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background. \n\nPlease go to Settings -> Applications -> Permissions and grant background location access to this app.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    openLocationPermissionSettings()
                }
                builder.show()
            }
        }
    }
}
