package com.example.theemergency_1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Camera extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    ImageView thumbnail;

    ActivityResultLauncher<Intent> launcher_record = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d("launcher_record Callback", "video recording is succeeded");

                        Intent data = result.getData();
                        if (data != null)
                        {

                            Uri videoPath = data.getData();

                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(Camera.this, videoPath);

                            Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(100000);

                            thumbnail.setImageBitmap(bitmap);

                        }

                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {

                        Log.d("launcher_record Callback", "video recording is canceled");

                    } else {

                        Log.e("launcher_record Callback", "video recording has failed");

                    }


                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        thumbnail = findViewById(R.id.img_thumbnail);
        Button record = findViewById(R.id.btn_record);
        Button back = findViewById(R.id.btn_back);

        record.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(Camera.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(Camera.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)

                {

                    if(ActivityCompat.shouldShowRequestPermissionRationale(Camera.this, Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(Camera.this, Manifest.permission.RECORD_AUDIO)){

                        new AlertDialog.Builder(Camera.this)
                                .setTitle("권한 필요")
                                .setMessage("동영상 촬영을 위해 카메라와 오디오 권한이 필요합니다.")
                                .setPositiveButton("허용", (dialog, which) -> {
                                    ActivityCompat.requestPermissions(Camera.this,
                                            new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                                            CAMERA_PERMISSION_CODE);
                                })
                                .setNegativeButton("거부", (dialog, which) -> {

                                    Toast.makeText(Camera.this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();

                                })
                                .create()
                                .show();
                    } else {
                        // 권한 요청
                        ActivityCompat.requestPermissions(Camera.this,
                                new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, CAMERA_PERMISSION_CODE);
                    }

                } else {

                    Toast.makeText(Camera.this, "동영상 촬영을 시작합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent_record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    launcher_record.launch(intent_record);

                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE){
            boolean cameraGranted = false;
            boolean audioGranted = false;

            if(grantResults.length > 0){

                for(int i=0; i < permissions.length; i++)
                {
                    if(permissions[i].equals(Manifest.permission.CAMERA))
                    {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED){

                            cameraGranted = true;

                        }
                    }

                    if(permissions[i].equals(Manifest.permission.RECORD_AUDIO)){
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            audioGranted = true;

                        }
                    }
                }
            }

            if(cameraGranted && audioGranted){
                Toast.makeText(this, "카메라 및 오디오 권한이 허용됨", Toast.LENGTH_SHORT).show();
                Intent intent_record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                launcher_record.launch(intent_record);

            } else {

                Toast.makeText(this, "카메라 또는 오디오 권한이 거부 됨", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
