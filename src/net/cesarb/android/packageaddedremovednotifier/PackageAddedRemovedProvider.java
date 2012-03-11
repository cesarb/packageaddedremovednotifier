package net.cesarb.android.packageaddedremovednotifier;

import net.cesarb.android.packageaddedremovednotifier.PackageDatabase.PackageEvent;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.v4.database.DatabaseUtilsCompat;

public class PackageAddedRemovedProvider extends ContentProvider {

	private static final String EVENTS_TABLE = "events";

	private static class DatabaseOpenHelper extends SQLiteOpenHelper {

		DatabaseOpenHelper(Context context) {
			super(context, "packages.db", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + EVENTS_TABLE + " ("
					+ PackageEvent._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PackageEvent.PACKAGE_NAME + " TEXT NOT NULL,"
					+ PackageEvent.PACKAGE_VERSION_CODE + " INTEGER,"
					+ PackageEvent.APPLICATION_LABEL + " TEXT,"
					+ PackageEvent.PACKAGE_VERSION_NAME + " TEXT,"
					+ PackageEvent.EVENT_ADDED + " INTEGER NOT NULL,"
					+ PackageEvent.EVENT_REPLACING + " INTEGER NOT NULL,"
					+ PackageEvent.EVENT_TIMESTAMP + " INTEGER NOT NULL"
					+ ")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Nothing to do
		}

	}

	private DatabaseOpenHelper databaseOpenHelper;

	private static final UriMatcher matcher;
	private static final int PACKAGES = 1;
	private static final int PACKAGE_ID = 2;

	static {
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		String authority = PackageEvent.CONTENT_URI.getAuthority();
		matcher.addURI(authority, "events", PACKAGES);
		matcher.addURI(authority, "events/#", PACKAGE_ID);
	}

	private String getIdSelection(Uri uri) {
		return PackageEvent._ID + "=" + uri.getPathSegments().get(1);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (matcher.match(uri)) {
		case PACKAGES:
			break;
		case PACKAGE_ID:
			selection = DatabaseUtilsCompat.concatenateWhere(selection, getIdSelection(uri));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		int ret = db.delete(EVENTS_TABLE, selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		return ret;
	}

	@Override
	public String getType(Uri uri) {
		switch (matcher.match(uri)) {
		case PACKAGES:
			return PackageEvent.CONTENT_TYPE;
		case PACKAGE_ID:
			return PackageEvent.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (matcher.match(uri) != PACKAGES)
			throw new IllegalArgumentException("Unknown URI " + uri);

		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		long rowId = db.insert(EVENTS_TABLE, null, values);
		if (rowId < 0)
			return null;

		Uri newUri = ContentUris.withAppendedId(uri, rowId);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}

	@Override
	public boolean onCreate() {
		databaseOpenHelper = new DatabaseOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (sortOrder == null)
			sortOrder = PackageEvent._ID + " DESC";

		switch (matcher.match(uri)) {
		case PACKAGES:
			break;
		case PACKAGE_ID:
			selection = DatabaseUtilsCompat.concatenateWhere(selection, getIdSelection(uri));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(EVENTS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (matcher.match(uri)) {
		case PACKAGES:
			break;
		case PACKAGE_ID:
			selection = DatabaseUtilsCompat.concatenateWhere(selection, getIdSelection(uri));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		int ret = db.update(EVENTS_TABLE, values, selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		return ret;
	}

}
