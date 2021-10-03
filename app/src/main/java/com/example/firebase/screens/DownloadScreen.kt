package com.example.firebase.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun DownloadScreen(
    viewModel: FirebaseViewModel,
    firebaseReference: StorageReference,
    NetworkConnection: @Composable () -> Unit,
    isValidNetwork: Boolean
) {
    val localContext = LocalContext.current
    val downloadedImageUri by viewModel.downloadedImageUri.observeAsState()
    var downloadedTextFieldValue by remember { mutableStateOf("")}
    var downloadButtonState by remember { mutableStateOf(false)}

    NetworkConnection()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if(downloadedImageUri != null) {
                Image(painter = rememberImagePainter(data = downloadedImageUri), contentDescription = null)
            }
            else {
                Text(
                    text = "Download an image from Firebase storage!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(10.dp)
                )
            }

            AnimatedVisibility(visible = !downloadButtonState) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        enabled = isValidNetwork,
                        onClick = { downloadButtonState = true },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primaryVariant),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(text = "Download")
                    }
                }
            }
            AnimatedVisibility(visible = downloadButtonState) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = downloadedTextFieldValue,
                        onValueChange = {
                            downloadedTextFieldValue = it
                        },
                        label = { Text(text = "File name")},
                        modifier = Modifier.padding(2.dp)
                    )
                    Button(
                        enabled = downloadedTextFieldValue != "",
                        onClick = {
                            viewModel.downloadPhotoFromFirebase(
                                context = localContext,
                                firebaseReference = firebaseReference,
                                filename = downloadedTextFieldValue
                            )
                            downloadedTextFieldValue = ""
                            downloadButtonState = false
                        },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primaryVariant),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(text = "Go")
                    }
                }
            }
        }
    }
}