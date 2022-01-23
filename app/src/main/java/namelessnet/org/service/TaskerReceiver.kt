package namelessnet.org.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.google.zxing.WriterException
import com.tencent.mmkv.MMKV
import namelessnet.org.Constants
import namelessnet.org.xtreme.MmkvManager
import namelessnet.org.xtreme.Utils

class TaskerReceiver : BroadcastReceiver() {
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    override fun onReceive(context: Context, intent: Intent?) {

        try {
            val bundle = intent?.getBundleExtra(Constants.TASKER_EXTRA_BUNDLE)
            val switch = bundle?.getBoolean(Constants.TASKER_EXTRA_BUNDLE_SWITCH, false)
            val guid = bundle?.getString(Constants.TASKER_EXTRA_BUNDLE_GUID, "")

            if (switch == null || guid == null || TextUtils.isEmpty(guid)) {
                return
            } else if (switch) {
                if (guid == Constants.TASKER_DEFAULT_GUID) {
                    Utils.startVServiceFromToggle(context)
                } else {
                    mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
                    V2RayServiceManager.startV2Ray(context)
                }
            } else {
                Utils.stopVService(context)
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
