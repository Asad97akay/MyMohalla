package com.example.samama.mymohalla;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.balysv.materialripple.MaterialRippleLayout;
import com.example.samama.mymohalla.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.reqica.drilon.androidpermissionchecklibrary.CheckPermission;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.refactor.lib.colordialog.ColorDialog;
import id.zelory.compressor.Compressor;
import siclo.com.ezphotopicker.api.EZPhotoPick;
import siclo.com.ezphotopicker.api.EZPhotoPickStorage;
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig;
import siclo.com.ezphotopicker.api.models.PhotoSource;

public class RegisterActivity extends AppCompatActivity{

    public StringBuilder profilePictureUrl, location;
    private ProgressDialog progressDialog;
    TextView registration, locationTextView;
    ImageView profilePicture, maleTag, femaleTag;
    boolean isLocationReady = false, gender;
    private static final int REQUEST_CODE_YOUR = 123;
    private static final int REQUEST_CODE_CROP = 987;
    private CheckPermission checkPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registration = (TextView) findViewById(R.id.registration);
        locationTextView = (TextView) findViewById(R.id.locationTextView);
        profilePicture = (ImageView) findViewById(R.id.profilepicture);
        maleTag = (ImageView) findViewById(R.id.maleTag);
        femaleTag = (ImageView) findViewById(R.id.femaleTag);
        progressDialog = new ProgressDialog(this);
        checkPermission = new CheckPermission(this);

        profilePictureUrl = new StringBuilder();
        location = new StringBuilder();

