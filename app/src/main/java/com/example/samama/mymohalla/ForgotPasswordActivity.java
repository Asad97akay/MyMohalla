package com.example.samama.mymohalla;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.samama.mymohalla.model.ChatUser;
import com.example.samama.mymohalla.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException;

import cn.refactor.lib.colordialog.PromptDialog;

public class ForgotPasswordActivity extends AppCompatActivity {

    myMohallaEditText emailTextView;
    private ProgressDialog progressDialog;
    Boolean flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailTextView = (myMohallaEditText) findViewById(R.id.emailEditText);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Searching our database...");
    }

    public void sendEmailVerification(View view) {
        progressDialog.show();
        final String email = emailTextView.getText().toString();
        if(email.length() != 0){
            Log.d("Length", "Not zero");
            FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("onDataChange", "True");
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        ChatUser user = ds.getValue(ChatUser.class);
                        Log.d("Email", user.getEmail());
                        if(email.equals(user.getEmail())){
                            Log.d("TRUE", "EMAIL");
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email);
                            new PromptDialog(ForgotPasswordActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                                    .setAnimationEnable(true)
                                    .setTitleText("Success")
                                    .setContentText("A password reset email has been sent.")
                                    .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                                        @Override
                                        public void onClick(PromptDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                            flag = true;
                        }
                    }
                    progressDialog.dismiss();
                    if(!flag){
                        new PromptDialog(ForgotPasswordActivity.this)
                                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                .setAnimationEnable(true)
                                .setTitleText("Error")
                                .setContentText("No user found by that email address.")
                                .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                                    @Override
                                    public void onClick(PromptDialog dialog) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{
            progressDialog.dismiss();
            new PromptDialog(ForgotPasswordActivity.this)
                    .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("Warning")
                    .setContentText("Please enter your email address.")
                    .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }
}
