package org.doubango.imsdroid.Screens;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

/**
 * Created by gaojing on 16-4-27.
 */
public class ScreenQuickSettings extends BaseScreen {
    private final static String TAG = ScreenQuickSettings.class.getCanonicalName();
    private final INgnConfigurationService mConfigurationService;
    private final INgnSipService mSipService;

    private EditText mEtDisplayName;
    private EditText mEtIMPU;
    private EditText mEtIMPI;
    private EditText mEtPassword;
    private EditText mEtRealm;
    private CheckBox mCbEarlyIMS;
    private EditText mProxyHost;


    public ScreenQuickSettings() {
        super(SCREEN_TYPE.SCREEN_QUICK_SETTINGS_T, TAG);

        mConfigurationService = getEngine().getConfigurationService();
        mSipService = getEngine().getSipService();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_quick_settings);

        Button wifiQuickSetting = (Button)findViewById(R.id.screen_wifi_quick_setting);
        wifiQuickSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        mEtDisplayName = (EditText)findViewById(R.id.screen_identity_editText_displayname);
        mEtIMPU = (EditText)findViewById(R.id.screen_identity_editText_impu);
        mEtIMPI = (EditText)findViewById(R.id.screen_identity_editText_impi);
        mEtPassword = (EditText)findViewById(R.id.screen_identity_editText_password);
        mEtRealm = (EditText)findViewById(R.id.screen_identity_editText_realm);
        mCbEarlyIMS = (CheckBox)findViewById(R.id.screen_identity_checkBox_earlyIMS);
		mProxyHost = (EditText) findViewById(R.id.screen_network_editText_pcscf_host);
        
        mEtDisplayName.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME));
        mEtIMPU.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
        mEtIMPI.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mEtPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_PASSWORD, NgnStringUtils.emptyValue()));
        mEtRealm.setText(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
        mCbEarlyIMS.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));
        mProxyHost.setText(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));

        super.addConfigurationListener(mEtDisplayName);
        super.addConfigurationListener(mEtIMPU);
        super.addConfigurationListener(mEtIMPI);
        super.addConfigurationListener(mEtPassword);
        super.addConfigurationListener(mEtRealm);
        super.addConfigurationListener(mCbEarlyIMS);
		super.addConfigurationListener(mProxyHost);
	}	

	protected void onPause() {
		if(super.mComputeConfiguration){

            String impi = mEtIMPI.getText().toString();
            String networkRealm = mEtRealm.getText().toString();
            String impu = "sip:" + impi + "@" + networkRealm;
            Log.d("GaoJing","00 ipmu = " + impu);



			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, 
					mEtDisplayName.getText().toString().trim());
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
                    impu);

			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
					mEtIMPI.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
					mEtPassword.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
					mEtRealm.getText().toString().trim());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
					mCbEarlyIMS.isChecked());
			mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
					mProxyHost.getText().toString().trim());

            if(mSipService.isRegistered()){
                Log.d("GaoJing", "already registered, should make it unregister firstly");
                mSipService.unRegister();
            }

			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
