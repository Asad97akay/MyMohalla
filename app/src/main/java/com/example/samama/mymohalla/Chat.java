package com.example.samama.mymohalla;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.example.samama.mymohalla.custom.CustomActivity;
import com.example.samama.mymohalla.model.ChatUser;
import com.example.samama.mymohalla.model.Conversation;
import com.example.samama.mymohalla.utils.Const;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */
public class Chat extends CustomActivity {

    /**
     * The Conversation list.
     */
    private ArrayList<Conversation> convList;

    /**
     * The chat adapter.
     */
    private ChatAdapter adp;

    /**
     * The Editext to compose the message.
     */
    private EditText txt;

    /**
     * The user name of buddy.
     */
    private ChatUser buddy;

    /**
     * The date of last message in conversation.
     */
    private Date lastMsgDate;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */

    private FirebaseUser user;

    private ListView list;

    private Bitmap buddyDpBitmap, userDpBitmap;

    private String[] users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.chat_action_bar, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        BootstrapCircleThumbnail buddyDpActionBar = (BootstrapCircleThumbnail) mCustomView.findViewById(R.id.buddy_dp_actionbar);

        convList = new ArrayList<Conversation>();
        list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);
        users = new String[2];
        txt = (EditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(R.id.btnSend);

        buddy = (ChatUser) getIntent().getSerializableExtra(Const.EXTRA_DATA);

        if(getIntent().getStringExtra("profilepicturebuddy") != null) {
            byte[] decodedString = Base64.decode(getIntent().getStringExtra("profilepicturebuddy"), Base64.DEFAULT);
            buddyDpBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            buddyDpActionBar.setImageBitmap(buddyDpBitmap);
        }

        mTitleTextView.setText(buddy.getUsername());

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatUser user = dataSnapshot.getValue(ChatUser.class);
                if(user.getProfilePicture() != null) {
                    Log.d("ProfilePicture", "FOUND");
                    byte[] decodedString = Base64.decode(user.getProfilePicture(), Base64.DEFAULT);
                    userDpBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                else{
                    Log.d("ProfilePicture", "NOT FOUND");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(user!=null) {
            users[0] = user.getUid();
            users[1] = buddy.getId();
        }
            final ArrayList<String> defaultRoom = new ArrayList<String>();
            defaultRoom.add("home");

            //UserList.user = new ChatUser(user.getUid(),user.getDisplayName(), user.getEmail(),true, defaultRoom);
            // Setup link to users database
            //FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).setValue(UserList.user);
        Arrays.sort(users);
        FirebaseDatabase.getInstance().getReference("messages").child(users[0] + "_" + users[1]).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Listener", "CALLED");
                Conversation conversation = dataSnapshot.getValue(Conversation.class);
                if (conversation.getReceiver().contentEquals(user.getUid()) || conversation.getSender().contentEquals(buddy.getId())) {
                    convList.add(conversation);
                    if (lastMsgDate == null
                            || lastMsgDate.before(conversation.getDate()))
                        lastMsgDate = conversation.getDate();

                    adp.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadConversationList();
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btnSend) {
            sendMessage();
        }

    }

    /**
     * Call this method to Send message to opponent. It does nothing if the text
     * is empty otherwise it creates a Parse object for Chat message and send it
     * to Parse server.
     */
    private void sendMessage() {
        if (txt.length() == 0)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            final Conversation conversation = new Conversation(s,
                    Calendar.getInstance().getTime(),
                    user.getUid(),
                    buddy.getId(),
                    "");
            conversation.setStatus(Conversation.STATUS_SENDING);
            convList.add(conversation);
            final String key = FirebaseDatabase.getInstance()
                    .getReference("messages")
                    .child(users[0] + "_" + users[1])
                    .push().getKey();
            Log.d("KEY", "KEY: " + key);
            //Updating last message.
            FirebaseDatabase.getInstance().getReference("messages").child(users[0] + "_" + users[1]).child("lastMessage").setValue(conversation);

            //Updating database conversations.
            FirebaseDatabase.getInstance().getReference("messages").child(users[0] + "_" + users[1]).child(key)
                    .setValue(conversation)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if (task.isSuccessful()) {
                                                       convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_SENT);
                                                   } else {
                                                       convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_FAILED);
                                                   }
                                                   FirebaseDatabase.getInstance()
                                                           .getReference("messages")
                                                           .child(users[0] + "_" + users[1]).child(key).setValue(convList.get(convList.indexOf(conversation)))
                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                              @Override
                                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                                  adp.notifyDataSetChanged();
                                                                                              }
                                                                                          });

                                               }
                                           }
                    );
        }
        adp.notifyDataSetChanged();
        txt.setText(null);
    }

    /**
     * Load the conversation list from Parse server and save the date of last
     * message that will be used to load only recent new messages
     */
    private void loadConversationList() {

        FirebaseDatabase.getInstance().getReference("messages").child(users[0] + "_" + users[1]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Conversation conversation = ds.getValue(Conversation.class);
                        if (conversation.getReceiver().contentEquals(user.getUid()) || conversation.getSender().contentEquals(user.getUid())) {
                            convList.add(conversation);
                            if (lastMsgDate == null
                                    || lastMsgDate.before(conversation.getDate()))
                                lastMsgDate = conversation.getDate();

                            adp.notifyDataSetChanged();

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * The Class ChatAdapter is the adapter class for Chat ListView. This
     * adapter shows the Sent or Receieved Chat message in each list item.
     */
    private class ChatAdapter extends BaseAdapter {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return convList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0) {
            return convList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @SuppressLint("InflateParams")
        @Override
        public View getView(int pos, View v, ViewGroup arg2) {
            Conversation c = getItem(pos);
            if (c.isSent()) {
                v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);

                if(userDpBitmap != null) {
                    Log.d("UserDP", "true");
                    BootstrapCircleThumbnail dp = (BootstrapCircleThumbnail) v.findViewById(R.id.userdp);
                    dp.setImageBitmap(userDpBitmap);
                }
            }
            else {
                v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);
                if(buddyDpBitmap != null) {
                    Log.d("BuddyDP", "true");
                    BootstrapCircleThumbnail dp = (BootstrapCircleThumbnail) v.findViewById(R.id.buddydp);
                    dp.setImageBitmap(buddyDpBitmap);
                }
            }

            TextView lbl = (TextView) v.findViewById(R.id.lbl1);
            lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c
                            .getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) v.findViewById(R.id.lbl3);
            if (c.isSent()) {
                if (c.getStatus() == Conversation.STATUS_SENT)
                    lbl.setText(R.string.delivered_text);
                else {
                    if (c.getStatus() == Conversation.STATUS_SENDING)
                        lbl.setText(R.string.sending_text);
                    else {
                        lbl.setText(R.string.failed_text);
                    }
                }
            } else
                lbl.setText("");
            return v;
        }

    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
