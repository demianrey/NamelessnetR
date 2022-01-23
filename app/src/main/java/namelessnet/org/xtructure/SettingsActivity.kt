package namelessnet.org.xtructure

import android.content.Intent
import android.os.Bundle
import androidx.preference.*
import android.text.TextUtils
import android.view.View
import namelessnet.org.R
import androidx.activity.viewModels
import namelessnet.org.Constants
import namelessnet.org.service.V2RayServiceManager
import namelessnet.org.viewmodel.SettingsViewModel
import namelessnet.org.xtreme.BaseActivity
import namelessnet.org.xtreme.Utils
import namelessnet.org.xtreme.toast

class SettingsActivity : BaseActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        title = getString(R.string.title_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settingsViewModel.startListenPreferenceChange()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val perAppProxy by lazy { findPreference(Constants.PREF_PER_APP_PROXY) as CheckBoxPreference }
        private val localDns by lazy { findPreference(Constants.PREF_LOCAL_DNS_ENABLED) }
        private val fakeDns by lazy { findPreference(Constants.PREF_FAKE_DNS_ENABLED) }
        private val localDnsPort by lazy { findPreference(Constants.PREF_LOCAL_DNS_PORT) }
        private val vpnDns by lazy { findPreference(Constants.PREF_VPN_DNS) }
        private val sppedEnabled by lazy { findPreference(Constants.PREF_SPEED_ENABLED) as CheckBoxPreference }
        private val sniffingEnabled by lazy { findPreference(Constants.PREF_SNIFFING_ENABLED) as CheckBoxPreference }
        private val proxySharing by lazy { findPreference(Constants.PREF_PROXY_SHARING) as CheckBoxPreference }
        private val domainStrategy by lazy { findPreference(Constants.PREF_ROUTING_DOMAIN_STRATEGY) as ListPreference }
        private val routingMode by lazy { findPreference(Constants.PREF_ROUTING_MODE) as ListPreference }

        private val forwardIpv6 by lazy { findPreference(Constants.PREF_FORWARD_IPV6) as CheckBoxPreference }
        private val enableLocalDns by lazy { findPreference(Constants.PREF_LOCAL_DNS_ENABLED) as CheckBoxPreference }
        private val domesticDns by lazy { findPreference(Constants.PREF_DOMESTIC_DNS) as EditTextPreference }
        private val remoteDns by lazy { findPreference(Constants.PREF_REMOTE_DNS) as EditTextPreference }

        //        val autoRestart by lazy { findPreference(PREF_AUTO_RESTART) as CheckBoxPreference }


//        val socksPort by lazy { findPreference(PREF_SOCKS_PORT) as EditTextPreference }
//        val httpPort by lazy { findPreference(PREF_HTTP_PORT) as EditTextPreference }

        private val routingCustom: Preference by lazy { findPreference(Constants.PREF_ROUTING_CUSTOM) }
//        val donate: Preference by lazy { findPreference(PREF_DONATE) }
        //        val licenses: Preference by lazy { findPreference(PREF_LICENSES) }
//        val feedback: Preference by lazy { findPreference(PREF_FEEDBACK) }
//        val tgGroup: Preference by lazy { findPreference(PREF_TG_GROUP) }

        private val mode by lazy { findPreference(Constants.PREF_MODE) as ListPreference }

        private fun restartProxy() {
            Utils.stopVService(requireContext())
            V2RayServiceManager.startV2Ray(requireContext())
        }

        private fun isRunning(): Boolean {
            return false //TODO no point of adding logic now since Settings will be changed soon
        }

        override fun onCreatePreferences(bundle: Bundle?, s: String?) {
            addPreferencesFromResource(R.xml.pref_settings)

            perAppProxy.setOnPreferenceClickListener {
                if (isRunning()) {
                    Utils.stopVService(requireContext())
                }
                startActivity(Intent(activity, PerAppProxyActivity::class.java))
                perAppProxy.isChecked = true
                true
            }
            sppedEnabled.setOnPreferenceClickListener {
                if (isRunning())
                    restartProxy()
                true
            }
            sniffingEnabled.setOnPreferenceClickListener {
                if (isRunning())
                    restartProxy()
                true
            }

            proxySharing.setOnPreferenceClickListener {
                if (proxySharing.isChecked)
                    activity?.toast(R.string.toast_warning_pref_proxysharing)
                if (isRunning())
                    restartProxy()
                true
            }

            domainStrategy.setOnPreferenceChangeListener { _, _ ->
                if (isRunning())
                    restartProxy()
                true
            }
            routingMode.setOnPreferenceChangeListener { _, _ ->
                if (isRunning())
                    restartProxy()
                true
            }

            routingCustom.setOnPreferenceClickListener {
                if (isRunning())
                    Utils.stopVService(requireContext())
                startActivity(Intent(activity, RoutingSettingsActivity::class.java))
                false
            }

            forwardIpv6.setOnPreferenceClickListener {
                if (isRunning())
                    restartProxy()
                true
            }

            enableLocalDns.setOnPreferenceClickListener {
                if (isRunning())
                    restartProxy()
                true
            }


            domesticDns.setOnPreferenceChangeListener { _, any ->
                // domesticDns.summary = any as String
                val nval = any as String
                domesticDns.summary = if (nval == "") Constants.DNS_DIRECT else nval
                if (isRunning())
                    restartProxy()
                true
            }

            remoteDns.setOnPreferenceChangeListener { _, any ->
                // remoteDns.summary = any as String
                val nval = any as String
                remoteDns.summary = if (nval == "") Constants.DNS_AGENT else nval
                if (isRunning())
                    restartProxy()
                true
            }
            localDns?.setOnPreferenceChangeListener{ _, any ->
                updateLocalDns(any as Boolean)
                true
            }
            localDnsPort?.setOnPreferenceChangeListener { _, any ->
                val nval = any as String
                localDnsPort?.summary = if (TextUtils.isEmpty(nval)) "10807" else nval
                true
            }
            vpnDns?.setOnPreferenceChangeListener { _, any ->
                vpnDns?.summary = any as String
                true
            }
            mode.setOnPreferenceChangeListener { _, newValue ->
                updateMode(newValue.toString())
                true
            }
            mode.dialogLayoutResource = R.layout.preference_with_help_link

//            donate.onClick {
//                startActivity<InappBuyActivity>()
//            }

//            licenses.onClick {
//                val fragment = LicensesDialogFragment.Builder(act)
//                        .setNotices(R.raw.licenses)
//                        .setIncludeOwnLicense(false)
//                        .build()
//                fragment.show((act as AppCompatActivity).supportFragmentManager, null)
//            }
//
//            feedback.onClick {
//                Utils.openUri(activity, "https://github.com/2dust/v2rayNG/issues")
//            }
//            tgGroup.onClick {
//                //                Utils.openUri(activity, "https://t.me/v2rayN")
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg:resolve?domain=v2rayN"))
//                try {
//                    startActivity(intent)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    toast(R.string.toast_tg_app_not_found)
//                }
//            }


//            socksPort.setOnPreferenceChangeListener { preference, any ->
//                socksPort.summary = any as String
//                true
//            }
//            httpPort.setOnPreferenceChangeListener { preference, any ->
//                httpPort.summary = any as String
//                true
//            }
        }

        override fun onStart() {
            super.onStart()
            val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            updateMode(defaultSharedPreferences.getString(Constants.PREF_MODE, "VPN"))
            var remoteDnsString = defaultSharedPreferences.getString(Constants.PREF_REMOTE_DNS, "")
            domesticDns.summary = defaultSharedPreferences.getString(Constants.PREF_DOMESTIC_DNS, "")

            if (TextUtils.isEmpty(remoteDnsString)) {
                remoteDnsString = Constants.DNS_AGENT
            }
            if ( domesticDns.summary == "") {
                domesticDns.summary = Constants.DNS_DIRECT
            }
            remoteDns.summary = remoteDnsString
            vpnDns?.summary = defaultSharedPreferences.getString(Constants.PREF_VPN_DNS, remoteDnsString)

//            socksPort.summary = defaultSharedPreferences.getString(PREF_SOCKS_PORT, "10808")
//            lanconnPort.summary = defaultSharedPreferences.getString(PREF_HTTP_PORT, "")
        }

        private fun updateMode(mode: String?) {
            val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val vpn = mode == "VPN"
            perAppProxy.isEnabled = vpn
            perAppProxy.isChecked = PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(Constants.PREF_PER_APP_PROXY, false)
            localDns?.isEnabled = vpn
            fakeDns?.isEnabled = vpn
            localDnsPort?.isEnabled = vpn
            vpnDns?.isEnabled = vpn
            if (vpn) {
                updateLocalDns(defaultSharedPreferences.getBoolean(Constants.PREF_LOCAL_DNS_ENABLED, false))
            }
        }

        private fun updateLocalDns(enabled: Boolean) {
            fakeDns?.isEnabled = enabled
            localDnsPort?.isEnabled = enabled
            vpnDns?.isEnabled = !enabled
        }
    }

    fun onModeHelpClicked(view: View) {
        //Utils.openUri(this, Constants.v2rayNGWikiMode)
    }
}
