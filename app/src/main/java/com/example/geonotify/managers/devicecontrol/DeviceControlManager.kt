package com.example.geonotify.managers.devicecontrol

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class DeviceControlManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Using a static/companion object to ensure original values are preserved across service cycles
    companion object {
        private var originalMediaVolume: Int = -1
        private var originalRingerMode: Int = -1
        private var originalInterruptionFilter: Int = -1
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun applySettings(muteMic: Boolean, muteMedia: Boolean, muteNotif: Boolean) {
        Log.d("DeviceControl", "Applying settings: Media=$muteMedia, Notif=$muteNotif")

        if (muteMedia) {
            // Save current volume if not already saved
            if (originalMediaVolume == -1) {
                originalMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            }
            
            // Force volume to 0 and use MUTE flag
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            }
            showToast("Media Volume Muted")
        }

        if (muteNotif) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    // Method 1: Do Not Disturb (Most reliable on Android 6+)
                    if (originalInterruptionFilter == -1) {
                        originalInterruptionFilter = notificationManager.currentInterruptionFilter
                    }
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                    
                    // Method 2: Silent Ringer (Backup)
                    if (originalRingerMode == -1) {
                        originalRingerMode = audioManager.ringerMode
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    
                    showToast("Do Not Disturb Enabled")
                } else {
                    showToast("DND Access Required")
                }
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                showToast("Silent Mode Enabled")
            }
        }
        
        if (muteMic) {
            audioManager.isMicrophoneMute = true
            showToast("Microphone Muted")
        }
    }

    fun restoreSettings(restoreMic: Boolean, restoreMedia: Boolean, restoreNotif: Boolean) {
        Log.d("DeviceControl", "Restoring settings")

        if (restoreMedia) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            }
            if (originalMediaVolume != -1) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, AudioManager.FLAG_SHOW_UI)
                originalMediaVolume = -1 // Reset after restore
            }
            showToast("Media Restored")
        }

        if (restoreNotif) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    // Restore DND
                    if (originalInterruptionFilter != -1) {
                        notificationManager.setInterruptionFilter(originalInterruptionFilter)
                        originalInterruptionFilter = -1
                    } else {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    
                    // Restore Ringer
                    if (originalRingerMode != -1) {
                        audioManager.ringerMode = originalRingerMode
                        originalRingerMode = -1
                    } else {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    }
                }
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            showToast("Notifications Restored")
        }

        if (restoreMic) {
            audioManager.isMicrophoneMute = false
        }
    }
}
