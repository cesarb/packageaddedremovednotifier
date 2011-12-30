package net.cesarb.android.packageaddedremovednotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class PackageAddedRemovedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
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
				packageAdded(context, packageName, packageInfo, applicationLabel);
			else
				packageReplaced(context, packageName, packageInfo, applicationLabel);
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intentAction)) {
			if (!replacing)
				packageRemoved(context, packageName);
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

	private void packageAdded(Context context, String packageName, PackageInfo packageInfo, String applicationLabel) {
		String contentText = packageInfo != null ? packageName + " " + packageInfo.versionName : packageName;
		Notification notification = createNotification(context, "added", contentText);
		notify(context, notification);
	}

	private void packageReplaced(Context context, String packageName, PackageInfo packageInfo, String applicationLabel) {
		String contentText = packageInfo != null ? packageName + " " + packageInfo.versionName : packageName;
		Notification notification = createNotification(context, "updated", contentText);
		notify(context, notification);
	}

	private void packageRemoved(Context context, String packageName) {
		Notification notification = createNotification(context, "removed", packageName);
		notify(context, notification);
	}

	private Notification createNotification(Context context, String action, String contentText) {
		Intent intent = new Intent(context, PackageAddedRemovedNotifier.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

		String message = "Package " + action;
		Notification notification = new Notification(R.drawable.ic_stat_notify_package, message, System.currentTimeMillis());
		notification.setLatestEventInfo(context, message, contentText, contentIntent);
		return notification;
	}

	private void notify(Context context, Notification notification) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}

}
