package com.example.firebase.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.firebase.FirebaseViewModel
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun UploadScreen(
    selectImageLauncher: ActivityResultLauncher<String>,
    viewModel: FirebaseViewModel,
    firebaseReference: StorageReference,
    NetworkConnection: @Composable () -> Unit,
    isValidNetwork: Boolean
) {
    val localContext = LocalContext.current
    val imageUri by viewModel.imageUriState.observeAsState()
    var textFieldValue by remember { mutableStateOf("") }


    NetworkConnection()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (imageUri != null) {
                Image(painter = rememberImagePainter(data = imageUri), contentDescription = null)
            } else {
                Text(
                    text = "Choose an image from gallery!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(10.dp)
                )
            }
            AnimatedVisibility(visible = imageUri != null) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                    },
                    label = { Text(text = "File name") },
                    modifier = Modifier.padding(2.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { selectImageLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primaryVariant),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(text = "Gallery")
                }
                Button(
                    enabled = textFieldValue != "" && isValidNetwork,
                    onClick = {
                        viewModel.uploadPhotoToFirebase(
                            context = localContext,
                            firebaseReference = firebaseReference,
                            filename = textFieldValue
                        )

                        textFieldValue = ""


                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primaryVariant),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(text = "Upload")
                }
            }
        }
    }
}
