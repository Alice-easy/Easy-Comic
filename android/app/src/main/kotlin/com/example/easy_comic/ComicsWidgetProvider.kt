package com.example.easy_comic

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class ComicsWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray, widgetData: SharedPreferences) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.comics_widget).apply {
                val minutes = widgetData.getInt("minutes", 0)
                setTextViewText(R.id.widget_minutes, "$minutes 分钟")
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}