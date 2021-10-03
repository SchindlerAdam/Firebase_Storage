package com.example.firebase


import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


class FirebaseViewModel: ViewModel() {

    private var _imageUriState = MutableLiveData<Uri?>(null)
    val imageUriState: LiveData<Uri?> = _imageUriState

    private var _downloadedImageUri = MutableLiveData<Uri?>(null)
    val downloadedImageUri: LiveData<Uri?> = _downloadedImageUri

    private var _allImage = MutableLiveData(listOf<ImageItem>())
    val allImage: LiveData<List<ImageItem>> = _allImage





    fun setUri(uri: Uri?){
        _imageUriState.value = uri
    }

    fun uploadPhotoToFirebase(context: Context, firebaseReference: StorageReference, filename: String) = viewModelScope.launch {
        try {
            _imageUriState.value.let {
                firebaseReference.child("Images/$filename").putFile(it!!).await()
                _allImage.value = _allImage.value!!.toMutableList().also {
                    val image = ImageItem(name = filename, uri = _imageUriState.value!!)
                    it.add(image)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Successfully uploaded image to Firebase!", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun downloadPhotoFromFirebase(context: Context, firebaseReference: StorageReference, filename: String) = viewModelScope.launch {
        try {
            val downloadedUri= firebaseReference.child("Images/$filename").downloadUrl.await()
            _downloadedImageUri.value = downloadedUri
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$filename image has downloaded from Firebase storage", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun downloadAllPhotoFromFirebase(firebaseReference: StorageReference) = viewModelScope.launch {
        try {
            val images = firebaseReference.child("Images/").listAll().await()
            for(image in images.items) {
                val uri = image.downloadUrl.await()
                val name = image.name
                val imageItem = ImageItem(name = name, uri = uri)
                _allImage.value = _allImage.value?.plus(listOf(imageItem))

            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("Error", "$e.message")
            }
        }
    }

    fun deletePhotoFromFirebase(context: Context, firebaseReference: StorageReference, filename: String) = viewModelScope.launch {
        try {
            firebaseReference.child("Images/$filename").delete().await()
            for(item in _allImage.value!!) {
                if(item.name == filename) {
                    _allImage.value = _allImage.value!!.toMutableList().also {
                        it.remove(item)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$filename image has deleted from Firebase storage", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}