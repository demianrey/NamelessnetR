package namelessnet.org.core

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    var isSelected: Int,
    val appIcon: Drawable,
    val playstore: String,
    val isPremium: Boolean,
    val packageName: String,
    val isSystemApp: Boolean,
)