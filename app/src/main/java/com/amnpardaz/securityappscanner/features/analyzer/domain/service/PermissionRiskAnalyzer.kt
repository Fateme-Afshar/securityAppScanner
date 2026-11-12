package com.amnpardaz.securityappscanner.features.analyzer.domain.service

import android.Manifest
import com.amnpardaz.securityappscanner.features.analyzer.data.model.PermissionInformation
import com.amnpardaz.securityappscanner.features.analyzer.data.model.RiskLevel

object PermissionRiskAnalyzer {

    private const val PERM_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    private const val PERM_MANAGE_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE"
    private const val PERM_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
    private const val PERM_USE_BIOMETRIC = "android.permission.USE_BIOMETRIC"

    private val highRiskPermissions = setOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        PERM_BACKGROUND_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
        Manifest.permission.REQUEST_INSTALL_PACKAGES,
        PERM_MANAGE_STORAGE,
        Manifest.permission.PACKAGE_USAGE_STATS,
        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
        Manifest.permission.BODY_SENSORS,
    )

    private val mediumRiskPermissions = setOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        PERM_POST_NOTIFICATIONS,
        PERM_USE_BIOMETRIC,
        Manifest.permission.CHANGE_WIFI_STATE,
    )

    fun analyze(name: String): PermissionInformation {
        val risk = when {
            name in highRiskPermissions -> RiskLevel.HIGH
            name in mediumRiskPermissions -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return PermissionInformation(
            name = name,
            riskLevel = risk,
            description = getDescription(name),
        )
    }

    private fun getDescription(name: String): String {
        return when (name) {
            Manifest.permission.CAMERA -> "Access to device camera"
            Manifest.permission.RECORD_AUDIO -> "Access to microphone for recording"
            Manifest.permission.READ_CONTACTS -> "Read sensitive contact data"
            Manifest.permission.WRITE_CONTACTS -> "Modify contact data"
            Manifest.permission.READ_SMS -> "Read private SMS messages"
            Manifest.permission.SEND_SMS -> "Send SMS messages"
            Manifest.permission.RECEIVE_SMS -> "Receive SMS messages & OTPs"
            Manifest.permission.READ_CALL_LOG -> "Access call history"
            Manifest.permission.WRITE_CALL_LOG -> "Modify call history"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Access precise GPS location"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Access approximate location"
            PERM_BACKGROUND_LOCATION -> "Track location in background"
            Manifest.permission.READ_PHONE_STATE -> "Access device IMEI & phone identity"
            Manifest.permission.SYSTEM_ALERT_WINDOW -> "Overlay screens on other apps (Tapjacking risk)"
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> "Full accessibility screen control & keylogging risk"
            Manifest.permission.REQUEST_INSTALL_PACKAGES -> "Install unknown app packages"
            PERM_MANAGE_STORAGE -> "Full unrestricted file system access"
            Manifest.permission.PACKAGE_USAGE_STATS -> "Monitor app usage statistics"
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> "Read incoming app notifications"
            Manifest.permission.BODY_SENSORS -> "Access fitness & body sensors"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Read external storage files"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Modify external storage files"
            Manifest.permission.INTERNET -> "Unrestricted network communication"
            else -> "Access to device resource"
        }
    }
}
