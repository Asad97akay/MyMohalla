package com.example.samama.mymohalla;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samama.mymohalla.custom.CustomActivity;
import com.example.samama.mymohalla.model.ChatUser;
import com.example.samama.mymohalla.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reqica.drilon.androidpermissionchecklibrary.CheckPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.refactor.lib.colordialog.PromptDialog;

public class LoginActivity extends CustomActivity {

    private TextView loginText, forgotPassword, registerText;
    private EditText username, password;
    private ProgressDialog loginProgressDlg;
    private CheckPermission checkPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginText = (TextView) findViewById(R.id.loginText);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        registerText = (TextView) findViewById(R.id.registerText);
        checkPermission = new CheckPermission(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION")
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission("android.permission.ACCESS_FINE_LOCATION")
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission("android.permission.CAMERA")
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE")
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")
                    != PackageManager.PERMISSION_GRANTED) {
                checkPermission.checkMultiple(new String[]{"android.permission.ACCESS_COARSE_LOCATION",
                        "android.permission.ACCESS_FINE_LOCATION",
                        "android.permission.CAMERA",
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"}, null);
            }
        }

    }

    public void registerOnClick(View view) {
        startActivityForResult(new Intent(this, RegisterActivity.class), 10);
    }

    public void forgotPasswordOnClick(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void loginOnClick(View view) {
        // Extract form fields
        final String user = this.username.getText().toString();
        String password = this.password.getText().toString();
        if (user.length() == 0 || password.length() == 0)
        {
            new PromptDialog(this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning")
                    .setContentText(getString(R.string.err_fields_empty))
                    .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
            return;
        }

        // Do the user authentication
        FirebaseAuth.getInstance().signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Logger.getLogger(LoginActivity.class.getName()).log(Level.ALL, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {

                            Logger.getLogger(LoginActivity.class.getName()).log(Level.ALL, "signInWithEmail", task.getException());
                            new PromptDialog(LoginActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Error")
                                    .setContentText(task.getException().getMessage())
                                    .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                                        @Override
                                        public void onClick(PromptDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            loginProgressDlg.dismiss();
                        }
                        else {
                           // ArrayList<String> defaultRoom = new ArrayList<String>();
                           // defaultRoom.add("home");
                            FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if(ds.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                            UserList.user = ds.getValue(ChatUser.class);
                                            loginProgressDlg.dismiss();
                                            startActivity(new Intent(LoginActivity.this, UserList.class));
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                });
        loginProgressDlg = ProgressDialog.show(this, null,
                getString(R.string.alert_wait));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK)
            finish();

    }
}
