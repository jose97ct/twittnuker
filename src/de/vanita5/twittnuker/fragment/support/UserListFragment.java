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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.UserListSelectorActivity;
import de.vanita5.twittnuker.adapter.support.SupportTabsAdapter;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.OnLinkClickHandler;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.ColorLabelRelativeLayout;
import de.vanita5.twittnuker.view.HeaderDrawerLayout;
import de.vanita5.twittnuker.view.HeaderDrawerLayout.DrawerCallback;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import static android.text.TextUtils.isEmpty;
import static de.vanita5.twittnuker.util.Utils.getAccountColor;
import static de.vanita5.twittnuker.util.Utils.getDisplayName;
import static de.vanita5.twittnuker.util.Utils.getLocalizedNumber;
import static de.vanita5.twittnuker.util.Utils.getTwitterInstance;
import static de.vanita5.twittnuker.util.Utils.openUserListMembers;
import static de.vanita5.twittnuker.util.Utils.openUserListSubscribers;
import static de.vanita5.twittnuker.util.Utils.openUserListTimeline;
import static de.vanita5.twittnuker.util.Utils.openUserProfile;
import static de.vanita5.twittnuker.util.Utils.setMenuItemAvailability;

public class UserListFragment extends BaseSupportFragment implements OnClickListener,
        LoaderCallbacks<SingleResponse<ParcelableUserList>>, DrawerCallback,
        SystemWindowsInsetsCallback {

	private ImageLoaderWrapper mProfileImageLoader;
	private AsyncTwitterWrapper mTwitterWrapper;

	private ImageView mProfileImageView;
	private TextView mListNameView, mCreatedByView, mDescriptionView, mErrorMessageView;
    private View mErrorRetryContainer, mProgressContainer;
	private ColorLabelRelativeLayout mProfileContainer;
	private View mDescriptionContainer;
	private Button mRetryButton;
    private HeaderDrawerLayout mHeaderDrawerLayout;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerIndicator;

    private SupportTabsAdapter mPagerAdapter;

	private ParcelableUserList mUserList;
	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
            final ParcelableUserList userList = intent.getParcelableExtra(EXTRA_USER_LIST);
            if (userList == null || mUserList == null)
                return;
			if (BROADCAST_USER_LIST_DETAILS_UPDATED.equals(action)) {
                if (userList.id == mUserList.id) {
					getUserListInfo(true);
				}
			} else if (BROADCAST_USER_LIST_SUBSCRIBED.equals(action) || BROADCAST_USER_LIST_UNSUBSCRIBED.equals(action)) {
                if (userList.id == mUserList.id) {
					getUserListInfo(true);
				}
			}
		}
	};
    private boolean mUserListLoaderInitialized;

	public void displayUserList(final ParcelableUserList userList) {
		if (userList == null || getActivity() == null) return;
		getLoaderManager().destroyLoader(0);
		final boolean is_myself = userList.account_id == userList.user_id;
		mErrorRetryContainer.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.GONE);
		mUserList = userList;
		mProfileContainer.drawEnd(getAccountColor(getActivity(), userList.account_id));
		mListNameView.setText(userList.name);
		final String display_name = getDisplayName(userList.user_name, userList.user_screen_name, false);
		mCreatedByView.setText(getString(R.string.created_by, display_name));
		final String description = userList.description;
		mDescriptionContainer.setVisibility(is_myself || !isEmpty(description) ? View.VISIBLE : View.GONE);
		mDescriptionView.setText(description);
        final TwidereLinkify linkify = new TwidereLinkify(new OnLinkClickHandler(getActivity(), getMultiSelectManager()));
		linkify.applyAllLinks(mDescriptionView, userList.account_id, false);
		mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
		mProfileImageLoader.displayProfileImage(mProfileImageView, userList.user_profile_image_url);
		invalidateOptionsMenu();
	}

	public void getUserListInfo(final boolean omit_intent_extra) {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(0);
		final Bundle args = new Bundle(getArguments());
		args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omit_intent_extra);
		if (!mUserListLoaderInitialized) {
			lm.initLoader(0, args, this);
			mUserListLoaderInitialized = true;
		} else {
			lm.restartLoader(0, args, this);
		}
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SELECT_USER: {
				if (resultCode != Activity.RESULT_OK || !data.hasExtra(EXTRA_USER) || mTwitterWrapper == null
						|| mUserList == null) return;
				final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
				mTwitterWrapper.addUserListMembersAsync(mUserList.account_id, mUserList.id, user);
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
	}

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        final FragmentActivity activity = getActivity();

        mHeaderDrawerLayout.setDrawerCallback(this);

        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mPagerIndicator.setViewPager(mViewPager);

        mTwitterWrapper = getApplication().getTwitterWrapper();
        mProfileImageLoader = getApplication().getImageLoaderWrapper();
        mProfileImageView.setOnClickListener(this);
        mProfileContainer.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
        getUserListInfo(false);

        setupUserPages();
	}

	@Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_DETAILS_UPDATED);
        filter.addAction(BROADCAST_USER_LIST_SUBSCRIBED);
        filter.addAction(BROADCAST_USER_LIST_UNSUBSCRIBED);
        registerReceiver(mStatusReceiver, filter);
	}

	@Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
	}

	@Override
	public void onDestroyView() {
		mUserList = null;
		getLoaderManager().destroyLoader(0);
		super.onDestroyView();
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_list, menu);
	}

	@Override
    public void onPrepareOptionsMenu(Menu menu) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final ParcelableUserList userList = mUserList;
        final MenuItem followItem = menu.findItem(MENU_FOLLOW);
        if (followItem != null) {
            followItem.setEnabled(userList != null);
            if (userList == null) {
                followItem.setIcon(android.R.color.transparent);
            }
        }
        if (twitter == null || userList == null) return;
        final boolean isMyList = userList.user_id == userList.account_id;
        setMenuItemAvailability(menu, MENU_EDIT, isMyList);
        setMenuItemAvailability(menu, MENU_ADD, isMyList);
        setMenuItemAvailability(menu, MENU_DELETE, isMyList);
        final boolean isFollowing = userList.is_following;
        if (followItem != null) {
            followItem.setVisible(!isMyList);
            if (isFollowing) {
                followItem.setIcon(R.drawable.ic_action_cancel);
                followItem.setTitle(R.string.unsubscribe);
		} else {
                followItem.setIcon(R.drawable.ic_action_add);
                followItem.setTitle(R.string.subscribe);
			}
		}
	}

	@Override
    public boolean onOptionsItemSelected(final MenuItem item) {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		final ParcelableUserList userList = mUserList;
		if (twitter == null || userList == null) return false;
		switch (item.getItemId()) {
			case MENU_ADD: {
				if (userList.user_id != userList.account_id) return false;
				final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
				intent.setClass(getActivity(), UserListSelectorActivity.class);
				intent.putExtra(EXTRA_ACCOUNT_ID, userList.account_id);
				startActivityForResult(intent, REQUEST_SELECT_USER);
				break;
			}
			case MENU_DELETE: {
				if (userList.user_id != userList.account_id) return false;
				DestroyUserListDialogFragment.show(getFragmentManager(), userList);
				break;
			}
			case MENU_EDIT: {
				final Bundle args = new Bundle();
				args.putLong(EXTRA_ACCOUNT_ID, userList.account_id);
				args.putString(EXTRA_LIST_NAME, userList.name);
				args.putString(EXTRA_DESCRIPTION, userList.description);
				args.putBoolean(EXTRA_IS_PUBLIC, userList.is_public);
				args.putLong(EXTRA_LIST_ID, userList.id);
				final DialogFragment f = new EditUserListDialogFragment();
				f.setArguments(args);
				f.show(getFragmentManager(), "edit_user_list_details");
				return true;
			}
			case MENU_FOLLOW: {
				if (userList.is_following) {
					DestroyUserListSubscriptionDialogFragment.show(getFragmentManager(), userList);
				} else {
					twitter.createUserListSubscriptionAsync(userList.account_id, userList.id);
				}
				return true;
			}
			default: {
				if (item.getIntent() != null) {
					try {
						startActivity(item.getIntent());
					} catch (final ActivityNotFoundException e) {
						if (Utils.isDebugBuild()) Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

    private void setupUserPages() {
        final Context context = getActivity();
        final Bundle args = getArguments(), tabArgs = new Bundle();
        if (args.containsKey(EXTRA_USER)) {
            final ParcelableUserList userList = args.getParcelable(EXTRA_USER_LIST);
            tabArgs.putLong(EXTRA_ACCOUNT_ID, userList.account_id);
            tabArgs.putLong(EXTRA_USER_ID, userList.user_id);
            tabArgs.putString(EXTRA_SCREEN_NAME, userList.user_screen_name);
            tabArgs.putInt(EXTRA_LIST_ID, (int) userList.id);
            tabArgs.putString(EXTRA_LIST_NAME, userList.name);
        } else {
            tabArgs.putLong(EXTRA_ACCOUNT_ID, args.getLong(EXTRA_ACCOUNT_ID, -1));
            tabArgs.putLong(EXTRA_USER_ID, args.getLong(EXTRA_USER_ID, -1));
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME));
            tabArgs.putInt(EXTRA_LIST_ID, args.getInt(EXTRA_LIST_ID, -1));
            tabArgs.putString(EXTRA_LIST_NAME, args.getString(EXTRA_LIST_NAME));
        }
        mPagerAdapter.addTab(UserListTimelineFragment.class, tabArgs, getString(R.string.statuses), null, 0);
        mPagerAdapter.addTab(UserListMembersFragment.class, tabArgs, getString(R.string.list_members), null, 1);
        mPagerAdapter.addTab(UserListSubscribersFragment.class, tabArgs, getString(R.string.list_subscribers), null, 2);
        mPagerIndicator.notifyDataSetChanged();
    }

	@Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.retry: {
                getUserListInfo(true);
                break;
	        }
            case R.id.profile_image: {
                if (mUserList == null) return;
                openUserProfile(getActivity(), mUserList.account_id,
                        mUserList.user_id, mUserList.user_screen_name, null);
                break;
            }
        }

    }

	@Override
    public Loader<SingleResponse<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
        mErrorMessageView.setText(null);
        mErrorMessageView.setVisibility(View.GONE);
        mErrorRetryContainer.setVisibility(View.GONE);
        mHeaderDrawerLayout.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.VISIBLE);
        setProgressBarIndeterminateVisibility(true);
        final long accountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
        final long userId = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
        final int listId = args != null ? args.getInt(EXTRA_LIST_ID, -1) : -1;
        final String listName = args != null ? args.getString(EXTRA_LIST_NAME) : null;
        final String screenName = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
        final boolean omitIntentExtra = args == null || args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
        return new ParcelableUserListLoader(getActivity(), omitIntentExtra, getArguments(), accountId, listId,
                listName, userId, screenName);
	}

	@Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableUserList>> loader,
                               final SingleResponse<ParcelableUserList> data) {
        if (data == null) return;
        if (getActivity() == null) return;
        if (data.getData() != null) {
            final ParcelableUserList list = data.getData();
            displayUserList(list);
            mHeaderDrawerLayout.setVisibility(View.VISIBLE);
            mErrorRetryContainer.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.GONE);
        } else {
            if (data.hasException()) {
                mErrorMessageView.setText(data.getException().getMessage());
                mErrorMessageView.setVisibility(View.VISIBLE);
            }
            mHeaderDrawerLayout.setVisibility(View.GONE);
            mErrorRetryContainer.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
        }
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableUserList>> loader) {

    }

    @Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        mHeaderDrawerLayout = (HeaderDrawerLayout) view.findViewById(R.id.details_container);
        mErrorRetryContainer = view.findViewById(R.id.error_retry_container);
        mProgressContainer = view.findViewById(R.id.progress_container);

        final View headerView = mHeaderDrawerLayout.getHeader();
        final View contentView = mHeaderDrawerLayout.getContent();
        mProfileContainer = (ColorLabelRelativeLayout) headerView.findViewById(R.id.profile);
        mListNameView = (TextView) headerView.findViewById(R.id.list_name);
        mCreatedByView = (TextView) headerView.findViewById(R.id.created_by);
        mDescriptionView = (TextView) headerView.findViewById(R.id.description);
        mProfileImageView = (ImageView) headerView.findViewById(R.id.profile_image);
        mDescriptionContainer = headerView.findViewById(R.id.description_container);
        mRetryButton = (Button) mErrorRetryContainer.findViewById(R.id.retry);
        mErrorMessageView = (TextView) mErrorRetryContainer.findViewById(R.id.error_message);
        mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        mPagerIndicator = (PagerSlidingTabStrip) contentView.findViewById(R.id.view_pager_tabs);
	}

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View progress = mProgressContainer, error = mErrorRetryContainer;
        final HeaderDrawerLayout content = mHeaderDrawerLayout;
        if (progress == null || error == null || content == null) {
            return;
		}
        progress.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        error.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        content.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        content.setClipToPadding(false);
	}

    @Override
    public void fling(float velocity) {

    }

    @Override
    public void scrollBy(float dy) {

    }

    @Override
    public boolean canScroll(float dy) {
        return false;
	}

    @Override
    public boolean isScrollContent(float x, float y) {
        return false;
    }

    @Override
    public void cancelTouch() {

    }

    @Override
    public void topChanged(int offset) {

    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        return false;
    }

	public static class EditUserListDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {

		private EditText mEditName, mEditDescription;
		private CheckBox mPublicCheckBox;
		private String mName, mDescription;
		private long mAccountId;
		private long mListId;
		private boolean mIsPublic;
		private AsyncTwitterWrapper mTwitterWrapper;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (mAccountId <= 0) return;
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mName = ParseUtils.parseString(mEditName.getText());
					mDescription = ParseUtils.parseString(mEditDescription.getText());
					mIsPublic = mPublicCheckBox.isChecked();
					if (mName == null || mName.length() <= 0) return;
					mTwitterWrapper.updateUserListDetails(mAccountId, mListId, mIsPublic, mName, mDescription);
					break;
				}
			}

		}

        @NonNull
		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			mTwitterWrapper = getApplication().getTwitterWrapper();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			mListId = bundle != null ? bundle.getLong(EXTRA_LIST_ID, -1) : -1;
			mName = bundle != null ? bundle.getString(EXTRA_LIST_NAME) : null;
			mDescription = bundle != null ? bundle.getString(EXTRA_DESCRIPTION) : null;
            mIsPublic = bundle == null || bundle.getBoolean(EXTRA_IS_PUBLIC, true);
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            final View view = LayoutInflater.from(wrapped).inflate(R.layout.edit_user_list_detail, null);
			builder.setView(view);
			mEditName = (EditText) view.findViewById(R.id.name);
			mEditDescription = (EditText) view.findViewById(R.id.description);
			mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
			if (mName != null) {
				mEditName.setText(mName);
			}
			if (mDescription != null) {
				mEditDescription.setText(mDescription);
			}
			mPublicCheckBox.setChecked(mIsPublic);
			builder.setTitle(R.string.user_list);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
			outState.putLong(EXTRA_LIST_ID, mListId);
			outState.putString(EXTRA_LIST_NAME, mName);
			outState.putString(EXTRA_DESCRIPTION, mDescription);
			outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
			super.onSaveInstanceState(outState);
		}

	}

	static class ParcelableUserListLoader extends AsyncTaskLoader<SingleResponse<ParcelableUserList>> {

		private final boolean mOmitIntentExtra;
		private final Bundle mExtras;
		private final long mAccountId, mUserId;
		private final int mListId;
		private final String mScreenName, mListName;

		private ParcelableUserListLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
				final long accountId, final int listId, final String listName, final long userId,
				final String screenName) {
			super(context);
			mOmitIntentExtra = omitIntentExtra;
			mExtras = extras;
			mAccountId = accountId;
			mUserId = userId;
			mListId = listId;
			mScreenName = screenName;
			mListName = listName;
		}

		@Override
		public SingleResponse<ParcelableUserList> loadInBackground() {
			if (!mOmitIntentExtra && mExtras != null) {
				final ParcelableUserList cache = mExtras.getParcelable(EXTRA_USER_LIST);
				if (cache != null) return SingleResponse.getInstance(cache);
			}
			final Twitter twitter = getTwitterInstance(getContext(), mAccountId, true);
			if (twitter == null) return SingleResponse.getInstance();
			try {
				final UserList list;
				if (mListId > 0) {
					list = twitter.showUserList(mListId);
				} else if (mUserId > 0) {
					list = twitter.showUserList(mListName, mUserId);
				} else if (mScreenName != null) {
					list = twitter.showUserList(mListName, mScreenName);
				} else
					return SingleResponse.getInstance();
                return SingleResponse.getInstance(new ParcelableUserList(list, mAccountId));
			} catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
			}
		}

		@Override
		public void onStartLoading() {
			forceLoad();
		}

	}

}
