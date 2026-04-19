package com.example.trangchu

import android.content.Context

object ProductGatewayProvider {
    fun provide(context: Context): ProductGateway = BackendProductGateway(context)
}


