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

package de.vanita5.twittnuker.fragment.support;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.etsy.android.grid.StaggeredGridView;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.support.HomeActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment;
import de.vanita5.twittnuker.fragment.iface.RefreshScrollTopInterface;
import de.vanita5.twittnuker.fragment.iface.SupportFragmentCallback;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MultiSelectManager;
import de.vanita5.twittnuker.util.Utils;

public class BaseSupportStaggeredGridFragment extends StaggeredGridFragment implements IBaseFragment, Constants,
		OnScrollListener, RefreshScrollTopInterface {

	private boolean mActivityFirstCreated;
	private boolean mIsInstanceStateSaved;
	private boolean mReachedBottom, mNotReachedBottomBefore = true;

	@Override
	public void requestFitSystemWindows() {
		final Activity activity = getActivity();
        final Fragment parentFragment = getParentFragment();
        final SystemWindowsInsetsCallback callback;
        if (parentFragment instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) parentFragment;
        } else if (activity instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) activity;
        } else {
            return;
        }
		final Rect insets = new Rect();
        if (callback.getSystemWindowsInsets(insets)) {
			fitSystemWindows(insets);
		}
	}

	protected void fitSystemWindows(Rect insets) {

	}

	public final TwittnukerApplication getApplication() {
		return TwittnukerApplication.getInstance(getActivity());
	}

	public final ContentResolver getContentResolver() {
		final Activity activity = getActivity();
		if (activity != null) return activity.getContentResolver();
		return null;
	}

	@Override
	public Bundle getExtraConfiguration() {
		final Bundle args = getArguments();
		final Bundle extras = new Bundle();
		if (args != null && args.containsKey(EXTRA_EXTRAS)) {
			extras.putAll(args.getBundle(EXTRA_EXTRAS));
		}
		return extras;
	}

	public final MultiSelectManager getMultiSelectManager() {
		return getApplication() != null ? getApplication().getMultiSelectManager() : null;
	}

	public final SharedPreferences getSharedPreferences(final String name, final int mode) {
		final Activity activity = getActivity();
		if (activity != null) return activity.getSharedPreferences(name, mode);
		return null;
	}

	public final Object getSystemService(final String name) {
		final Activity activity = getActivity();
		if (activity != null) return activity.getSystemService(name);
		return null;
	}

	@Override
	public final int getTabPosition() {
		final Bundle args = getArguments();
		return args != null ? args.getInt(EXTRA_TAB_POSITION, -1) : -1;
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return getApplication() != null ? getApplication().getTwitterWrapper() : null;
	}

	public void invalidateOptionsMenu() {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.invalidateOptionsMenu();
	}

	public boolean isActivityFirstCreated() {
		return mActivityFirstCreated;
	}

	public boolean isInstanceStateSaved() {
		return mIsInstanceStateSaved;
	}

	public boolean isReachedBottom() {
		return mReachedBottom;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mIsInstanceStateSaved = savedInstanceState != null;
		final StaggeredGridView lv = getListView();
		lv.setOnScrollListener(this);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivityFirstCreated = true;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mActivityFirstCreated = true;
	}

	@Override
	public void onDetach() {
		super.onDetach();
        final Fragment fragment = getParentFragment();
        if (fragment instanceof SupportFragmentCallback) {
            ((SupportFragmentCallback) fragment).onDetachFragment(this);
        }
		final Activity activity = getActivity();
		if (activity instanceof SupportFragmentCallback) {
			((SupportFragmentCallback) activity).onDetachFragment(this);
		}
	}

	public void onPostStart() {
	}

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
			final int totalItemCount) {
		final boolean reached = firstVisibleItem + visibleItemCount >= totalItemCount
				&& totalItemCount >= visibleItemCount;

		if (mReachedBottom != reached) {
			mReachedBottom = reached;
			if (mReachedBottom && mNotReachedBottomBefore) {
				mNotReachedBottomBefore = false;
				return;
			}
			if (mReachedBottom && getListAdapter().getCount() > visibleItemCount) {
				onReachedBottom();
			}
		}

	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {

	}

	@Override
	public void onStart() {
		super.onStart();
		onPostStart();
	}

	@Override
	public void onStop() {
		mActivityFirstCreated = false;
		super.onStop();
	}

	public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.registerReceiver(receiver, filter);
	}

	@Override
	public boolean scrollToStart() {
		if (!isAdded() || getActivity() == null) return false;
		Utils.scrollListToTop(getListView());
		return true;
	}

	public void setProgressBarIndeterminateVisibility(final boolean visible) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.setProgressBarIndeterminateVisibility(visible);
		if (activity instanceof HomeActivity) {
			((HomeActivity) activity).setHomeProgressBarIndeterminateVisibility(visible);
		}
	}

	@Override
	public void setSelection(final int position) {
		Utils.scrollListToPosition(getListView(), position);
	}

	@Override
	public void setUserVisibleHint(final boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		final Activity activity = getActivity();
		final Fragment fragment = getParentFragment();
		if (fragment instanceof SupportFragmentCallback) {
			((SupportFragmentCallback) fragment).onSetUserVisibleHint(this, isVisibleToUser);
        }
        if (activity instanceof SupportFragmentCallback) {
            ((SupportFragmentCallback) activity).onSetUserVisibleHint(this, isVisibleToUser);
		}
	}

	@Override
	public boolean triggerRefresh() {
		return false;
	}

	public void unregisterReceiver(final BroadcastReceiver receiver) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.unregisterReceiver(receiver);
	}

	protected void onReachedBottom() {

	}
}
