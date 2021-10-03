package com.example.firebase.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.firebase.FirebaseViewModel
import com.example.firebase.R
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay

private const val SplashWaitTime: Long = 2000

@Composable
fun LandingScreen(
    firebaseReference: StorageReference,
    viewModel: FirebaseViewModel,
    onTimeout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {

        val currentOnTimeout by rememberUpdatedState(newValue = onTimeout)

        LaunchedEffect(true) {
            viewModel.downloadAllPhotoFromFirebase(firebaseReference = firebaseReference)
            Log.d("Images", "${viewModel.allImage.value?.size}")
            delay(SplashWaitTime)
            currentOnTimeout()
        }
        Image(painterResource(id = R.drawable.firebase), contentDescription = null)
    }
}