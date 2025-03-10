package com.example.widgetdemo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class CounterWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_INCREMENT = "ACTION_INCREMENT"
        private const val ACTION_DECREMENT = "ACTION_DECREMENT"
        private const val PREFS_NAME = "CounterWidgetPrefs"
        private const val PREF_COUNTER_KEY = "CounterValue"

        private fun getCounterValue(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(PREF_COUNTER_KEY, 0)  // Default to 0
        }

        private fun updateCounterValue(context: Context, newValue: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(PREF_COUNTER_KEY, newValue).apply()
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_counter)

            val counterValue = getCounterValue(context)
            views.setTextViewText(R.id.tv_counter, counterValue.toString())

            // Set up intents for buttons
            val incrementIntent = Intent(context, CounterWidget::class.java).apply {
                action = ACTION_INCREMENT
            }
            val decrementIntent = Intent(context, CounterWidget::class.java).apply {
                action = ACTION_DECREMENT
            }

            val incrementPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                incrementIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val decrementPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                decrementIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.btn_plus, incrementPendingIntent)
            views.setOnClickPendingIntent(R.id.btn_minus, decrementPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_INCREMENT || intent.action == ACTION_DECREMENT) {
            val currentValue = getCounterValue(context)
            val newValue =
                if (intent.action == ACTION_INCREMENT) currentValue + 1 else currentValue - 1
            updateCounterValue(context, newValue)

            // Update all widgets
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CounterWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