        setListeners();
    }

    protected void setListeners(){
        maleTag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    maleTag.setImageResource(R.drawable.maletag);
                    femaleTag.setImageResource(R.drawable.female);
                    gender = true;
                    return true;
                }
                return false;
            }
        });

        femaleTag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    femaleTag.setImageResource(R.drawable.femaletag);
                    maleTag.setImageResource(R.drawable.male);
                    gender = false;
                    return true;
                }
                return false;
            }
        });

        locationTextView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION")
                            != PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission("android.permission.ACCESS_FINE_LOCATION")
                            != PackageManager.PERMISSION_GRANTED) {
                        checkPermission.checkMultiple(new String[]{"android.permission.ACCESS_COARSE_LOCATION",
                                "android.permission.ACCESS_FINE_LOCATION"}, null);
                    }
                }
                Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
                startActivityForResult(intent, REQUEST_CODE_YOUR);
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorDialog dialog = new ColorDialog(RegisterActivity.this);
                dialog.setAnimationEnable(true);
                dialog.setContentText("Select an option");
                dialog.setTitle("Upload your picture");
                dialog.setPositiveListener("Camera", new ColorDialog.OnPositiveListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(ColorDialog dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission("android.permission.CAMERA")
                                    != PackageManager.PERMISSION_GRANTED) {
                                checkPermission.checkOne("android.permission.CAMERA", null);
                            }
                        }
                        EZPhotoPickConfig config = new EZPhotoPickConfig();
                        config.photoSource = PhotoSource.CAMERA; // or PhotoSource.CAMERA
                        EZPhotoPick.startPhotoPickActivity(RegisterActivity.this, config);
                        dialog.dismiss();
                        progressDialog.setMessage("Processing...");
                        progressDialog.show();
                    }
                })
                        .setNegativeListener("Gallery", new ColorDialog.OnNegativeListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onClick(ColorDialog dialog) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")
                                            != PackageManager.PERMISSION_GRANTED
                                            || checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE")
                                            != PackageManager.PERMISSION_GRANTED) {
                                        checkPermission.checkMultiple(new String[]
                                                        {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
                                                null);

                                    }
                                }
                                EZPhotoPickConfig config = new EZPhotoPickConfig();
                                config.photoSource = PhotoSource.GALERY; // or PhotoSource.CAMERA
                                EZPhotoPick.startPhotoPickActivity(RegisterActivity.this, config);
                                dialog.dismiss();
                                progressDialog.setMessage("Processing...");
                                progressDialog.show();
                            }
                        }).show();
            }
        });
    }

    public static int getExitOrientation(String filpath){
        int degree = 0;
        ExifInterface exif = null;

        try{
            exif = new ExifInterface(filpath);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        if(exif != null){
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            if(orientation != -1){
                switch(orientation){
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_YOUR:
                if (resultCode == Activity.RESULT_OK) {

                    String latitude = data.getStringExtra("lat");
                    String longitude = data.getStringExtra("lng");
                    String GPS = latitude + "," + longitude;
                    location.append(GPS);
                    locationTextView.setText(GPS);
                    isLocationReady = true;
                }
                break;
            case EZPhotoPick.PHOTO_PICK_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK) {
                    try {
                        EZPhotoPickStorage ezPhotoPickStorage = new EZPhotoPickStorage(this);
                        Bitmap pickedPhoto = ezPhotoPickStorage.loadLatestStoredPhotoBitmap();
                        if (pickedPhoto != null) {
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            pickedPhoto.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                            File file = new File(Environment.getExternalStorageDirectory()
                                    + File.separator + "profile.jpg");
                            file.createNewFile();

                            Log.d("BEFORE", String.valueOf(pickedPhoto.getByteCount()));
                            //profilePicture.setImageBitmap(pickedPhoto);

//                            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
//                                    "/MyMohalla";
//                            File dir = new File(file_path);
//                            if (!dir.exists()) {
//                                Log.d("FILE", "DOES NOT EXIT");
//                                dir.getParentFile().mkdirs();
//                            }
//                            File file = new File(ezPhotoPickStorage.loadLatestStoredPhotoDir(), ezPhotoPickStorage.loadLatestStoredPhotoName());
                            FileOutputStream fOut = new FileOutputStream(file);

                            pickedPhoto.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                            fOut.flush();
                            fOut.close();



                            pickedPhoto = new Compressor.Builder(this)
                                    .setQuality(25)
                                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                                    .build()
                                    .compressToBitmap(file);

                            Log.d("AFTER", String.valueOf(pickedPhoto.getByteCount()));

                            Matrix matrix = new Matrix();
                            matrix.postRotate(getExitOrientation(file.getPath()));

                            pickedPhoto = Bitmap.createBitmap(pickedPhoto, 0, 0, pickedPhoto.getWidth(), pickedPhoto.getHeight(), matrix, true);
                            profilePicture.setImageBitmap(pickedPhoto);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            pickedPhoto.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] byteFormat = stream.toByteArray();

                            //Clear profilePictureUrl so that the previous Url does not create any problem.
                            profilePictureUrl.delete(0, profilePictureUrl.length());
                            profilePictureUrl.append(Base64.encodeToString(byteFormat, Base64.DEFAULT));

                            Log.d("CROP", "CALLING");
                            Intent intent = new Intent(RegisterActivity.this, CropImageActivity.class);
                            intent.putExtra("bitmap", profilePictureUrl.toString());
                            startActivityForResult(intent, REQUEST_CODE_CROP);
                            Log.d("CROP", "CALLED");

                            try {
                                ezPhotoPickStorage.removePhoto(ezPhotoPickStorage.loadLatestStoredPhotoDir(), ezPhotoPickStorage.loadLatestStoredPhotoName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        progressDialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    progressDialog.dismiss();
                }
                break;

            case REQUEST_CODE_CROP:
                if(resultCode == Activity.RESULT_OK){
                    byte[] decodedString = Base64.decode(data.getStringExtra("image"), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profilePicture.setImageBitmap(decodedByte);

                    profilePictureUrl = new StringBuilder(data.getStringExtra("image"));

                    Log.d("AFTER AGAIN", String.valueOf(decodedByte.getByteCount()));
                }
                break;
        }
    }

    public void continueOnClick(View view) {
        if (profilePictureUrl.length() == 0 && location.length() == 0)
        {
            Utils.showDialog(this, R.string.err_fields_empty);
            return;
        }
        else if (profilePictureUrl.length() == 0)
        {
            Utils.showDialog(this, R.string.err_upload_image);
            return;
        }
        else if (location.length() == 0)
        {
            Utils.showDialog(this, R.string.err_enter_location);
            return;
        }
        else{
            Intent intent = new Intent(this, Register2Activity.class);
            Log.d("location", location.toString());
            Log.d("profilePic", profilePictureUrl.toString());
            intent.putExtra("location", location.toString());
            intent.putExtra("profilePictureUrl", profilePictureUrl.toString());
            intent.putExtra("gender", gender);
            startActivity(intent);
            finish();
        }
    }
}
