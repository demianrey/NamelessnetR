package namelessnet.org.xtructure

import android.os.Bundle
import namelessnet.org.R
import namelessnet.org.service.V2RayServiceManager
import namelessnet.org.xtreme.BaseActivity
import namelessnet.org.xtreme.Utils

class ScSwitchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        setContentView(R.layout.activity_none)

        if (V2RayServiceManager.v2rayPoint.isRunning) {
            Utils.stopVService(this)
        } else {
            Utils.startVServiceFromToggle(this)
        }
        finish()
    }
}
