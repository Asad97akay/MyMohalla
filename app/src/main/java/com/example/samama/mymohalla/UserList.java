package com.example.samama.mymohalla;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.example.samama.mymohalla.custom.CustomActivity;
import com.example.samama.mymohalla.model.ChatUser;
import com.example.samama.mymohalla.model.Conversation;
import com.example.samama.mymohalla.utils.Const;
import com.example.samama.mymohalla.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The Class UserList is the Activity class. It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */
public class UserList extends CustomActivity
{

	/** Users database reference */
	DatabaseReference database;
	/** The Chat list. */
	private ArrayList<ChatUser> uList;

	/** The user. */
	public static ChatUser user;

	private static HashMap<String, Boolean> conversations;
	private HashMap<String, String> lastMsgs;
	private String[] tempConvo;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list);
		tempConvo = new String[2];

		// Get reference to the Firebase database
		database  = FirebaseDatabase.getInstance().getReference();

		android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
		if(mActionBar != null) {
			mActionBar.setDisplayShowHomeEnabled(false);
			mActionBar.setDisplayShowTitleEnabled(false);
		}
		LayoutInflater mInflater = LayoutInflater.from(this);

		View mCustomView = mInflater.inflate(R.layout.userlist_action_bar, null);
		TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);

		mActionBar.setCustomView(mCustomView);
		mActionBar.setDisplayShowCustomEnabled(true);

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		updateUserStatus(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		updateUserStatus(false);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		loadUserList();

	}

	/**
	 * Update user status.
	 *
	 * @param online
	 *            true if user is online
	 */
	private void updateUserStatus(boolean online)
	{
        database.child("users").child(user.getId()).child("online").setValue(online);
	}

	/**
	 * Load list of users.
	 */
	private void loadUserList()
	{
		final String currentUserId = user.getId();
		Log.d("CONVO", "loadUserList: currentUserId " + currentUserId);
		final ProgressDialog dia = ProgressDialog.show(this, null,
				getString(R.string.alert_loading));

		// Pull the users list once no sync required.
		database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				dia.dismiss();
				long size  = dataSnapshot.getChildrenCount();
				if(size == 0) {
					Toast.makeText(UserList.this,
							R.string.msg_no_user_found,
							Toast.LENGTH_SHORT).show();
					return;
				}
				uList = new ArrayList<ChatUser>();
				for(DataSnapshot ds : dataSnapshot.getChildren()) {
					ChatUser user = ds.getValue(ChatUser.class);
					Logger.getLogger(UserList.class.getName()).log(Level.ALL,user.getUsername());
					if(!user.getId().contentEquals(currentUserId))
						uList.add(user);
				}

				conversations = new HashMap<>();
				for(ChatUser x : uList){
					tempConvo[1] = x.getId();
					tempConvo[0] = currentUserId;
					Arrays.sort(tempConvo);
					conversations.put(tempConvo[0] + "_" + tempConvo[1], false);
				}

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

		lastMsgs = new HashMap<>();
		database.child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				//Iterating every single conversation.
				for(DataSnapshot ds : dataSnapshot.getChildren()){
					Log.d("USERLIST", "onDataChange: Loop Works");

					//Checking if user_user key exists in database.
					if(conversations.containsKey(ds.getKey())){
						Log.d("USERLIST", "onDataChange: Condition True");
						//False: No conversation by the corresponding user_user key
						conversations.remove(ds.getKey());
						conversations.put(ds.getKey(), true);

						Conversation temp = ds.child("lastMessage").getValue(Conversation.class);

						lastMsgs.put(ds.getKey(), temp.getMsg() + "#" + DateUtils.getRelativeDateTimeString(UserList.this, temp
										.getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
								DateUtils.DAY_IN_MILLIS, 0));
						Log.d("SIZE", String.valueOf(lastMsgs.size()));
					}
				}
				ListView list = (ListView) findViewById(R.id.list);
				list.setAdapter(new UserAdapter());
				list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0,
											View arg1, int pos, long arg3)
					{
						startActivity(new Intent(UserList.this,
								Chat.class).putExtra(
								Const.EXTRA_DATA,  uList.get(pos))
								.putExtra("profilepicturebuddy", uList.get(pos).getProfilePicture()));
					}
				});
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}

	/**
	 * The Class UserAdapter is the adapter class for User ListView. This
	 * adapter shows the user name and it's only online status for each item.
	 */
	private class UserAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return uList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public ChatUser getItem(int arg0)
		{
			return uList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0)
		{
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2)
		{
			if (v == null)
				v = getLayoutInflater().inflate(R.layout.chat_item, null);

			String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
			ChatUser c = getItem(pos);
			myMohallaTextView lbl = (myMohallaTextView) v.findViewById(R.id.textView);
			myMohallaTextView msg = (myMohallaTextView) v.findViewById(R.id.message);
			myMohallaTextView time = (myMohallaTextView) v.findViewById(R.id.time);
			BootstrapCircleThumbnail profilePicture = (BootstrapCircleThumbnail) v.findViewById(R.id.profilepicture);

			String[] newConvo = new String[2];
			newConvo[0] = c.getId();
			newConvo[1] = currentUser;
			Arrays.sort(newConvo);

			String newConvo1 = newConvo[0] + "_" + newConvo[1];

			//Removing user_user pairs for which no conversations exists in DB.
			conversations.values().removeAll(Collections.singleton(false));

			if(lastMsgs != null && conversations.containsKey(newConvo1)) {
				Log.d("USERS", c.getUsername() + " in true condition");
				String[] msgDateBreaker = lastMsgs.get(newConvo1).split("#");
//				if(lastMsgs.get(c.getId()) != null){
//					msgDateBreaker = lastMsgs.get(c.getId()).split("#");
//				}
//				else{
//					msgDateBreaker = lastMsgs.get(currentUser).split("#");
//				}
				lbl.setText(c.getUsername());
				msg.setText(msgDateBreaker[0]);
				String[] dateTimerBreaker = msgDateBreaker[1].split(",");
				time.setText(dateTimerBreaker[0] + "\n" + dateTimerBreaker[1]);
				msg.setTextColor(ResourcesCompat.getColor(getResources(), R.color.bootstrap_gray_light, null));
			}
			else{
				Log.d("USERS", c.getUsername() + " in false condition");
				lbl.setText(c.getUsername());
				msg.setText(getString(R.string.new_chat));
				msg.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
			}

			if(c.getProfilePicture() != null) {
				Log.d("USERNAME", c.getUsername());
				Log.d("PROFILE PIC", c.getProfilePicture());
				byte[] decodedString = Base64.decode(c.getProfilePicture(), Base64.DEFAULT);
				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				profilePicture.setImageBitmap(decodedByte);
			}

			lbl.setCompoundDrawablesWithIntrinsicBounds(
					c.isOnline() ? R.drawable.ic_online
							: R.drawable.ic_offline, 0, R.drawable.arrow, 0);

			return v;
		}

	}
}
