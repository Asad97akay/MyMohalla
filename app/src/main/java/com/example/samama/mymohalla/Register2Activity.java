package com.example.samama.mymohalla;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.TransactionTooLargeException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.samama.mymohalla.model.ChatUser;
import com.example.samama.mymohalla.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.redmadrobot.inputmask.MaskedTextChangedListener;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Register2Activity extends AppCompatActivity {

    private myMohallaEditText cnicEditText, fullnameEditText, passwordEditText, emailEditText;
    private Intent intent;

    /** Register progress dialog */
    private ProgressDialog registerProgressDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        cnicEditText = (myMohallaEditText) findViewById(R.id.cnicEditText);
        fullnameEditText = (myMohallaEditText) findViewById(R.id.fullnameEditText);
        passwordEditText = (myMohallaEditText) findViewById(R.id.passwordEditText);
        emailEditText = (myMohallaEditText) findViewById(R.id.emailEditText);

        InputFilter inputFilter = new InputFilter() {
            int iter = 0;
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                for(iter = i; iter < i1 ; iter++){
                    if(!Character.isLetter(charSequence.charAt(iter)) && !Character.isSpaceChar(charSequence.charAt(iter))){
                        return "";
                    }
                }
                return null;
            }
        };

        fullnameEditText.setFilters(new InputFilter[]{inputFilter});

        intent = getIntent();
//        String picurl = intent.getStringExtra("profilePictureUrl");

//        byte[] decodedString = Base64.decode(picurl, Base64.DEFAULT);
//        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//        myimageview.setImageBitmap(decodedByte);




        final MaskedTextChangedListener listener = new MaskedTextChangedListener(
                "[00000]-[0000000]-[0]",
                true,
                cnicEditText,
                null,
                new MaskedTextChangedListener.ValueListener() {
                    @Override
                    public void onTextChanged(boolean maskFilled, @NonNull final String extractedValue) {
                        Log.d(Register2Activity.class.getSimpleName(), extractedValue);
                        Log.d(Register2Activity.class.getSimpleName(), String.valueOf(maskFilled));
                    }
                }
        );

        cnicEditText.addTextChangedListener(listener);
        cnicEditText.setOnFocusChangeListener(listener);
        cnicEditText.setHint(listener.placeholder());
    }

    public void registerOnClick(View view) {
        final String[] data = new String[4];
        data[0] = fullnameEditText.getText().toString();
        data[1] = passwordEditText.getText().toString();
        data[2] = emailEditText.getText().toString();
        data[3] = cnicEditText.getText().toString();

        if(data[0].length() == 0 || data[1].length() == 0 || data[2].length() == 0 || data[3].length() == 0){
            Utils.showDialog(this, R.string.err_fields_empty);
            return;
        }

        else{
            // Register the user
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(data[2], data[1]) .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Logger.getLogger(Register2Activity.class.getName()).log(Level.ALL, "createUserWithEmailAndPassword:onComplete:" + task.isSuccessful());
                    registerProgressDlg.dismiss();
                    if (!task.isSuccessful()) {
                        Logger.getLogger(Register2Activity.class.getName()).log(Level.ALL, "createUserWithEmailAndPassword", task.getException());
                        Utils.showDialog(
                                Register2Activity.this,
                                task.getException().getMessage());
                    }
                    else {
                        final ArrayList<String> defaultRoom = new ArrayList<String>();
                        defaultRoom.add("home");

                        // Update the user profile information
                        final FirebaseUser user = task.getResult().getUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(data[0])
                                //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                                .build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Logger.getLogger(Register2Activity.class.getName()).log(Level.ALL, "User profile updated.");
                                    // Construct the ChatUser
                                    ChatUser newUser = new ChatUser(user.getUid(),data[0], data[2],true,defaultRoom, intent.getStringExtra("profilePictureUrl"), intent.getStringExtra("location"), data[3], intent.getBooleanExtra("gender", true));
                                    UserList.user = newUser;

                                    // Setup link to users database
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).setValue(UserList.user);
                                    startActivity(new Intent(Register2Activity.this, UserList.class));
                                    finish();
                                }
                            }
                        });

                    }

                }
            });

            registerProgressDlg = ProgressDialog.show(this, null,
                    getString(R.string.alert_wait));
        }
    }
}
