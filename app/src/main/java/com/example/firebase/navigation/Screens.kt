package com.example.firebase.navigation

sealed class Screens(val route: String) {
    object UploadScreen: Screens("upload_screen")
    object DownloadScreen: Screens("download_screen")
    object CollectionScreen: Screens("collection_screen")
}
