package namelessnet.org

import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.tencent.mmkv.MMKV

class App : MultiDexApplication() {
    companion object {
        const val PREF_LAST_VERSION = "pref_last_version"
    }

    var curIndex = -1 //Current proxy that is opened. (Used to implement restart feature)
    var firstRun = false
        private set

    override fun onCreate() {
        super.onCreate()

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE
        if (firstRun)
            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE)
                .apply()

        MMKV.initialize(this)
    }
}