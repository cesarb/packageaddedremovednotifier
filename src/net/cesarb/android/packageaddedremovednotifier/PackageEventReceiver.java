package net.cesarb.android.packageaddedremovednotifier;

import net.cesarb.android.packageaddedremovednotifier.PackageDatabase.PackageEvent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;

public class PackageEventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		long timestamp = System.currentTimeMillis();

		String packageName = getPackageName(intent);
		if (packageName == null)
			return;

		boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

		String intentAction = intent.getAction();
		if (Intent.ACTION_PACKAGE_ADDED.equals(intentAction)) {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = getPackageInfo(packageManager, packageName);
			String applicationLabel = getApplicationLabel(packageManager, packageInfo);

			if (!replacing)
				packageAdded(context, timestamp, packageName, packageInfo, applicationLabel);
			else
				packageReplaced(context, timestamp, packageName, packageInfo, applicationLabel);
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intentAction)) {
			if (!replacing)
				packageRemoved(context, timestamp, packageName);
		}
	}

	private String getPackageName(Intent intent) {
		Uri intentData = intent.getData();
		if (intentData == null || !"package".equals(intentData.getScheme()))
			return null;
		return intentData.getSchemeSpecificPart();
	}

	private PackageInfo getPackageInfo(PackageManager packageManager, String packageName) {
		try {
			return packageManager.getPackageInfo(packageName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	private String getApplicationLabel(PackageManager packageManager, PackageInfo packageInfo) {
		if (packageInfo == null)
			return null;

		ApplicationInfo applicationInfo = packageInfo.applicationInfo;
		if (applicationInfo == null)
			return null;

		CharSequence applicationLabel = packageManager.getApplicationLabel(applicationInfo);
		if (applicationLabel == null)
			return null;

		return applicationLabel.toString();
	}

	private void packageAdded(Context context, long timestamp, String packageName, PackageInfo packageInfo, String applicationLabel) {
		long id = insertPackage(context, timestamp, true, false, packageName, packageInfo, applicationLabel);

		String contentText = getNotificationContentText(packageName, packageInfo);
		Notification notification = createNotification(context, "added", contentText, timestamp);
		notify(context, id, notification);
	}

	private void packageReplaced(Context context, long timestamp, String packageName, PackageInfo packageInfo, String applicationLabel) {
		long id = insertPackage(context, timestamp, true, true, packageName, packageInfo, applicationLabel);

		String contentText = getNotificationContentText(packageName, packageInfo);
		Notification notification = createNotification(context, "updated", contentText, timestamp);
		notify(context, id, notification);
	}

	private void packageRemoved(Context context, long timestamp, String packageName) {
		long id = insertPackage(context, timestamp, false, false, packageName, null, null);

		Notification notification = createNotification(context, "removed", packageName, timestamp);
		notify(context, id, notification);
	}

	private long insertPackage(Context context, long timestamp, boolean added, boolean replacing, String packageName, PackageInfo packageInfo, String applicationLabel) {
		ContentValues values = new ContentValues();

		values.put(PackageEvent.EVENT_TIMESTAMP, timestamp);
		values.put(PackageEvent.EVENT_ADDED, added);
		values.put(PackageEvent.EVENT_REPLACING, replacing);
		values.put(PackageEvent.PACKAGE_NAME, packageName);

		if (packageInfo != null) {
			values.put(PackageEvent.PACKAGE_VERSION_CODE, packageInfo.versionCode);
			values.put(PackageEvent.PACKAGE_VERSION_NAME, packageInfo.versionName);
		}
		if (applicationLabel != null)
			values.put(PackageEvent.APPLICATION_LABEL, applicationLabel);

		Uri contentUri = context.getContentResolver().insert(PackageEvent.CONTENT_URI, values);
		if (contentUri == null)
			return 0;

		return ContentUris.parseId(contentUri);
	}

	private static String getNotificationContentText(String packageName, PackageInfo packageInfo) {
		return packageInfo != null ? packageName + " " + packageInfo.versionName : packageName;
	}

	private Notification createNotification(Context context, String action, String contentText, long timestamp) {
		Intent intent = makeRestartActivityTask(new ComponentName(context, MainActivity.class));
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

		String message = "Package " + action;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.ic_stat_notify_package);
		builder.setTicker(message);
		builder.setWhen(timestamp);
		builder.setContentTitle(message);
		builder.setContentText(contentText);
		builder.setContentIntent(contentIntent);
		builder.setAutoCancel(true);
		return builder.getNotification();
	}

	// Missing from android.support.v4.content.IntentCompat
	private static Intent makeRestartActivityTask(ComponentName mainActivity) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(mainActivity);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
		return intent;
	}

	private void notify(Context context, long id, Notification notification) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify((int) id, notification);
	}

}
