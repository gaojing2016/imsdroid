/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.CustomDialog;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;


public class ScreenHome extends BaseScreen {
	private static String TAG = ScreenHome.class.getCanonicalName();
	
	private static final int MENU_EXIT = 0;
	private static final int MENU_SETTINGS = 1;
	
	private GridView mGridView;
	
	private final INgnSipService mSipService;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	public ScreenHome() {
		super(SCREEN_TYPE.HOME_T, TAG);
		
		mSipService = getEngine().getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_home);
		
		mGridView = (GridView) findViewById(R.id.screen_home_gridview);
		mGridView.setAdapter(new ScreenHomeAdapter(this));
        
        if(!mSipService.isRegistered()){
            mSipService.register(ScreenHome.this); //add by gaojing:register default
        }

		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				final ScreenHomeItem item = (ScreenHomeItem)parent.getItemAtPosition(position);
				if (item != null) {
					Log.d("GaoJIng", "item is ("+item+")");
					Log.d("GaoJing", "position is ("+position+")");
					
                    if(position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS){
						if(mSipService.getRegistrationState() == ConnectionState.CONNECTING || mSipService.getRegistrationState() == ConnectionState.TERMINATING){
                            Log.d("GaoJing", "test01");
							mSipService.stopStack();
						}
						else if(mSipService.isRegistered()){
                            Log.d("GaoJing", "test02");
							mSipService.unRegister();
						}
						else{
                            Log.d("GaoJing", "test03");
							mSipService.register(ScreenHome.this);
						}
					}
					/*
					else if(position == ScreenHomeItem.ITEM_EXIT_POS){
						final AlertDialog dialog = CustomDialog.create(
								ScreenHome.this,
								R.drawable.exit_48,
								null,
								"Are you sure you want to exit?",
								"Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										((Main)(getEngine().getMainActivity())).exit();
									}
								}, "No",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								});
						dialog.show();
					}
					*/
					else if ((position == ScreenHomeItem.ITEM_CONTACT_1_POS)
							|| (position == ScreenHomeItem.ITEM_CONTACT_2_POS)
							|| (position == ScreenHomeItem.ITEM_CONTACT_3_POS)
							|| (position == ScreenHomeItem.ITEM_CONTACT_4_POS)){
						final AlertDialog dialog = CustomDialog.create(ScreenHome.this,
								R.drawable.phone_call_25,
								null,
								"Are you sure you want to call this person?",
								"Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Log.d("GaoJing", "prepare to call??" );
										//((Main)(getEngine().getMainActivity())).exit();
										int num = position + 1;
										final String phoneNum = "100" + num;
										Log.d("GaoJing", "phone num is ("+phoneNum+")");
										if(!mSipService.isRegistered()) {
                                            mSipService.register(ScreenHome.this); //add by gaojing:register default
										}
										else {
											ScreenAV.makeCall(phoneNum, NgnMediaType.AudioVideo);
											//Toast.makeText(ScreenHome.this, "Please register firstly", Toast.LENGTH_LONG).show();
										}
									}
								},
								"No",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Log.d("GaoJing", "prepare to not call??");
										dialog.cancel();
									}
								}
								);
						dialog.show();
					}
					else{					
						mScreenService.show(item.mClass, item.mClass.getCanonicalName());
					}
				}
			}
		});
		
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_NOK:
						case UNREGISTRATION_OK:
						case REGISTRATION_OK:
						case REGISTRATION_INPROGRESS:
						case UNREGISTRATION_INPROGRESS:
						case UNREGISTRATION_NOK:
						default:
							((ScreenHomeAdapter)mGridView.getAdapter()).refresh();
							break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
	}

	@Override
	protected void onDestroy() {
       if(mSipBroadCastRecv != null){
    	   unregisterReceiver(mSipBroadCastRecv);
    	   mSipBroadCastRecv = null;
       }
        
       super.onDestroy();
	}
	
	@Override
	public boolean hasMenu() {
		return true;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu) {
		menu.add(0, ScreenHome.MENU_SETTINGS, 0, "Settings");
		/*MenuItem itemExit =*/ menu.add(0, ScreenHome.MENU_EXIT, 0, "Exit");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "itemid is ("+item.getItemId()+")");
		switch(item.getItemId()){
			case ScreenHome.MENU_EXIT:
				((Main)getEngine().getMainActivity()).exit();
				break;
			case ScreenHome.MENU_SETTINGS:
				mScreenService.show(ScreenSettings.class);
				break;
			default:
				Log.d(TAG, "itemid is ("+item.getItemId()+")");
		}
		return true;
	}
	
	
	/**
	 * ScreenHomeItem
	 */
	static class ScreenHomeItem {
		static final int ITEM_SIGNIN_SIGNOUT_POS = 4;
		//static final int ITEM_EXIT_POS = 5;
		static final int ITEM_CONTACT_1_POS = 0;
		static final int ITEM_CONTACT_2_POS = 1;
		static final int ITEM_CONTACT_3_POS = 2;
		static final int ITEM_CONTACT_4_POS = 3;

		final int mIconResId;
		final String mText;
		final Class<? extends Activity> mClass;

		private ScreenHomeItem(int iconResId, String text, Class<? extends Activity> _class) {
			mIconResId = iconResId;
			mText = text;
			mClass = _class;
		}
	}
	
	/**
	 * ScreenHomeAdapter
	 */
	static class ScreenHomeAdapter extends BaseAdapter{
		static final int ALWAYS_VISIBLE_ITEMS_COUNT = 6;
		static final ScreenHomeItem[] sItems =  new ScreenHomeItem[]{
			// always visible
			new ScreenHomeItem(R.drawable.eab_48, "1001", null),
			new ScreenHomeItem(R.drawable.eab_48, "1002", null),
			new ScreenHomeItem(R.drawable.eab_48, "1003", null),
			new ScreenHomeItem(R.drawable.eab_48, "1004", null),
    		new ScreenHomeItem(R.drawable.sign_in_48, "Sign In", null),
    		//new ScreenHomeItem(R.drawable.exit_48, "Exit/Quit", null),
    		//new ScreenHomeItem(R.drawable.options_48, "Options", ScreenSettings.class),
			new ScreenHomeItem(R.drawable.options_48, "QuickSettings", ScreenQuickSettings.class),
			//new ScreenHomeItem(R.drawable.about_48, "About", ScreenAbout.class),
			// visible only if connected
    		//new ScreenHomeItem(R.drawable.dialer_48, "Dialer", ScreenTabDialer.class),
    		//new ScreenHomeItem(R.drawable.eab2_48, "Address Book", ScreenTabContacts.class),
    		//new ScreenHomeItem(R.drawable.history_48, "History", ScreenTabHistory.class),
    		//new ScreenHomeItem(R.drawable.chat_48, "Messages", ScreenTabMessages.class),
		};
		
		private final LayoutInflater mInflater;
		private final ScreenHome mBaseScreen;
		
		ScreenHomeAdapter(ScreenHome baseScreen){
			mInflater = LayoutInflater.from(baseScreen);
			mBaseScreen = baseScreen;
		}
		
		void refresh(){
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mBaseScreen.mSipService.isRegistered() ? sItems.length : ALWAYS_VISIBLE_ITEMS_COUNT;
		}

		@Override
		public Object getItem(int position) {
			return sItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ScreenHomeItem item = (ScreenHomeItem)getItem(position);
			
			if(item == null){
				return null;
			}

			if (view == null) {
				view = mInflater.inflate(R.layout.screen_home_item, null);
			}
			
			if(position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS){
				if(mBaseScreen.mSipService.getRegistrationState() == ConnectionState.CONNECTING || mBaseScreen.mSipService.getRegistrationState() == ConnectionState.TERMINATING){
					((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Cancel");
					((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.sign_inprogress_48);
				}
				else{
					if(mBaseScreen.mSipService.isRegistered()){
						((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Sign Out");
						((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.sign_out_48);
					}
					else{
						((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Sign In");
						((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.sign_in_48);
					}
				}
			}
			else{				
				((TextView) view.findViewById(R.id.screen_home_item_text)).setText(item.mText);
				((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(item.mIconResId);
			}
			
			return view;
		}
		
	}
}
