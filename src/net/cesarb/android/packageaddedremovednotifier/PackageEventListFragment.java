package net.cesarb.android.packageaddedremovednotifier;

import java.text.DateFormat;
import java.util.Date;

import net.cesarb.android.packageaddedremovednotifier.PackageDatabase.PackageEvent;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class PackageEventListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	private static class PackageEventCursorAdapter extends
			ResourceCursorAdapter {

		public PackageEventCursorAdapter(Context context, Cursor c) {
			super(context, R.layout.item, c, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			boolean added = getBoolean(cursor, PackageEvent.EVENT_ADDED);
			boolean replacing = getBoolean(cursor, PackageEvent.EVENT_REPLACING);
			String applicationLabel = getStringOrNull(cursor, PackageEvent.APPLICATION_LABEL);
			String packageName = getString(cursor, PackageEvent.PACKAGE_NAME);
			String packageVersionName = getStringOrNull(cursor, PackageEvent.PACKAGE_VERSION_NAME);
			Long packageVersionCode = getLongOrNull(cursor, PackageEvent.PACKAGE_VERSION_CODE);
			long timestamp = getLong(cursor, PackageEvent.EVENT_TIMESTAMP);

			String event = added ? (replacing ? "Updated" : "Added") : "Removed";
			((TextView) view.findViewById(R.id.packageEventAndLabel)).setText(applicationLabel != null ? event + " " + applicationLabel : event);
			((TextView) view.findViewById(R.id.packageNameAndVersion)).setText(formatPackageNameAndVersion(packageName, packageVersionName, packageVersionCode));
			((TextView) view.findViewById(R.id.timestamp)).setText(DateFormat.getDateTimeInstance().format(new Date(timestamp)));
		}

		private static boolean getBoolean(Cursor cursor, String columnName) {
			return getLong(cursor, columnName) != 0;
		}

		private static long getLong(Cursor cursor, String columnName) {
			int columnIndex = cursor.getColumnIndexOrThrow(columnName);
			return cursor.getLong(columnIndex);
		}

		private static Long getLongOrNull(Cursor cursor, String columnName) {
			int columnIndex = cursor.getColumnIndexOrThrow(columnName);
			if (cursor.isNull(columnIndex))
				return null;
			return cursor.getLong(columnIndex);
		}

		private static String getString(Cursor cursor, String columnName) {
			int columnIndex = cursor.getColumnIndexOrThrow(columnName);
			return cursor.getString(columnIndex);
		}

		private static String getStringOrNull(Cursor cursor, String columnName) {
			int columnIndex = cursor.getColumnIndexOrThrow(columnName);
			if (cursor.isNull(columnIndex))
				return null;
			return cursor.getString(columnIndex);
		}

		private static String formatPackageNameAndVersion(String packageName, String packageVersionName, Long packageVersionCode) {
			StringBuilder builder = new StringBuilder(packageName);
			if (packageVersionName != null) {
				builder.append(' ');
				builder.append(packageVersionName);
			}
			if (packageVersionCode != null) {
				builder.append(" (");
				builder.append((long) packageVersionCode);
				builder.append(')');
			}
			return builder.toString();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new PackageEventCursorAdapter(getActivity(), null));
		getLoaderManager().initLoader(0, null, this);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), PackageEvent.CONTENT_URI, null, null, null, PackageEvent._ID + " DESC");
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		((CursorAdapter) getListAdapter()).swapCursor(data);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		((CursorAdapter) getListAdapter()).swapCursor(null);
	}

}
