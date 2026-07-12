package com.gtkim.mobile_access_control.component.sync.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.gtkim.mobile_access_control.component.sync.domain.provider.NetworkStateProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NetworkStateProviderImpl @Inject constructor(
    @ApplicationContext context: Context,
) : NetworkStateProvider {

    private val cm: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun isOnline(): Boolean {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun observe(): Flow<Boolean> = callbackFlow {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }
        cm.registerNetworkCallback(request, callback)
        trySend(isOnline())
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
