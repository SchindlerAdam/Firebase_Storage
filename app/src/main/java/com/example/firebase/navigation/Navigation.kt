package com.example.firebase.navigation

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.annotation.ExperimentalCoilApi
import com.example.firebase.FirebaseViewModel
import com.example.firebase.screens.CollectionScreen
import com.example.firebase.screens.DownloadScreen
import com.example.firebase.screens.UploadScreen
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun Navigation(
    navController: NavHostController,
    viewModel: FirebaseViewModel,
    firebaseReference: StorageReference,
    selectImageLauncher: ActivityResultLauncher<String>,
    networkConnection: Boolean,
    updateOrRequestPermission: () -> Unit,
    writePermissionGranted: Boolean,
    savePhotoToExternalStorage: (Context, String, String) -> Boolean
) {

    NavHost(navController = navController, startDestination = Screens.UploadScreen.route) {

        composable(route = Screens.UploadScreen.route) {
            UploadScreen(
                selectImageLauncher = selectImageLauncher,
                viewModel = viewModel,
                firebaseReference = firebaseReference,
                NetworkConnection = { NetworkErrorMessage(networkConnection = networkConnection) },
                isValidNetwork = networkConnection
            )
        }

        composable(route = Screens.DownloadScreen.route) {
            DownloadScreen(
                viewModel = viewModel,
                firebaseReference = firebaseReference,
                NetworkConnection = { NetworkErrorMessage(networkConnection = networkConnection) },
                isValidNetwork = networkConnection
            )
        }

        composable(route = Screens.CollectionScreen.route) {
            CollectionScreen(
                viewModel = viewModel,
                firebaseReference = firebaseReference,
                updateOrRequestPermission = updateOrRequestPermission,
                writePermissionGranted = writePermissionGranted,
                savePhotoToExternalStorage = savePhotoToExternalStorage
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun NetworkErrorMessage(
    networkConnection: Boolean
) {
    var networkConnectionState by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = !networkConnection) {
        if(!networkConnection) {
            networkConnectionState = true
            delay(3000L)
            networkConnectionState = false
        } else {
            showErrorMessage = false
            delay(3000L)
            showErrorMessage = true
        }
    }

    AnimatedVisibility(
        visible = networkConnectionState && showErrorMessage,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = {fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(4.dp)
                .background(MaterialTheme.colors.error)
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "no connection",
                tint = MaterialTheme.colors.onError,
                modifier = Modifier.padding(2.dp)
            )
            Text(
                text = "No internet connection!",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onError,
                modifier = Modifier.padding(2.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = !networkConnectionState && !showErrorMessage,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = {fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(4.dp)
                .background(MaterialTheme.colors.secondaryVariant)
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "connected",
                tint = MaterialTheme.colors.onSecondary,
                modifier = Modifier.padding(2.dp)
            )
            Text(
                text = "Connected!",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}