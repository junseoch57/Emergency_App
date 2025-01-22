package com.example.theemergency_1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RecorderActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    private boolean isRecording = false;
    private Button recordButton;
    private ImageView overlayImageView;

    private int tapCount = 0;
    private long lastTapTime = 0;
    private static final long TAP_TIMEOUT = 1000; // 1초 내에 3번 터치

    private Handler handler = new Handler();
    private Runnable resetTapCountRunnable = () -> tapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder);

        // 레이아웃 요소 초기화
        recordButton = findViewById(R.id.recordButton);
        overlayImageView = findViewById(R.id.overlayImageView);

        // 권한 확인 및 요청
        if (!hasAudioPermission()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // 녹음 버튼 클릭 리스너
        recordButton.setOnClickListener(v -> toggleRecording());

        // 오버레이 이미지 클릭 리스너
        overlayImageView.setOnClickListener(v -> handleOverlayClick());

        // Back 버튼 클릭 리스너
        Button backButton = findViewById(R.id.Back);
        backButton.setOnClickListener(v -> finish());
    }

    private void handleOverlayClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTapTime < TAP_TIMEOUT) {
            tapCount++;
            if (tapCount == 3) {
                stopRecording();
                resetTapCount();
            }
        } else {
            tapCount = 1;
        }

        lastTapTime = currentTime;

        handler.removeCallbacks(resetTapCountRunnable);
        handler.postDelayed(resetTapCountRunnable, TAP_TIMEOUT);
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            if (hasAudioPermission()) {
                startRecording();
            } else {
                requestAudioPermission();
            }
        }
    }

    private void startRecording() {
        startService(new Intent(this, AudioRecordService.class));
        overlayImageView.setVisibility(View.VISIBLE);
        recordButton.setText("녹음 중지");

        isRecording = true;
    }

    private void stopRecording() {
        stopService(new Intent(this, AudioRecordService.class));
        overlayImageView.setVisibility(View.GONE);
        recordButton.setText("녹음 시작");
        isRecording = false;
        tapCount = 0;

        Toast.makeText(this, "녹음이 종료되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void resetTapCount() {
        tapCount = 0;
    }

    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("권한이 필요합니다")
                    .setMessage("이 앱은 오디오 녹음을 위해 마이크 접근 권한이 필요합니다.")
                    .setPositiveButton("확인", (dialog, which) ->
                            ActivityCompat.requestPermissions(RecorderActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS))
                    .setNegativeButton("취소", (dialog, which) -> {
                        Toast.makeText(RecorderActivity.this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (hasAudioPermission()) {
                // 권한이 승인됨
                Toast.makeText(this, "녹음 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "필수 권한이 거부되었습니다. 녹음 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(resetTapCountRunnable);
        super.onDestroy();
    }
}
