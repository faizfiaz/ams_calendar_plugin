import 'package:ams_calendar_plugin/ams_calendar_plugin.dart';
import 'package:ams_calendar_plugin/model_add_event.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    // initPlatformState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: InkWell(
            onTap: () => addEventToCalendar(),
            child: const Text('Add event to calendar'),
          ),
        ),
      ),
    );
  }

  void addEventToCalendar() {
    final ModelAddEvent model = ModelAddEvent(
      eventTitle: 'Event title',
      eventLocation: 'Event location',
      eventDescription: 'Event description',
      beginTime: DateTime.now().millisecondsSinceEpoch,
      endTime:
          DateTime.now().add(const Duration(hours: 1)).millisecondsSinceEpoch,
      reminderInMinutes: 10,
    );
    AmsCalendarPlugin.addEventToCalendar(model).then((String? result) {
      print('result: $result');
    });
  }
}
