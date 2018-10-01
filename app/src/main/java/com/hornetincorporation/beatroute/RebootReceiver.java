package com.hornetincorporation.beatroute;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

public class RebootReceiver extends BroadcastReceiver {

    private AlarmManager StartalarmMgr, StopalarmMgr;
    private PendingIntent startSvcPI, stopSvcPI;

    @Override
    public void onReceive(Context context, Intent intent) {

        StartalarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        StopalarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (PendingIntent.getBroadcast(context, Constants.ALARM.START_SERVICE_REQUEST_CODE, new Intent(context, StartServiceAlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) == null) {
            // Set Start Service Alarm
            Intent startSvcIntent = new Intent(context, StartServiceAlarmReceiver.class);
            startSvcPI = PendingIntent.getBroadcast(context, Constants.ALARM.START_SERVICE_REQUEST_CODE, startSvcIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar StartSvcCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            StartSvcCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
            StartSvcCalendar.setTimeInMillis(System.currentTimeMillis());
            StartSvcCalendar.set(Calendar.HOUR_OF_DAY, Constants.ALARM.START_SERVICE_HOUR);
            StartSvcCalendar.set(Calendar.MINUTE, Constants.ALARM.START_SERVICE_MIN);
            StartSvcCalendar.set(Calendar.SECOND, Constants.ALARM.START_SERVICE_SEC);

            StartalarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, StartSvcCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startSvcPI);
        }

        if (PendingIntent.getBroadcast(context, Constants.ALARM.STOP_SERVICE_REQUEST_CODE, new Intent(context, StopServiceAlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) == null) {
            // Stop Service Alarm
            Intent stopSvcIntent = new Intent(context, StopServiceAlarmReceiver.class);
            stopSvcPI = PendingIntent.getBroadcast(context, Constants.ALARM.STOP_SERVICE_REQUEST_CODE, stopSvcIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar StopSvcCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            StopSvcCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
            StopSvcCalendar.setTimeInMillis(System.currentTimeMillis());
            StopSvcCalendar.set(Calendar.HOUR_OF_DAY, Constants.ALARM.STOP_SERVICE_HOUR);
            StopSvcCalendar.set(Calendar.MINUTE, Constants.ALARM.STOP_SERVICE_MIN);
            StopSvcCalendar.set(Calendar.SECOND, Constants.ALARM.STOP_SERVICE_SEC);
            StopalarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, StopSvcCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, stopSvcPI);
        }

        Calendar curDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        curDateCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        Integer Cur_Hour_Of_Day = curDateCalendar.get(Calendar.HOUR_OF_DAY);

        if (!(Constants.ALARM.START_SERVICE_HOUR > Cur_Hour_Of_Day && Constants.ALARM.STOP_SERVICE_HOUR <= Cur_Hour_Of_Day)) {
            Intent service = new Intent(context, BeeterTrackingService.class);

            SharedPreferences sharedPref = context.getSharedPreferences(Constants.BEETER_TRACKING_SERVICE.PREF_FILE, Context.MODE_PRIVATE);
            if (sharedPref.contains(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING)) {
                if (sharedPref.getString(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING, "DEFAULT").equals("FALSE") || sharedPref.getString(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING, "DEFAULT").equals("DEFAULT")) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING, "TRUE");
                    editor.commit();
                    context.startService(service);
                }
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING, "TRUE");
                editor.commit();
                context.startService(service);
            }
        } else {
            Intent service = new Intent(context, BeeterTrackingService.class);

            SharedPreferences sharedPref = context.getSharedPreferences(Constants.BEETER_TRACKING_SERVICE.PREF_FILE, Context.MODE_PRIVATE);
            if (sharedPref.contains(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING)) {
                if (sharedPref.getString(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING, "DEFAULT").equals("TRUE")) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.BEETER_TRACKING_SERVICE.IS_SERVICE_RUNNING, "FALSE");
                    editor.commit();
                    context.stopService(service);
                }
            }
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Show initialized message at mobile start
            Toast.makeText(context, R.string.app_initialized, Toast.LENGTH_SHORT).show();
        }
    }
}