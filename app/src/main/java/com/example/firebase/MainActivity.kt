package com.example.firebase

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.example.firebase.navigation.BottomNavItem
import com.example.firebase.navigation.BottomNavigationBar
import com.example.firebase.navigation.Navigation
import com.example.firebase.networkstatus.NetworkConnection
import com.example.firebase.screens.LandingScreen
import com.example.firebase.ui.theme.FirebaseTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.net.URL

class MainActivity : ComponentActivity() {

    private lateinit var selectImageLauncher: ActivityResultLauncher<String>
    lateinit var networkConnection: NetworkConnection
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.Q)
    @ExperimentalCoilApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        networkConnection = NetworkConnection(this)
        val firebaseStorage = Firebase.storage
        val firebaseReference = firebaseStorage.reference
        val viewModel: FirebaseViewModel by viewModels()
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            viewModel.setUri(uri)
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }


        setContent {
            val isNetworkAvailable = networkConnection.observeAsState(false).value
            FirebaseTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        firebaseReference = firebaseReference,
                        viewModel = viewModel,
                        selectImageLauncher = selectImageLauncher,
                        networkConnection = isNetworkAvailable,
                        updateOrRequestPermission = { updateOrRequestPermission() },
                        writePermissionGranted = writePermissionGranted,
                        savePhotoToExternalStorage = {context, displayName, url -> savePhotoToExternalStorage(context, displayName, url)}
                    )
                }
            }
        }
    }

    private fun updateOrRequestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        val permissionsToRequest = mutableListOf<String>()
        if(!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePhotoToExternalStorage(context: Context, displayName: String, url: String): Boolean {

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        return try {
            val resolver = context.contentResolver
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)?.also { uri ->
                URL(url).openStream().use { input ->
                    resolver.openOutputStream(uri).use { output ->
                        input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry!")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

}

@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
private fun MainScreen(
    firebaseReference: StorageReference,
    viewModel: FirebaseViewModel,
    selectImageLauncher: ActivityResultLauncher<String>,
    networkConnection: Boolean,
    updateOrRequestPermission: () -> Unit,
    writePermissionGranted: Boolean,
    savePhotoToExternalStorage: (Context, String, String) -> Boolean
) {

    var showLandingScreen by remember { mutableStateOf(true)}

    Surface(color = MaterialTheme.colors.primary) {
        if(showLandingScreen) {
            LandingScreen(
                firebaseReference = firebaseReference,
                viewModel = viewModel,
                onTimeout = { showLandingScreen = false})
        } else {
            FireBaseStorage(
                firebaseReference = firebaseReference,
                viewModel = viewModel,
                selectImageLauncher = selectImageLauncher,
                networkConnection = networkConnection,
                updateOrRequestPermission = updateOrRequestPermission,
                writePermissionGranted = writePermissionGranted,
                savePhotoToExternalStorage = savePhotoToExternalStorage
            )
        }

    }
}


@ExperimentalCoilApi
@ExperimentalAnimationApi
@Composable
fun FireBaseStorage(
    firebaseReference: StorageReference,
    viewModel: FirebaseViewModel,
    selectImageLauncher: ActivityResultLauncher<String>,
    networkConnection: Boolean,
    updateOrRequestPermission: () -> Unit,
    writePermissionGranted: Boolean,
    savePhotoToExternalStorage: (Context, String, String) -> Boolean
) {

    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = listOf(
                    BottomNavItem(
                        name = "Upload",
                        route = "upload_screen",
                        icon = Icons.Default.CloudUpload
                    ),
                    BottomNavItem(
                        name = "Download",
                        route = "download_screen",
                        icon = Icons.Default.CloudDownload
                    ),
                    BottomNavItem(
                        name = "Collection",
                        route = "collection_screen",
                        icon = Icons.Default.Collections
                    )
                ),
                navController = navController,
                onItemClick = {
                    navController.navigate(it.route)
                }
            )
        }
    ) {
        Navigation(
            navController = navController,
            viewModel = viewModel,
            firebaseReference = firebaseReference,
            selectImageLauncher = selectImageLauncher,
            networkConnection = networkConnection,
            updateOrRequestPermission = updateOrRequestPermission,
            writePermissionGranted = writePermissionGranted,
            savePhotoToExternalStorage = savePhotoToExternalStorage
        )
    }
}

@Composable
fun NetworkError() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "No internet connection!",
            textAlign = TextAlign.Center
        )
    }
}






