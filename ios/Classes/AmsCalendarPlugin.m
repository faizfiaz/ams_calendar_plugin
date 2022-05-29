#import "AmsCalendarPlugin.h"
#if __has_include(<ams_calendar_plugin/ams_calendar_plugin-Swift.h>)
#import <ams_calendar_plugin/ams_calendar_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "ams_calendar_plugin-Swift.h"
#endif

@implementation AmsCalendarPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAmsCalendarPlugin registerWithRegistrar:registrar];
}
@end
