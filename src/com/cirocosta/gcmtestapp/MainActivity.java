package com.cirocosta.gcmtestapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {
	/*
	 * SENDER_ID trata-se do ID do projeto em https://code.google.com/apis/console
	 * tento Google Cloud Messaging for Android habilitado.
	 */
	
	private TextView tvStatus, tvCheckPlay, tvGetRegid, tvRegInBack,
			tvReadyReceive;
	private ProgressBar pbBar;
	private static final String TAG = "MainActivity";
	private Context context;
	private int custom_green, custom_red;
	private String codigo;

	private final static String GCM_TESTING_URL = "http://apresentae.appspot.com/gcm_testing/registro";

	// GCM
	private final static String SENDER_ID = "SEU_SENDER_ID";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	// GCM - Keys for SharedPrefs
	public final static String PROPERTY_REG_ID = "registration_id";
	private final static String PROPERTY_APP_VERSION = "appVersion";

	GoogleCloudMessaging gcm;
	AtomicInteger messageID = new AtomicInteger();
	SharedPrefsHelper prefsHelper;
	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);
		initialize();
		codigo = getIntent().getStringExtra("codigo");
		context = getApplicationContext();

		if (checkPlayServices()) {
			marcaItem(tvCheckPlay, true);
			prefsHelper = new SharedPrefsHelper(context);
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			marcaItem(tvGetRegid, true);
			if (regid.equals("")) {
				registerInBackground(context);
			} else {
				tvStatus.setText("Já foi registrado");
				tvStatus.setTextColor(custom_red);
				pbBar.setVisibility(View.GONE);
			}
		} else {
			marcaItem(tvCheckPlay, false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}

	public String getRegistrationId(Context context) {
		String registration_id = prefsHelper.getStringFromKey(PROPERTY_REG_ID);
		if (registration_id.equals("")) {
			return "";
		}
		int registeredVersion = prefsHelper.getIntFromKey(PROPERTY_APP_VERSION);
		int currentVersion = getCurrentAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registration_id;
	}

	public int getCurrentAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// rarely happens (should not happen)
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public void registerInBackground(final Context mContext) {
		new AsyncTask<Void, Void, String>() {

			boolean status_registro;

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(mContext);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registrado, registration_id = " + regid;
					if (sendRegistrationToBackend(regid)) {
						storeRegistrationId(context, regid);
						status_registro = true;
					} else {
						status_registro = false;
					}
				} catch (IOException ex) {
					msg = "Error: " + ex.getMessage();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if (status_registro) {
					marcaItem(tvRegInBack, true);
					marcaItem(tvReadyReceive, true);
				} else {
					marcaItem(tvRegInBack, false);
					marcaItem(tvReadyReceive, false);
				}
				pbBar.setVisibility(View.GONE);
			}

		}.execute();
	}

	private boolean sendRegistrationToBackend(String registration_id) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("codigo", codigo);
		data.put("regid", registration_id);
		data.put("modelo", getDeviceName());
		PostData pd = new PostData(GCM_TESTING_URL, data);
		try {
			String response = pd.sendData();
			Log.v(TAG,response);
			if (!response.equals("") && !response.equals("error")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER.toUpperCase();
		String model = Build.MODEL.toUpperCase();
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	private void storeRegistrationId(Context context, String registration_id) {
		int app_version = getAppVersion();
		Editor editor = prefsHelper.getEditor();
		editor.putString(PROPERTY_REG_ID, registration_id);
		editor.putInt(PROPERTY_APP_VERSION, app_version);
		editor.commit();
	}

	private int getAppVersion() {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(
					"Nao foi possivel obter o nome de pacote: " + e);
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	private void marcaItem(TextView item, boolean status) {
		if (status) {
			item.setTextColor(custom_green);
			return;
		}
		item.setTextColor(custom_red);
	}

	private void initialize() {
		tvStatus = (TextView) findViewById(R.id.main_tvStatus);
		pbBar = (ProgressBar) findViewById(R.id.main_pbBar);
		tvCheckPlay = (TextView) findViewById(R.id.main_tvCheckPlay);
		tvGetRegid = (TextView) findViewById(R.id.main_tvGetRegid);
		tvRegInBack = (TextView) findViewById(R.id.main_tvRegInBack);
		tvReadyReceive = (TextView) findViewById(R.id.main_tvReadyReceive);

		custom_green = Color.parseColor("#006600");
		custom_red = Color.parseColor("#800000");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_info:
			return true;
		case R.id.menu_trocarCodigo:
			SharedPrefsHelper prefs = new SharedPrefsHelper(MainActivity.this);
			prefs.clearSharedPreferences();
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
