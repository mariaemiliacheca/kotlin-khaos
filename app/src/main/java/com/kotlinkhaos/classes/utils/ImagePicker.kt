package com.kotlinkhaos.classes.utils

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.setupImagePickerCallbacks(onImagePicked: (Uri?) -> Unit): Pair<ActivityResultLauncher<Intent>, ActivityResultLauncher<String>> {
    val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onImagePicked(result.data?.data)
            }
        }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery(pickImageLauncher)
            }
        }

    return Pair(pickImageLauncher, requestPermissionLauncher)
}

fun Fragment.openPictureGallery(
    pickImageLauncher: ActivityResultLauncher<Intent>,
    requestPermissionLauncher: ActivityResultLauncher<String>
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            requireContext(),
            READ_MEDIA_IMAGES
        ) -> {
            openGallery(pickImageLauncher)
        }

        else -> {
            requestPermissionLauncher.launch(READ_MEDIA_IMAGES)
        }
    }
}

private fun openGallery(launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_PICK).apply {
        type = "image/*"
    }
    launcher.launch(intent)
}
