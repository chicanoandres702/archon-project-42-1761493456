package com.example.app.permissions

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

// Constants for permissions, enforced internal visibility
internal const val CAMERA_PERMISSION = Manifest.permission.CAMERA
internal const val STORAGE_READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
internal const val STORAGE_WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

/**
 * Defines the permissions required at runtime.
 * WRITE_EXTERNAL_STORAGE is conditionally included as it is scoped on newer APIs (Q+).
 */
internal val REQUIRED_PERMISSIONS = listOfNotNull(
    CAMERA_PERMISSION,
    STORAGE_READ_PERMISSION,
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) STORAGE_WRITE_PERMISSION else null
)

/**
 * Main Composable to handle requesting and checking required runtime permissions (Camera and Storage).
 * Uses standard Accompanist patterns for declarative permission management.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RequiredPermissionsHandler(
    onPermissionsGranted: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionsState = rememberMultiplePermissionsState(permissions = REQUIRED_PERMISSIONS)

    if (permissionsState.allPermissionsGranted) {
        onPermissionsGranted()
    } else {
        PermissionRequestContent(
            permissionsState = permissionsState,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequestContent(
    permissionsState: MultiplePermissionsState,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("The application needs Camera and Storage permissions to function correctly.")

        Spacer(Modifier.height(16.dp))

        // Display which specific permissions are pending
        val pendingPermissions = permissionsState.permissions.filter { !it.status.isGranted }
        
        pendingPermissions.forEach { pState ->
            val name = when (pState.permission) {
                CAMERA_PERMISSION -> "Camera Access"
                STORAGE_READ_PERMISSION -> "Storage Read Access"
                STORAGE_WRITE_PERMISSION -> "Storage Write Access"
                else -> "Unknown Permission"
            }
            Text(text = "- Requires $name", modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(24.dp))

        // Deferring the state read/action by passing a lambda to onClick
        Button(
            onClick = { 
                permissionsState.launchMultiplePermissionRequest()
            }
        ) {
            val requestText = if (permissionsState.shouldShowRationale) {
                "Show Rationale & Request Again"
            } else {
                "Request Permissions"
            }
            Text(requestText)
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (permissionsState.shouldShowRationale) {
            Text(
                "If permission is permanently denied, please enable it manually in settings.",
                Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}