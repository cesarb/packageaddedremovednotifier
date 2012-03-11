package net.cesarb.android.packageaddedremovednotifier;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PackageDatabase {

	private PackageDatabase() { }

	public static final class PackageEvent implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://net.cesarb.android.packageaddedremovednotifier.packageaddedremovedprovider/events");
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.cesarb.packageaddedremovednotifier.package";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.cesarb.packageaddedremovednotifier.package";

		public static final String PACKAGE_NAME = "packagename";
		public static final String PACKAGE_VERSION_CODE = "versioncode";

		public static final String APPLICATION_LABEL = "applicationlabel";
		public static final String PACKAGE_VERSION_NAME = "versionname";

		public static final String EVENT_ADDED = "added";
		public static final String EVENT_REPLACING = "replacing";
		public static final String EVENT_TIMESTAMP = "timestamp";

		private PackageEvent() { }

	}

}
