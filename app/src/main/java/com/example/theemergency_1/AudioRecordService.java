package com.example.theemergency_1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class AudioRecordService extends Service {

    private static final String CHANNEL_ID = "AudioRecordChannel";
    private static final int NOTIFICATION_ID = 1;
    private MediaRecorder recorder;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        startRecording();
    }

    private Notification buildNotification() {


        Intent notificationIntent = new Intent(this, RecorderActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
        );

       // (talk 이미지)
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.talk);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("녹음 중")
                .setContentText("백그라운드에서 녹음이 진행 중입니다.")
                .setSmallIcon(R.drawable.ic_notification) // 증요 설정
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "녹음 채널",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("백그라운드 녹음 채널");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/recording_" + System.currentTimeMillis() + ".m4a";
        recorder.setOutputFile(filePath);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {

            recorder.prepare();
            recorder.start();

        } catch (IOException | IllegalStateException e) {

            e.printStackTrace();

            stopSelf(); // 녹음 실패 시 서비스 종료
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                // 녹음 중 예외 발생 시 로그 출력
            }
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 바인딩을 사용하지 않으므로 null 반환
    }
}
