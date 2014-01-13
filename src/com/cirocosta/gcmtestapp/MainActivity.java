package com.cirocosta.gcmtestapp;

import java.io.IOException;
import java.util.HashMap;

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

/**
 * Atividade principal do Aplicativo. Ira fazer os testes de presenca do Google
 * Play Services. Se tudo ocorrer bem entao todos os processos estarao verdes e
 * o aparelho estara entao pronto para receber notificacoes
 */
public class MainActivity extends Activity {

	private TextView tvStatus, tvCheckPlay, tvGetRegid, tvRegInBack,
			tvReadyReceive;
	private ProgressBar pbBar;
	private static final String TAG = "MainActivity";
	private static final int custom_green, custom_red;

	private final static String GCM_TESTING_URL = 
			"http://apresentae.appspot.com/gcm_testing/registro";

	private String SENDER_ID;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public final static String PROPERTY_REG_ID = "registration_id";
	private final static String PROPERTY_APP_VERSION = "appVersion";

	private Context mContext;
	private String mCodigo;
	GoogleCloudMessaging mGcm;
	SharedPrefsHelper mPrefsHelper;
	String mRegid;

	static {
		custom_green = Color.parseColor("#006600");
		custom_red = Color.parseColor("#800000");
	}

	/**
	 * Inicia a atividade.
	 * gcm_sender_id esta localizado em res/values/secrets.xml, onde os
	 * keys sao guardados (.gitignore anula a publicacao da pasta)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		SENDER_ID = getString(R.string.gcm_sender_id);
		setContentView(R.layout.activity_main);
		initialize();
		mCodigo = getIntent().getStringExtra("codigo");
		mContext = getApplicationContext();

		if (checkPlayServices()) {
			marcaItem(tvCheckPlay, true);
			mPrefsHelper = new SharedPrefsHelper(mContext);
			mGcm = GoogleCloudMessaging.getInstance(this);
			mRegid = getRegistrationId(mContext);
			marcaItem(tvGetRegid, true);
			if (mRegid.equals("")) {
				registerInBackground(mContext);
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

	/**
	 * Obtem o codigo de registro do GCM guardado
	 * 
	 * @param mContext
	 * @return registration_id ou ""
	 */
	public String getRegistrationId(Context mContext) {
		String registration_id = mPrefsHelper.getStringFromKey(PROPERTY_REG_ID);
		if (registration_id.equals("")) {
			return "";
		}
		int registeredVersion = mPrefsHelper
				.getIntFromKey(PROPERTY_APP_VERSION);
		int currentVersion = getCurrentAppVersion(mContext);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registration_id;
	}

	/**
	 * Obtem a versao atual do aplicativo. Necessario para enviar diferentes
	 * tipos de mensagens dependendo da versao do aplicativo do cliente
	 * 
	 * @param mContext
	 * @return Versao do aplicativo
	 */
	public int getCurrentAppVersion(Context mContext) {
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(mContext.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// rarely happens (should not happen)
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Prepare e executa o envio do registro para o backend. Se correto, salva o
	 * codigo de registro, caso contrario, nao.
	 * 
	 * @param mContext
	 *            mContexto da aplicacao
	 */
	public void registerInBackground(final Context mContext) {
		new AsyncTask<Void, Void, String>() {

			boolean status_registro;

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (mGcm == null) {
						mGcm = GoogleCloudMessaging.getInstance(mContext);
					}
					mRegid = mGcm.register(SENDER_ID);
					msg = "Device registrado, registration_id = " + mRegid;
					if (sendRegistrationToBackend(mRegid)) {
						storeRegistrationId(mContext, mRegid);
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

	/**
	 * Executa um POST para o webservice para o registro temporario do aparelho
	 * para que possamos enviar a ele a notificacao.
	 * 
	 * @param registration_id
	 * @return True -- conseguiu
	 * @return False -- falhou
	 */
	private boolean sendRegistrationToBackend(String registration_id) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("codigo", mCodigo);
		data.put("mRegid", registration_id);
		data.put("modelo", getDeviceName());
		PostData pd = new PostData(GCM_TESTING_URL, data);
		try {
			String response = pd.sendData();
			Log.v(TAG, response);
			if (!response.equals("") && !response.equals("error")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Obtem o Nome do aparelho junto a seu fabricante
	 * 
	 * @return exemplo: SAMSUNG I9300GT
	 */
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER.toUpperCase();
		String model = Build.MODEL.toUpperCase();
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	/**
	 * Guarda o codigo de registro do aparelho no SharedPreferences para nao ser
	 * preciso gera-lo toda vez que a atividade rodar.
	 * 
	 * @param mContext
	 *            Contexto da aplicacao
	 * @param registration_id
	 *            'mRegid' para guardar
	 */
	private void storeRegistrationId(Context mContext, String registration_id) {
		int app_version = getAppVersion();
		Editor editor = mPrefsHelper.getEditor();
		editor.putString(PROPERTY_REG_ID, registration_id);
		editor.putInt(PROPERTY_APP_VERSION, app_version);
		editor.commit();
	}

	private int getAppVersion() {
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(mContext.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(
					"Nao foi possivel obter o nome de pacote: " + e);
		}
	}

	/**
	 * Verifica se o cliente possui o Google Play Services instalado. Caso
	 * contrário irá emitir um dialog padrão para que o usuário baixe.
	 * 
	 * @return true -- possui
	 * @return false -- nao foi possivel obter
	 */
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

	/**
	 * Altera a cor da textview dependendo de seu status
	 * 
	 * @param item
	 *            TextView a ser marcada com verde ou vermelho
	 * @param status
	 *            true: Verde false: Vermelho
	 */
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
