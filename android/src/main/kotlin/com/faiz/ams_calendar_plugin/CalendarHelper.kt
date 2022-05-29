package com.faiz.ams_calendar_plugin

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import java.util.*


class CalendarHelper(private val applicationContext: Context) {
    //Change return type List<EventDay> to String Json so Flutter App can parse it
//    fun readEvents(context: Context, month: Calendar): List<EventDay> {
//        val INSTANCE_PROJECTION = arrayOf<String>(
//            CalendarContract.Instances.EVENT_ID,  // 0
//            CalendarContract.Instances.BEGIN,  // 1
//            CalendarContract.Instances.TITLE,  // 2
//            CalendarContract.Instances.ORGANIZER
//        )
//
//        // The indices for the projection array above.
//        val PROJECTION_ID_INDEX = 0
//        val PROJECTION_BEGIN_INDEX = 1
//        val PROJECTION_TITLE_INDEX = 2
//        val PROJECTION_ORGANIZER_INDEX = 3
//
//        // Specify the date range you want to search for recurring event instances
//        val startMillis = month.timeInMillis
//        month.add(Calendar.MONTH, 1)
//        val endMillis = month.timeInMillis
//
//        // Construct the query with the desired date range.
//        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
//        ContentUris.appendId(builder, startMillis)
//        ContentUris.appendId(builder, endMillis)
//
//        // Submit the query
//        val cur =
//            context.contentResolver.query(builder.build(), INSTANCE_PROJECTION, null, null, null)
//        val events: MutableList<EventDay> = ArrayList<EventDay>()
//        while (cur != null && cur.moveToNext()) {
//            val beginVal = cur.getLong(PROJECTION_BEGIN_INDEX)
//            val calendar = Calendar.getInstance()
//            calendar.timeInMillis = beginVal
//            events.add(EventDay(calendar, R.drawable.ic_dot_black))
//        }
//        cur?.close()
//        return events
//    }

    fun readEventsDetail(context: Context, day: Calendar): List<Event> {
        val INSTANCE_PROJECTION = arrayOf<String>(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ORGANIZER,
            CalendarContract.Instances.DESCRIPTION
        )

        // The indices for the projection array above.
        val PROJECTION_ID_INDEX = 0
        val PROJECTION_BEGIN_INDEX = 1
        val PROJECTION_TITLE_INDEX = 2
        val PROJECTION_ORGANIZER_INDEX = 3
        val PROJECTION_DESCRIPTION_INDEX = 4

        // Specify the date range you want to search for recurring event instances
        val startMillis = day.timeInMillis
        val endTime = Calendar.getInstance()
        endTime.timeInMillis = startMillis
        endTime[Calendar.HOUR] = 23
        endTime[Calendar.MINUTE] = 59
        val endMillis = endTime.timeInMillis

        // The ID of the recurring event whose instances you are searching for in the Instances table
        val selection: String =
            CalendarContract.Instances.DTSTART + " >= ? AND " + CalendarContract.Instances.DTSTART + "<= ?"
        val selectionArgs = arrayOf(startMillis.toString() + "", endMillis.toString() + "")

        // Construct the query with the desired date range.
        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        // Submit the query
        val cur = context.contentResolver.query(
            builder.build(),
            INSTANCE_PROJECTION,
            selection,
            selectionArgs,
            null
        )
        val events: MutableList<Event> = ArrayList()
        while (cur != null && cur.moveToNext()) {
            // Get the field values
            val event = Event()
            event.id = cur.getLong(PROJECTION_ID_INDEX)
            event.begin = cur.getLong(PROJECTION_BEGIN_INDEX)
            event.title = cur.getString(PROJECTION_TITLE_INDEX)
            event.organizer = cur.getString(PROJECTION_ORGANIZER_INDEX)
            event.description = cur.getString(PROJECTION_DESCRIPTION_INDEX)
            events.add(event)
        }
        cur?.close()
        return events
    }

