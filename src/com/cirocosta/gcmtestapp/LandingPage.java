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

public class LandingPage extends Activity {

	private Button bIr;
	private EditText etCodigo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landing_page);
		initialize();

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
		bIr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vaiParaMain(etCodigo);
			}
		});
	}

	private void vaiParaMain(EditText et) {
		String sCodigo = et.getText().toString();
		if (sCodigo.length() > 0) {
			Intent i = new Intent(LandingPage.this, MainActivity.class);
			i.putExtra("codigo", sCodigo);
			startActivity(i);
			return;
		}
		Toast.makeText(LandingPage.this, "Insira o código gerado", Toast.LENGTH_LONG)
				.show();
	}

	private void initialize() {
		bIr = (Button) findViewById(R.id.landing_bIr);
		etCodigo = (EditText) findViewById(R.id.landing_etCodigo);
	}

}
