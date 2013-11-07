package com.cirocosta.gcmtestapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import android.util.Log;

public class PostData{
	/* THIS MUST NOT BE USED IN THE MAIN THREAD */

	private HashMap<String,String> data;
	private String endereco;

	public PostData(String endereco, HashMap<String,String> data) {
		this.data = data;
		this.endereco = endereco;
	}

	public String sendData() throws Exception {
		try {
			URL url = new URL(endereco);
			StringBuffer param = new StringBuffer();
			int i = 0;
			for(Map.Entry<String,String> entry : data.entrySet()){
				if(i>0){
					param.append("&" + entry.getKey() + "=");
				} else {
					param.append(entry.getKey() + "=");
				}
				param.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
				i++;
			}
			String params = param.toString();			
			Log.v("POSTDATA",params);
			HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setFixedLengthStreamingMode(params.getBytes().length);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.print(params);
			out.close();

			String response = "";
			Scanner inStream = new Scanner(conn.getInputStream());
			while (inStream.hasNextLine()) {
				response += (inStream.nextLine());
			}
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
