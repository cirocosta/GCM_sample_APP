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
	private static int sNotificationId = 0;

	public MeuGcmService() {
		super("MeuGcmService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		processaMensagem(extras);
		MeuGcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void processaMensagem(Bundle extras) {
		if (extras.containsKey(Constants.BUNDLE_MESSAGE)) {
			sendNotification(extras.getString(Constants.BUNDLE_MESSAGE));
		}
	}

	private void sendNotification(String message) {
		Context context = getApplicationContext();
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setTicker(getString(R.string.app_name))
				.setContentTitle(getString(R.string.app_name))
				.setContentInfo(getString(R.string.notif_teste))
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
		mNotificationManager.notify(sNotificationId, mBuilder.build());
		sNotificationId++;
	}

}
