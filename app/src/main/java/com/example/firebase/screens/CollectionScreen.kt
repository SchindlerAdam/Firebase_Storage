package com.example.firebase.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.example.firebase.FirebaseViewModel
import com.example.firebase.ImageItem
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun CollectionScreen(
    viewModel: FirebaseViewModel,
    firebaseReference: StorageReference,
    updateOrRequestPermission: () -> Unit,
    writePermissionGranted: Boolean,
    savePhotoToExternalStorage: (Context, String, String) -> Boolean

) {
    val lazyListState = rememberLazyListState()
    val lazyItems: List<ImageItem> by viewModel.allImage.observeAsState(listOf())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if(lazyItems.isNullOrEmpty()) {
            Text(
                text = "No downloaded image",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(lazyItems.size) { index ->
                    LazyItem(
                        imageItem = lazyItems[index],
                        onDeleteItem = {
                            viewModel.deletePhotoFromFirebase(
                                context = context,
                                firebaseReference = firebaseReference,
                                filename = it
                            )
                        },
                        updateOrRequestPermission = updateOrRequestPermission,
                        writePermissionGranted = writePermissionGranted,
                        savePhotoToExternalStorage = savePhotoToExternalStorage
                    )
                }
            }
        }
        val showListUpButtonState = lazyListState.firstVisibleItemIndex > 0
        AnimatedVisibility(
            visible = showListUpButtonState,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ScrollToTopButton(
                listState = lazyListState,
                coroutineScope = scope
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun LazyItem(
    imageItem: ImageItem,
    onDeleteItem: (fileName: String) -> Unit,
    updateOrRequestPermission: () -> Unit,
    writePermissionGranted: Boolean,
    savePhotoToExternalStorage: (Context, String, String) -> Boolean
) {
    val uri = imageItem.uri
    val name = imageItem.name
    val painter = rememberImagePainter(data = uri)


    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(20.dp)
    ) {

        LazyItemImage(
            painter = painter,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SaveImageToExternalStorage(
                name = name,
                uri = uri,
                writePermissionGranted = writePermissionGranted,
                updateOrRequestPermission = updateOrRequestPermission,
                savePhotoToExternalStorage = savePhotoToExternalStorage
            )

            DeleteButton(
                name = name,
                onDeleteItem = onDeleteItem
            )
        }

        Spacer(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.Black)
        )
    }
}

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun LazyItemImage(
    painter: ImagePainter
) {
    val painterState = painter.state
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (painterState) {
            is ImagePainter.State.Loading -> {
                CircularProgressIndicator()
            }
            is ImagePainter.State.Error -> {
                Toast.makeText(context, "Something wrong!", Toast.LENGTH_LONG).show()
            }
            else -> {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .width(300.dp)
                        .height(300.dp)
                        .padding(5.dp)
                )
            }
        }
    }
}

@Composable
fun SaveImageToExternalStorage(
    name: String,
    uri: Uri,
    updateOrRequestPermission: () -> Unit,
    writePermissionGranted: Boolean,
    savePhotoToExternalStorage: (Context, String, String) -> Boolean
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(5.dp)
    ) {
        Button(
            onClick = {
                updateOrRequestPermission()
                if (writePermissionGranted) {
                    savePhotoToExternalStorage(context, name, uri.toString())
                }

            }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Save",
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "save",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun DeleteButton(
    name: String,
    onDeleteItem: (fileName: String) -> Unit
) {
    var alertDialog by remember { mutableStateOf(false)}

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(5.dp)
    ) {
        Button(
            onClick = { alertDialog = true },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primaryVariant)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delete",
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "delete",
                    tint = MaterialTheme.colors.onPrimary
                )
            }

        }
        AnimatedVisibility(visible = alertDialog) {
            AlertDialog(
                onDismissRequest = { alertDialog = false },
                title = {
                    Text(text = "Delete")
                },
                text = {
                    Text(text = "Do you want to delete this image?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteItem(name)
                            alertDialog = false
                        },
                    ) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { alertDialog = false})
                    {
                        Text(text = "Dismiss")
                    }}

            )
        }
    }
}


@ExperimentalAnimationApi
@Composable
fun ScrollToTopButton(
    listState: LazyListState,
    coroutineScope: CoroutineScope,
) {
    Button(
        onClick = {
            coroutineScope.launch {
                listState.animateScrollToItem(index = 0)
            }
        },
        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primaryVariant),
        modifier = Modifier
            .padding(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            tint = MaterialTheme.colors.onSecondary,
            contentDescription = "Up",
            modifier = Modifier.size(15.dp)
        )
    }
}

