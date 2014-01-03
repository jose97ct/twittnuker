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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.dialog.ColorPickerDialog;
import de.vanita5.twittnuker.view.ColorPickerView;

public class ColorPickerPreference extends Preference implements DialogInterface.OnClickListener, Constants {

	private View mView;
	protected int mDefaultValue = Color.WHITE;
	private final float mDensity;
	private boolean mAlphaSliderEnabled = false;

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULTVALUE = "defaultValue";
	private static final String ATTR_ALPHASLIDER = "alphaSlider";

    private final Resources mResources;

    private ColorPickerDialog mDialog;

	public ColorPickerPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
        mResources = context.getResources();
        mDensity = mResources.getDisplayMetrics().density;
		init(context, attrs);
	}

    public void onActivityDestroy() {
        if (mDialog == null || !mDialog.isShowing()) return;
        mDialog.dismiss();
    }

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
                if (mDialog == null) return;
                final int color = mDialog.getColor();
				if (isPersistent()) {
					persistInt(color);
				}
				setPreviewColor();
				final OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
				if (listener != null) {
					listener.onPreferenceChange(this, color);
				}
				break;
		}
	}

	@Override
	public void setDefaultValue(final Object value) {
		if (!(value instanceof Integer)) return;
		mDefaultValue = (Integer) value;
	}

	protected void init(final Context context, final AttributeSet attrs) {
		if (attrs != null) {
			final String defaultValue = attrs.getAttributeValue(ANDROID_NS, ATTR_DEFAULTVALUE);
			if (defaultValue != null && defaultValue.startsWith("#")) {
				try {
					setDefaultValue(Color.parseColor(defaultValue));
				} catch (final IllegalArgumentException e) {
					Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
					setDefaultValue(Color.WHITE);
				}
			} else {
				final int colorResourceId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
				if (colorResourceId != 0) {
					setDefaultValue(context.getResources().getColor(colorResourceId));
				}
			}
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, ATTR_ALPHASLIDER, false);
		}
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		mView = view;
		setPreviewColor();
	}

    protected void onClick() {
        if (mDialog != null && mDialog.isShowing()) return;
        mDialog = new ColorPickerDialog(getContext(), getValue(), mAlphaSliderEnabled);
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, mResources.getString(android.R.string.ok), this);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel), this);
        mDialog.show();
        return;
    }

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		if (isPersistent() && defaultValue instanceof Integer) {
			persistInt(restoreValue ? getValue() : (Integer) defaultValue);
		}
	}

	private int getValue() {
		try {
			if (isPersistent()) return getPersistedInt(mDefaultValue);
		} catch (final ClassCastException e) {
			e.printStackTrace();
		}
		return mDefaultValue;
	}

	private void setPreviewColor() {
		if (mView == null) return;
		final View widgetFrameView = mView.findViewById(android.R.id.widget_frame);
		if (!(widgetFrameView instanceof ViewGroup)) return;
		final ViewGroup widgetFrame = (ViewGroup) widgetFrameView;
        widgetFrame.setVisibility(View.VISIBLE);
        widgetFrame.setPadding(widgetFrame.getPaddingLeft(), widgetFrame.getPaddingTop(), (int) (mDensity * 8),
                widgetFrame.getPaddingBottom());
		// remove preview image that is already created
        widgetFrame.setAlpha(isEnabled() ? 1 : 0.25f);
		final View foundView = widgetFrame.findViewById(R.id.color);
		final ImageView imageView;
		if (foundView instanceof ImageView) {
			imageView = (ImageView) foundView;
		} else {
			imageView = new ImageView(getContext());
			widgetFrame.removeAllViews();
			imageView.setId(R.id.color);
			widgetFrame.addView(imageView);
		}
		imageView.setImageBitmap(ColorPickerView.getColorPreviewBitmap(getContext(), getValue()));
	}

}
