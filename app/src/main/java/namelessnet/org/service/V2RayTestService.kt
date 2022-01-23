package namelessnet.org.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.SyncStateContract
import android.util.Log
import go.Seq
import kotlinx.coroutines.*
import namelessnet.org.Constants
import namelessnet.org.xtreme.MessageUtil
import namelessnet.org.xtreme.MmkvManager
import namelessnet.org.xtreme.Utils
import namelessnet.org.xtreme.V2rayConfigUtil

/**
 * Created by shaowen.jiang@wellcare.cn on 2021/4/4.
 */
/**class V2RayTestService : Service() {

 /**   private val realTestScope by lazy { CoroutineScope(Dispatchers.IO) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Seq.setContext(this)
        namelesslibray.initV2Env(Utils.userAssetPath(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var value = 0
        if (intent != null) {
            value = intent.getIntExtra("key", 0)
        }

        if (MSG_MEASURE_CONFIG == value) {

            val content = intent?.getStringArrayExtra("content")

            content?.let {

                for (guid in content) {

                    realTestScope.launch {

                        try {

                            val result = V2rayConfigUtil.getV2rayConfig(this@V2RayTestService, guid)
                            if (!result.status) {
                                MmkvManager.encodeServerTestDelayMillis(guid, -1L)
                                return@launch
                            }

                            val delay = namelesslibray.measureOutboundDelay(result.content)
                            MmkvManager.encodeServerTestDelayMillis(guid, delay)
                            MessageUtil.sendMsg2UI(
                                application,
                                Constants.MSG_MEASURE_CONFIG_SUCCESS,
                                guid
                            )

                            Log.e("TAG", "onStartCommand: $delay")
                        } catch (e: Exception) {
                            MmkvManager.encodeServerTestDelayMillis(guid, -1L)
                        }

                    }
                }

            }

        }

        if (MSG_MEASURE_CONFIG_CANCEL == value) {
            realTestScope.coroutineContext[Job]?.cancelChildren()
        }



        return super.onStartCommand(intent, flags, startId)
    }  **/
}**/