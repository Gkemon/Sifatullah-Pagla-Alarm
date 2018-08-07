package com.binary.geek.shitatullahPaglaAlarm.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.binary.geek.shitatullahPaglaAlarm.R;
import com.binary.geek.shitatullahPaglaAlarm.model.Alarm;
import com.binary.geek.shitatullahPaglaAlarm.ui.AlarmLandingPageActivity;
import com.binary.geek.shitatullahPaglaAlarm.util.AlarmUtils;
import com.binary.geek.shitatullahPaglaAlarm.util.ViewUtils;

import java.util.Calendar;

public final class AlarmReceiver extends BroadcastReceiver {

    private static final String ALARM_EXTRA = "alarm_extra";

    @Override
    public void onReceive(Context context, Intent intent) {

        final Alarm alarm = intent.getParcelableExtra(ALARM_EXTRA);
        final int id = AlarmUtils.getNotificationId(alarm);

        final NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final Intent notifIntent = new Intent(context, AlarmLandingPageActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        final Intent intentForOpenLandingPage = new Intent(context, AlarmLandingPageActivity.class);
        intentForOpenLandingPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentForOpenLandingPage);


        final PendingIntent pIntent = PendingIntent.getActivity(
                context, id, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_alarm_white_24dp);
        builder.setColor(ContextCompat.getColor(context, R.color.accent));
       // builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText("অশিক্ষিত মূর্খ বর্বররা ঘুম থেকে উঠ জলদি");
        builder.setTicker(alarm.getLabel());
        builder.setVibrate(new long[] {1000,500,1000,500,1000,500,1000,500,1000,500,1000,500});
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setContentIntent(pIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);

        manager.notify(id, builder.build());

        //Reset Alarm manually
        if(AlarmUtils.isAlarmActive(alarm))
            setReminderAlarm(context, alarm);

    }

    //Convenience method for setting a notification
    public static void setReminderAlarm(Context context, Alarm alarm) {

       // Check whether the alarm is set to run on any days
        if(!AlarmUtils.isAlarmActive(alarm)) {
            Log.d("GK","ACTIVE");


            final Calendar time = Calendar.getInstance();

            time.set(Calendar.SECOND,(int)(alarm.getTime()/1000);

            alarm.setTime(System.currentTimeMillis()+(System.currentTimeMillis()-alarm.getTime()));

            Log.d("GK","Next time :"+System.currentTimeMillis()+(System.currentTimeMillis()-alarm.getTime())+"");

            final Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(ALARM_EXTRA, alarm);



            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(context,AlarmUtils.getNotificationId(alarm), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Log.d("GK","A NORMAL ALARM CREATED.Alarm time "+alarm.getTime()+" Current time :"+System.currentTimeMillis());
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                am.set(AlarmManager.RTC_WAKEUP, alarm.getTime(), pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, alarm.getTime(), pi);
            }

            //If alarm not set to run on any days, cancel any existing notifications for this alarm
           // cancelReminderAlarm(context, alarm);

            return;
        }
        else {
            Log.d("GK","INACTIVE");
        }

        final Calendar nextAlarmTime = getTimeForNextAlarm(alarm);
        alarm.setTime(nextAlarmTime.getTimeInMillis());

        final Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_EXTRA, alarm);

        final PendingIntent pIntent = PendingIntent.getBroadcast(
                context,
                AlarmUtils.getNotificationId(alarm),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            am.set(AlarmManager.RTC_WAKEUP, alarm.getTime(), pIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, alarm.getTime(), pIntent);
        }

    }

    /**
     * Calculates the actual time of the next alarm/notification based on the user-set time the
     * alarm should sound each day, the days the alarm is set to run, and the current time.
     * @param alarm Alarm containing the daily time the alarm is set to run and days the alarm
     *              should run
     * @return A Calendar with the actual time of the next alarm.
     */
    private static Calendar getTimeForNextAlarm(Alarm alarm) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getTime());

        final long currentTime = System.currentTimeMillis();
        final int startIndex = getStartIndexFromTime(calendar);

        int count = 0;
        boolean isAlarmSetForDay;

        final SparseBooleanArray daysArray = alarm.getDays();

        do {
            final int index = (startIndex + count) % 7;
            isAlarmSetForDay =
                    daysArray.valueAt(index) && (calendar.getTimeInMillis() > currentTime);
            if(!isAlarmSetForDay) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                count++;
            }
        } while(!isAlarmSetForDay && count < 7);

        return calendar;

    }

    public static void cancelReminderAlarm(Context context, Alarm alarm) {

        final Intent intent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(
                context,
                AlarmUtils.getNotificationId(alarm),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pIntent);
    }

    private static int getStartIndexFromTime(Calendar c) {

        final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        int startIndex = 0;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                startIndex = 0;
                break;
            case Calendar.TUESDAY:
                startIndex = 1;
                break;
            case Calendar.WEDNESDAY:
                startIndex = 2;
                break;
            case Calendar.THURSDAY:
                startIndex = 3;
                break;
            case Calendar.FRIDAY:
                startIndex = 4;
                break;
            case Calendar.SATURDAY:
                startIndex = 5;
                break;
            case Calendar.SUNDAY:
                startIndex = 6;
                break;
        }

        return startIndex;

    }

}
