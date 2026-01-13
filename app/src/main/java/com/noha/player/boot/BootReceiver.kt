package com.noha.player.boot

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noha.player.ui.MainActivity
import com.noha.player.data.local.SettingsStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun settingsStore(): SettingsStore
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext as Application
        val settingsStore = EntryPointAccessors.fromApplication(
            appContext,
            BootReceiverEntryPoint::class.java
        ).settingsStore()

        CoroutineScope(Dispatchers.IO).launch {
            val enabled = settingsStore.startOnBootFlow.first()
            if (enabled) {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(launchIntent)
            }
        }
    }
}

