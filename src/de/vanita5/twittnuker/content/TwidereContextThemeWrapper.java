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

package de.vanita5.twittnuker.content;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;

import de.vanita5.twittnuker.util.theme.TwidereAccentHelper;

public class TwidereContextThemeWrapper extends ContextThemeWrapper {

	private final TwidereAccentHelper mAccentHelper;

	private final int mThemeResourceId;
	private final int mAccentColor;

	public TwidereContextThemeWrapper(final Context base, final int themeResource, final int accentColor) {
		super(base, themeResource);
		mThemeResourceId = themeResource;
		mAccentColor = accentColor;
		mAccentHelper = new TwidereAccentHelper(accentColor, themeResource);
	}

	public int getAccentColor() {
		return mAccentColor;
	}

	@Override
	public Resources getResources() {
		return mAccentHelper.getResources(this, super.getResources());
	}

	public int getThemeResourceId() {
		return mThemeResourceId;
	}

}