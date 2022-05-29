package com.faiz.ams_calendar_plugin

import androidx.annotation.NonNull
import com.google.gson.Gson

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** AmsCalendarPlugin */
class AmsCalendarPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var calendarHelper: CalendarHelper

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "ams_calendar_plugin")
    channel.setMethodCallHandler(this)

    calendarHelper = CalendarHelper(flutterPluginBinding.applicationContext)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "addEvent") {
      val modelAddEvent = Gson().fromJson(call.arguments.toString(), ModelAddEvent::class.java)
      val resultAddEvent = calendarHelper.addEvent(
        eventTitle = modelAddEvent.eventTitle,
        eventLocation = modelAddEvent.eventLocation,
        eventDescription = modelAddEvent.eventDescription,
        beginTime = modelAddEvent.beginTime,
        endTime = modelAddEvent.endTime,
        reminderMinute = modelAddEvent.reminderInMinutes
      )
      result.success(resultAddEvent.toString())
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
