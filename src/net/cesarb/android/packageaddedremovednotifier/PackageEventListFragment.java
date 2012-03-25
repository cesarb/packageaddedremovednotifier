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
			boolean added = cursor.getLong(cursor.getColumnIndexOrThrow(PackageEvent.EVENT_ADDED)) != 0;
			boolean replacing = cursor.getLong(cursor.getColumnIndexOrThrow(PackageEvent.EVENT_REPLACING)) != 0;
			String applicationLabel = cursor.getString(cursor.getColumnIndexOrThrow(PackageEvent.APPLICATION_LABEL));
			String packageName = cursor.getString(cursor.getColumnIndexOrThrow(PackageEvent.PACKAGE_NAME));
			String packageVersion = cursor.getString(cursor.getColumnIndexOrThrow(PackageEvent.PACKAGE_VERSION_NAME));
			long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(PackageEvent.EVENT_TIMESTAMP));

			String event = added ? (replacing ? "Updated" : "Added") : "Removed";
			((TextView) view.findViewById(R.id.packageEventAndLabel)).setText(applicationLabel != null ? event + " " + applicationLabel : event);
			((TextView) view.findViewById(R.id.packageNameAndVersion)).setText(packageVersion != null ? packageName + " " + packageVersion : packageName);
			((TextView) view.findViewById(R.id.timestamp)).setText(DateFormat.getDateTimeInstance().format(new Date(timestamp)));
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
