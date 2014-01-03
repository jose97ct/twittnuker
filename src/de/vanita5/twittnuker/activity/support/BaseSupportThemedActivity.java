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

package de.vanita5.twittnuker.activity.support;

import static de.vanita5.twittnuker.util.Utils.restartActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import com.negusoft.holoaccent.AccentHelper;

import de.vanita5.twittnuker.activity.iface.IThemedActivity;
import de.vanita5.twittnuker.theme.TwidereAccentHelper;
import de.vanita5.twittnuker.util.CompareUtils;
import de.vanita5.twittnuker.util.StrictModeUtils;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;


public abstract class BaseSupportThemedActivity extends FragmentActivity implements IThemedActivity {

	private int mCurrentThemeResource, mCurrentThemeColor;

	private String mCurrentThemeFontFamily;

    private AccentHelper mAccentHelper;

	@Override
	public void finish() {
		super.finish();
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public final int getCurrentThemeResourceId() {
		return mCurrentThemeResource;
	}
	
	@Override
	public final Resources getDefaultResources() {
		return super.getResources();
	}

    @Override
    public Resources getResources() {
        return getThemedResources();
    }

    @Override
    public abstract int getThemeColor();

    @Override
    public final Resources getThemedResources() {
        if (mAccentHelper == null) {
            mAccentHelper = new TwidereAccentHelper(ThemeUtils.getUserThemeColor(this));
        }
        return mAccentHelper.getResources(this, super.getResources());
    }

	@Override
	public String getThemeFontFamily() {
		return ThemeUtils.getThemeFontFamily(this);
	}

    @Override
    public abstract int getThemeResourceId();

	@Override
	public void navigateUpFromSameTask() {
		NavUtils.navigateUpFromSameTask(this);
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public void overrideCloseAnimationIfNeeded() {
		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityCloseAnimation(this);
		} else {
			ThemeUtils.overrideNormalActivityCloseAnimation(this);
		}
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return true;
	}

	protected boolean isThemeChanged() {
		return getThemeResourceId() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor
				|| !CompareUtils.objectEquals(getThemeFontFamily(), mCurrentThemeFontFamily);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (Utils.isDebugBuild()) {
			StrictModeUtils.detectAllVmPolicy();
			StrictModeUtils.detectAllThreadPolicy();
		}

		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityOpenAnimation(this);
		}
		setTheme();
		super.onCreate(savedInstanceState);
		setActionBarBackground();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isThemeChanged()) {
			restart();
		}
	}

	protected final void restart() {
		restartActivity(this);
	}

	private final void setActionBarBackground() {
		ThemeUtils.applyActionBarBackground(getActionBar(), this);
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResourceId();
		mCurrentThemeColor = getThemeColor();
		mCurrentThemeFontFamily = getThemeFontFamily();
		setTheme(mCurrentThemeResource);
	}
}