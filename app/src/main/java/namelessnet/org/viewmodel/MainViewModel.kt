package namelessnet.org.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*
import namelessnet.org.App
import namelessnet.org.Constants
import namelessnet.org.R
import namelessnet.org.core.ServerConfig
import namelessnet.org.core.V2rayConfig
import namelessnet.org.core.protocols
import namelessnet.org.xtreme.MessageUtil
import namelessnet.org.xtreme.MmkvManager
import namelessnet.org.xtreme.MmkvManager.KEY_ANG_CONFIGS
import namelessnet.org.xtreme.Utils
import namelessnet.org.xtreme.toast
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val serverRawStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SERVER_RAW,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    var serverList = MmkvManager.decodeServerList()
        private set
    val serversCache = ConcurrentHashMap<String, ServerConfig>()
    val isRunning by lazy { MutableLiveData<Boolean>() }
    val updateListAction by lazy { MutableLiveData<Int>() }
    val updateTestResultAction by lazy { MutableLiveData<String>() }

    private val tcpingTestScope by lazy { CoroutineScope(Dispatchers.IO) }

    fun startListenBroadcast() {
        isRunning.value = false
        getApplication<App>().registerReceiver(
            mMsgReceiver,
            IntentFilter(Constants.BROADCAST_ACTION_ACTIVITY)
        )
        MessageUtil.sendMsg2Service(getApplication(), Constants.MSG_REGISTER_CLIENT, "")
    }

    override fun onCleared() {
        getApplication<App>().unregisterReceiver(mMsgReceiver)
        tcpingTestScope.coroutineContext[Job]?.cancelChildren()
        Utils.closeAllTcpSockets()
        Log.i(Constants.ANG_PACKAGE, "Main ViewModel is cleared")
        super.onCleared()
    }

    fun reloadServerList() {
        serverList = MmkvManager.decodeServerList()
        updateCache()
        updateListAction.value = -1
    }

    fun removeServer(guid: String) {
        serverList.remove(guid)
        MmkvManager.removeServer(guid)
    }

    fun appendCustomConfigServer(server: String) {
        val config = ServerConfig.create(protocols.PRIVATE)
        config.remarks = System.currentTimeMillis().toString()
        config.fullConfig = Gson().fromJson(server, V2rayConfig::class.java)
        val key = MmkvManager.encodeServerConfig("", config)
        serverRawStorage?.encode(key, server)
        serverList.add(key)
        serversCache[key] = config
    }

    fun swapServer(fromPosition: Int, toPosition: Int) {
        Collections.swap(serverList, fromPosition, toPosition)
        mainStorage?.encode(KEY_ANG_CONFIGS, Gson().toJson(serverList))
    }

    private fun updateCache() {
        serversCache.clear()
        GlobalScope.launch(Dispatchers.Default) {
            serverList.forEach { guid ->
                MmkvManager.decodeServerConfig(guid)?.let {
                    serversCache[guid] = it
                }
            }
        }
    }

    fun testAllTcping() {
        tcpingTestScope.coroutineContext[Job]?.cancelChildren()
        Utils.closeAllTcpSockets()
        MmkvManager.clearAllTestDelayResults()
        updateListAction.value = -1 // update all

        getApplication<App>().toast(R.string.connection_test_testing)
        for (guid in serverList) {
            serversCache.getOrElse(guid) { MmkvManager.decodeServerConfig(guid) }
                ?.getProxyOutbound()?.let { outbound ->
                val serverAddress = outbound.getServerAddress()
                val serverPort = outbound.getServerPort()
                if (serverAddress != null && serverPort != null) {
                    tcpingTestScope.launch {
                        val testResult = Utils.tcping(serverAddress, serverPort)
                        launch(Dispatchers.Main) {
                            MmkvManager.encodeServerTestDelayMillis(guid, testResult)
                            updateListAction.value = serverList.indexOf(guid)
                        }
                    }
                }
            }
        }
    }

    @DelicateCoroutinesApi
    fun testCurrentServerRealPing() {
        val socksPort =
            10808//Utils.parseInt(defaultDPreference.getPrefString(SettingsActivity.PREF_SOCKS_PORT, "10808"))
        GlobalScope.launch(Dispatchers.IO) {
            val result = Utils.testConnection(getApplication(), socksPort)
            launch(Dispatchers.Main) {
                updateTestResultAction.value = result
            }
        }
    }

    private val mMsgReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.getIntExtra("key", 0)) {
                Constants.MSG_STATE_RUNNING -> {
                    isRunning.value = true
                }
                Constants.MSG_STATE_NOT_RUNNING -> {
                    isRunning.value = false
                }
                Constants.MSG_STATE_START_SUCCESS -> {
                    getApplication<App>().toast(R.string.toast_services_success)
                    isRunning.value = true
                }
                Constants.MSG_STATE_START_FAILURE -> {
                    getApplication<App>().toast(R.string.toast_services_failure)
                    isRunning.value = false
                }
                Constants.MSG_STATE_STOP_SUCCESS -> {
                    isRunning.value = false
                }
            }
        }
    }
}
