//  val eventTitle: String?,
//     val eventLocation: String?,
//     val eventDescription: String?,
//     val beginTime: Long,
//     val endTime: Long,
//     val reminderInMinutes: Int

class ModelAddEvent {
  String? eventTitle;
  String? eventLocation;
  String? eventDescription;
  int beginTime;
  int endTime;
  int reminderInMinutes;

  ModelAddEvent({
    this.eventTitle,
    this.eventLocation,
    this.eventDescription,
    required this.beginTime,
    required this.endTime,
    required this.reminderInMinutes,
  });

  Map<String, dynamic> toJson() => {
        'eventTitle': eventTitle,
        'eventLocation': eventLocation,
        'eventDescription': eventDescription,
        'beginTime': beginTime,
        'endTime': endTime,
        'reminderInMinutes': reminderInMinutes,
      };
}
