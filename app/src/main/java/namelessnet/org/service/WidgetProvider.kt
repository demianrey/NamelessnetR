package namelessnet.org.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import namelessnet.org.Constants
import namelessnet.org.R
import namelessnet.org.xtreme.Utils

class WidgetProvider : AppWidgetProvider() {
    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidgetBackground(
            context,
            appWidgetManager,
            appWidgetIds,
            V2RayServiceManager.v2rayPoint.isRunning
        )
    }

    private fun updateWidgetBackground(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        isRunning: Boolean
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_switch)
        val intent = Intent(context, WidgetProvider::class.java)
        intent.action = Constants.BROADCAST_ACTION_WIDGET_CLICK
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            R.id.layout_switch,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        remoteViews.setOnClickPendingIntent(R.id.layout_switch, pendingIntent)
        if (isRunning) {
            remoteViews.setInt(
                R.id.layout_switch,
                "setBackgroundResource",
                R.drawable.confusing_shape
            )
        } else {
            remoteViews.setInt(
                R.id.layout_switch,
                "setBackgroundResource",
                R.drawable.confusing_shape
            )
        }

        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    /**
     * 接收窗口小部件发送的广播
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (Constants.BROADCAST_ACTION_WIDGET_CLICK == intent.action) {
            if (V2RayServiceManager.v2rayPoint.isRunning) {
                Utils.stopVService(context)
            } else {
                Utils.startVServiceFromToggle(context)
            }
        } else if (Constants.BROADCAST_ACTION_ACTIVITY == intent.action) {
            AppWidgetManager.getInstance(context)?.let { manager ->
                when (intent.getIntExtra("key", 0)) {
                    Constants.MSG_STATE_RUNNING, Constants.MSG_STATE_START_SUCCESS -> {
                        updateWidgetBackground(
                            context,
                            manager,
                            manager.getAppWidgetIds(
                                ComponentName(
                                    context,
                                    WidgetProvider::class.java
                                )
                            ),
                            true
                        )
                    }
                    Constants.MSG_STATE_NOT_RUNNING, Constants.MSG_STATE_START_FAILURE, Constants.MSG_STATE_STOP_SUCCESS -> {
                        updateWidgetBackground(
                            context,
                            manager,
                            manager.getAppWidgetIds(
                                ComponentName(
                                    context,
                                    WidgetProvider::class.java
                                )
                            ),
                            false
                        )
                    }
                }
            }
        }
    }
}
