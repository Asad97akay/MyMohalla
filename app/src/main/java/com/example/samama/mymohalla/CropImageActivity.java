package com.example.samama.mymohalla;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

public class CropImageActivity extends AppCompatActivity {

    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        Intent intent = getIntent();

        byte[] decodedString = Base64.decode(intent.getStringExtra("bitmap"), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        cropImageView.setImageBitmap(decodedByte);

        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                StringBuilder croppedImage = new StringBuilder();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                result.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteFormat = stream.toByteArray();
                croppedImage.append(Base64.encodeToString(byteFormat, Base64.NO_WRAP));

                Intent returnIntent = new Intent();
                returnIntent.putExtra("image", croppedImage.toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    public void cropOnClick(View view) {
        cropImageView.getCroppedImageAsync();
    }

    public void cancelOnClick(View view) {
        finish();
    }
}