    fun addEvent(
        eventTitle: String?, eventLocation: String?,
        eventDescription: String?, beginTime: Long, endTime: Long, reminderMinute: Int
    ): Uri? {
        val cr: ContentResolver = applicationContext.contentResolver
        val values = ContentValues()
        values.put(CalendarContract.Events.DTSTART, getTzAdjustedDate(beginTime))
        values.put(CalendarContract.Events.DTEND, getTzAdjustedDate(endTime))
        values.put(CalendarContract.Events.TITLE, eventTitle)
        values.put(CalendarContract.Events.EVENT_LOCATION, eventLocation)
        values.put(CalendarContract.Events.DESCRIPTION, eventDescription)
        values.put(CalendarContract.Events.CALENDAR_ID, getGmailCalendarId())
        values.put(CalendarContract.Events.HAS_ALARM, 1)
        values.put(
            CalendarContract.Events.EVENT_TIMEZONE,
            "UTC"
        )
        values.put(CalendarContract.Events.EVENT_END_TIMEZONE, "UTC")
        val uri: Uri? = cr.insert(CalendarContract.Events.CONTENT_URI, values)
        var eventID: Long = 0
        if (uri != null) if (uri.lastPathSegment != null) eventID = uri.lastPathSegment!!
            .toLong()
        val reminders = ContentValues()
        reminders.put(CalendarContract.Reminders.EVENT_ID, eventID)
        reminders.put(
            CalendarContract.Reminders.METHOD,
            CalendarContract.Reminders.METHOD_ALERT
        )
        reminders.put(CalendarContract.Reminders.MINUTES, reminderMinute)
        return try {
            val result = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders)
            resyncCalendars()
            result
        } catch (ignored: SQLiteException) {
            null
        }
    }

    fun getTzAdjustedDate(date: Long): Long {
        val tzDefault = TimeZone.getDefault()
        return date - tzDefault.getOffset(date)
    }

    private fun getGmailCalendarId(): String? {
        val projection = arrayOf("_id", "calendar_displayName")
        var calenderId: String? = "1"
        val calendars: Uri = Uri.parse("content://com.android.calendar/calendars")
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val managedCursor: Cursor? = contentResolver.query(
            calendars,
            projection, null, null, null
        )
        if (managedCursor != null && managedCursor.moveToFirst()) {
            var calName: String
            var calID: String?
            val nameCol = managedCursor.getColumnIndex(projection[1])
            val idCol = managedCursor.getColumnIndex(projection[0])
            do {
                calName = managedCursor.getString(nameCol)
                calID = managedCursor.getString(idCol)
                if (calName.contains("@gmail")) calenderId = calID
            } while (managedCursor.moveToNext())
            managedCursor.close()
            return calenderId
        }
        return calenderId
    }

    private fun listSelectedCalendars(
        c: Context,
        eventTitle: String, eventLocation: String,
        beginTime: Long
    ): List<Int> {
        val eventUri = calendarUriBase
        val result: MutableList<Int> = ArrayList()
        val projection = arrayOf<String>(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )
        val selection: String = CalendarContract.Instances.DTSTART + " == ? AND " +
                CalendarContract.Instances.EVENT_LOCATION + "== ?"
        val selectionArgs = arrayOf(
            beginTime.toString() + "",
            eventLocation
        )
        val cursor = c.contentResolver.query(
            eventUri, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                var calID: String
                var calName: String?
                val idCol = cursor.getColumnIndex(projection[0])
                val nameCol = cursor.getColumnIndex(projection[1])
                do {
                    try {
                        calName = cursor.getString(nameCol)
                        calID = cursor.getString(idCol)
                        if (calName != null && calName.contains(eventTitle)) {
                            result.add(calID.toInt())
                        }
                    } catch (e: Exception) {
                        Objects.requireNonNull(e.message)
                            ?.let { Log.e("listSelectedCalendars: ", it) }
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return result
    }

    private fun listSelectedCalendars(c: Context, eventTitle: String): List<Int> {
        val eventUri = calendarUriBase
        val result: MutableList<Int> = ArrayList()
        val projection = arrayOf("_id", "title", "description")
        val cursor = c.contentResolver.query(
            eventUri, null, null, null,
            null
        )
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(projection[0])
                val nameCol = cursor.getColumnIndex(projection[1])
                val descCol = cursor.getColumnIndex(projection[2])
                do {
                    try {
                        val id = cursor.getString(idCol)
                        val title = cursor.getString(nameCol)
                        val description = cursor.getString(descCol)
                        if (title != null && title.contains(eventTitle)) {
                            result.add(id.toInt())
                        } else if (description != null && description.contains(eventTitle)) {
                            result.add(id.toInt())
                        }
                    } catch (e: Exception) {
                        Objects.requireNonNull(e.message)
                            ?.let { Log.e("listSelectedCalendars: ", it) }
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return result
    }

    fun updateCalendarEntry(c: Context, entryID: Int): Int {
        val iNumRowsUpdated: Int
        val eventUri = calendarUriBase
        val values = ContentValues()
        values.put(CalendarContract.Events.TITLE, "test")
        values.put(CalendarContract.Events.EVENT_LOCATION, "Chennai")
        val updateUri: Uri = ContentUris.withAppendedId(eventUri, entryID.toLong())
        iNumRowsUpdated = c.contentResolver.update(
            updateUri, values, null,
            null
        )
        return iNumRowsUpdated
    }

    fun deleteCalendarEntry(
        c: Context,
        eventTitle: String, eventLocation: String,
        beginTime: Long
    ): Int {
        var iNumRowsDeleted = 0
        var beginTimeWithoutMillis = beginTime / 10000
        beginTimeWithoutMillis *= 10000
        if (eventTitle.length > 0) {
            val selectedCalendars = listSelectedCalendars(
                c,
                eventTitle, eventLocation, beginTimeWithoutMillis
            )
            Log.e("deleteCalendarEntry: ", selectedCalendars.size.toString() + "//")
            for (i in selectedCalendars.indices) {
                val eventUri: Uri = ContentUris
                    .withAppendedId(calendarUriBase, selectedCalendars[i].toLong())
                iNumRowsDeleted += c.contentResolver.delete(eventUri, null, null)
            }
        }
        return iNumRowsDeleted
    }

    fun deleteCalendarEntry(c: Context, eventTitle: String): Int {
        var iNumRowsDeleted = 0
        if (eventTitle.length > 0) {
            val calendarIds = listSelectedCalendars(c, eventTitle)
            for (i in calendarIds.indices) {
                val eventUri: Uri = ContentUris
                    .withAppendedId(calendarUriBase, calendarIds[i].toLong())
                iNumRowsDeleted += c.contentResolver.delete(eventUri, null, null)
            }
        }
        return iNumRowsDeleted
    }

    fun deleteCalendarEntry(c: Context, entryID: Int): Int {
        var iNumRowsDeleted = 0
        val eventUri: Uri = ContentUris
            .withAppendedId(calendarUriBase, entryID.toLong())
        iNumRowsDeleted = c.contentResolver.delete(eventUri, null, null)
        return iNumRowsDeleted
    }

    private val calendarUriBase: Uri
        private get() {
            val eventUri: Uri
            eventUri = Uri.parse("content://com.android.calendar/events")
            return eventUri
        }

    class Event {
        var organizer: String? = null
        var id: Long = 0
        var begin: Long = 0
        var title: String? = null
        var description: String? = null
        fun print() {
            Log.i("Event", "$id - $begin - $organizer - $title - $description")
        }
    }

    fun resyncCalendars() {
        val accounts = AccountManager.get(applicationContext).accounts
        Log.d("Calendar Plugin", "Refreshing " + accounts.size + " accounts")
        val authority = CalendarContract.Calendars.CONTENT_URI.authority
        for (i in accounts.indices) {
            Log.d("Calendar Plugin", "Refreshing calendars for: " + accounts[i])
            val extras = Bundle()
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            ContentResolver.requestSync(accounts[i], authority, extras)
        }
    }
}