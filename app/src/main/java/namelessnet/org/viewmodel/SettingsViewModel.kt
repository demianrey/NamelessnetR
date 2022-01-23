package namelessnet.org.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.tencent.mmkv.MMKV
import namelessnet.org.Constants
import namelessnet.org.xtreme.MmkvManager

class SettingsViewModel(application: Application) : AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    fun startListenPreferenceChange() {
        PreferenceManager.getDefaultSharedPreferences(getApplication())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCleared() {
        PreferenceManager.getDefaultSharedPreferences(getApplication())
            .unregisterOnSharedPreferenceChangeListener(this)
        Log.i(Constants.ANG_PACKAGE, "Settings ViewModel is cleared")
        super.onCleared()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        Log.d(Constants.ANG_PACKAGE, "Observe settings changed: $key")
        when (key) {
            Constants.PREF_MODE,
            Constants.PREF_VPN_DNS,
            Constants.PREF_REMOTE_DNS,
            Constants.PREF_DOMESTIC_DNS,
            Constants.PREF_ROUTING_DOMAIN_STRATEGY,
            Constants.PREF_ROUTING_MODE,
            Constants.PREF_V2RAY_ROUTING_AGENT,
            Constants.PREF_V2RAY_ROUTING_BLOCKED,
            Constants.PREF_V2RAY_ROUTING_DIRECT,
            -> {
                settingsStorage?.encode(key, sharedPreferences.getString(key, ""))
            }
            Constants.PREF_SPEED_ENABLED,
            Constants.PREF_PROXY_SHARING,
            Constants.PREF_LOCAL_DNS_ENABLED,
            Constants.PREF_FAKE_DNS_ENABLED,
            Constants.PREF_FORWARD_IPV6,
            Constants.PREF_PER_APP_PROXY,
            Constants.PREF_BYPASS_APPS,
            -> {
                settingsStorage?.encode(key, sharedPreferences.getBoolean(key, false))
            }
            Constants.PREF_SNIFFING_ENABLED -> {
                settingsStorage?.encode(key, sharedPreferences.getBoolean(key, true))
            }
            Constants.PREF_PER_APP_PROXY_SET -> {
                settingsStorage?.encode(key, sharedPreferences.getStringSet(key, setOf()))
            }
        }
    }
}
