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

package de.vanita5.twittnuker.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.iface.IThemedActivity;
import de.vanita5.twittnuker.util.ThemeUtils;

import static de.vanita5.twittnuker.util.Utils.restartActivity;

public abstract class BasePreferenceActivity extends PreferenceActivity implements Constants,
        IThemedActivity {

    private int mCurrentThemeResource;

	@Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
	}

	@Override
    public int getCurrentThemeBackgroundAlpha() {
        return 0;
    }

    @Override
    public int getCurrentThemeColor() {
        return 0;
    }

    @Override
    public int getCurrentThemeResourceId() {
        return mCurrentThemeResource;
    }

    @Override
    public Resources getDefaultResources() {
        return super.getResources();
    }

    @Override
    public int getThemeBackgroundAlpha() {
        return 0;
    }

    @Override
    public int getThemeColor() {
        return 0;
    }

	@Override
	public int getActionBarColor() {
		return ThemeUtils.getActionBarColor(this);
	}

	@Override
    public String getThemeFontFamily() {
        return VALUE_THEME_FONT_FAMILY_REGULAR;
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getSettingsThemeResource(this);
	}

    @Override
	public void navigateUpFromSameTask() {
		NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	public final void restart() {
		restartActivity(this);
	}

	protected final boolean isThemeChanged() {
		return getThemeResourceId() != mCurrentThemeResource;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		ThemeUtils.notifyStatusBarColorChanged(this, mCurrentThemeResource, 0, 0xFF);
        setTheme(mCurrentThemeResource = getThemeResourceId());
		super.onCreate(savedInstanceState);
		setActionBarBackground();
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (isThemeChanged()) {
            restart();
		} else {
			ThemeUtils.notifyStatusBarColorChanged(this, mCurrentThemeResource, 0, 0xFF);
        }
	}

	private final void setActionBarBackground() {
		// ThemeUtils.applyActionBarBackground(getActionBar(), this,
		// mCurrentThemeResource);
	}

}