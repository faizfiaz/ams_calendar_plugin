import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:ams_calendar_plugin/model_add_event.dart';
import 'package:flutter/services.dart';

class AmsCalendarPlugin {
  static const MethodChannel _channel = MethodChannel('ams_calendar_plugin');

  static Future<String?> addEventToCalendar(ModelAddEvent model) async {
    final String? version = await _channel.invokeMethod(
        'addEvent', jsonEncode(model.toJson(), toEncodable: encoder));
    return version;
  }

  static dynamic encoder(dynamic item) {
    if (item is DateTime) {
      return item.toIso8601String();
    }
    if (item is File) {
      return "";
    }
    return item;
  }
}
