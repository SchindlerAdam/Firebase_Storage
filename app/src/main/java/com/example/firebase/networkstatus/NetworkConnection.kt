package com.example.firebase.networkstatus

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData

class NetworkConnection(context: Context) : LiveData<Boolean>() {

    var connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    val validNetworkConnections : MutableSet<Network> = HashSet()

    fun checkValidNetworks(){
        postValue(validNetworkConnections.size > 0)
    }

    private fun getConnectivityManagerCallback() = object : ConnectivityManager.NetworkCallback(){

        override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val networkCapability = connectivityManager.getNetworkCapabilities(network)
                val hasNetworkConnection = networkCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasNetworkConnection == true){
                    validNetworkConnections.add(network)
                    checkValidNetworks()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                validNetworkConnections.remove(network)
                checkValidNetworks()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
                    validNetworkConnections.add(network)
                } else {
                    validNetworkConnections.remove(network)
                }
                checkValidNetworks()
            }
        }


    override fun onActive() {
        super.onActive()
        connectivityManagerCallback = getConnectivityManagerCallback()
        val networkRequest = NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, connectivityManagerCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    }


}
