package com.cirocosta.gcmtestapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class MeuGcmService extends IntentService {

	private static final String TAG = "MeuGcmService";

	public MeuGcmService() {
		super("MeuGcmService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		// processamento como desejar
		processaMensagem(extras);
		MeuGcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void processaMensagem(Bundle extras) {
		if (extras.containsKey("message")) {
			sendNotification(extras.getString("message"));
		}
	}

	private void sendNotification(String message) {
		Context context = getApplicationContext();
		int notificationId = 0;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setTicker("GCM Teste").setContentTitle("GCM Teste").setContentInfo("Teste de notificacao")
				.setContentText(message);
		mBuilder.setLights(Color.WHITE, 1000, 1000);
		mBuilder.setSound(RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		Intent intent = new Intent();
		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				context.getApplicationContext(), 0, intent, 0);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(notificationId, mBuilder.build());
		notificationId++;
	}

}
