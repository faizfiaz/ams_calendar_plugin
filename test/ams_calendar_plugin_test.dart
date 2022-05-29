import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ams_calendar_plugin/ams_calendar_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('ams_calendar_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AmsCalendarPlugin.platformVersion, '42');
  });
}
