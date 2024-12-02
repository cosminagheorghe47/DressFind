package com.example.dressfind.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;

public class CameraService {
    private final Context context;

    public CameraService(Context context) {
        this.context = context;
    }

    public void openCamera(ActivityResultLauncher<Intent> cameraLauncher) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1_000_000);

        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }
}
