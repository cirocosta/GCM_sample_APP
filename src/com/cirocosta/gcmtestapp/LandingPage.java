package com.cirocosta.gcmtestapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * Atividade inicial
 */
public class LandingPage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landing_page);
		final EditText etCodigo = (EditText) findViewById(R.id.landing_etCodigo);
		etCodigo.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					vaiParaMain(etCodigo);
				}
				return false;
			}
		});
		((Button) findViewById(R.id.landing_bIr))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						vaiParaMain(etCodigo);
					}
				});
	}

	/**
	 * Vai para atividade principal carregando o codigo colocado
	 */
	private void vaiParaMain(EditText et) {
		final String codigo = et.getText().toString();
		if (codigo.length() > 0) {
			Intent i = new Intent(LandingPage.this, MainActivity.class);
			i.putExtra(Constants.INTENT_CODIGO, codigo);
			startActivity(i);
			return;
		}
		Toast.makeText(LandingPage.this,
				getString(R.string.landing_insirir_codigo), Toast.LENGTH_LONG)
				.show();
	}

}
