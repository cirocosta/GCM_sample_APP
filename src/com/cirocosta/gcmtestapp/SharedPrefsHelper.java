package com.cirocosta.gcmtestapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefsHelper {
		
	private static final int PRIVATE_MODE = 0;
	private static final String PREF_NAME = "MyPrefs";

	SharedPreferences pref;
	Editor editor;
	Context context;
	
	public SharedPrefsHelper(Context context){
		this.context = context;
		pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	public Editor getEditor(){
		return editor;
	}
	
	public int getIntFromKey(String key){
		return pref.getInt(key, -1);
	}
	
	public String getStringFromKey(String key){
		return pref.getString(key, "");
	}
	
	public boolean putString(String key, String value){
		editor.putString(key, value);
		return editor.commit();
	}

	public void clearSharedPreferences() {
		editor.clear();
		editor.commit();
	}
	
}
