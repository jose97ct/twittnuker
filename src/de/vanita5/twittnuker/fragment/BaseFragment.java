/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.support.BaseSupportActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MultiSelectManager;

public class BaseFragment extends Fragment implements Constants {

    public TwittnukerApplication getApplication() {
		final Activity activity = getActivity();
		if (activity != null) return (TwittnukerApplication) activity.getApplication();
		return null;
	}

	public ContentResolver getContentResolver() {
		final Activity activity = getActivity();
		if (activity != null) return activity.getContentResolver();
		return null;
	}

	public MultiSelectManager getMultiSelectManager() {
		final TwittnukerApplication app = getApplication();
		return app != null ? app.getMultiSelectManager() : null;
	}

	public SharedPreferences getSharedPreferences(final String name, final int mode) {
		final Activity activity = getActivity();
		if (activity != null) return activity.getSharedPreferences(name, mode);
		return null;
	}

	public Object getSystemService(final String name) {
		final Activity activity = getActivity();
		if (activity != null) return activity.getSystemService(name);
		return null;
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		final TwittnukerApplication app = getApplication();
		return app != null ? app.getTwitterWrapper() : null;
	}

	public void invalidateOptionsMenu() {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.invalidateOptionsMenu();
	}

	public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.registerReceiver(receiver, filter);
	}

	public void setProgressBarIndeterminateVisibility(final boolean visible) {
		final Activity activity = getActivity();
		if (activity instanceof BaseSupportActivity) {
			((BaseSupportActivity) activity).setProgressBarIndeterminateVisibility(visible);
		}
	}

	public void unregisterReceiver(final BroadcastReceiver receiver) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.unregisterReceiver(receiver);
	}
}
