package com.example.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.today_.MainActivity;
import com.example.today_.R;

public class MyAccessibilityService extends AccessibilityService {
    private Context context;
    private MainActivity activity;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String recognizedText = event.getText().toString();

        MainActivity activity = MainActivity.getInstance();
        if (activity != null) {
            activity.setFloatingActionButtonText(recognizedText)
            ;
        }

        Toast.makeText(context, recognizedText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        context = getApplicationContext();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스를 foreground로 실행하기 위해 알림 생성
        Notification notification = buildNotification();
        startForeground(1, notification); // 포그라운드 서비스 시작
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildNotification() {
        // foreground로 실행할 알림 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "채널 이름",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("접근성 서비스")
                .setContentText("실행 중")
                .setSmallIcon(R.drawable.run_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    @Override
    public void onInterrupt() {
        // 서비스가 중단되거나 비활성화되었을 때 호출됨
    }
}
