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

package de.vanita5.twittnuker.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.view.iface.IExtendedView;

public class ProfileBannerImageView extends ForegroundImageView implements IExtendedView, Constants {

	private OnSizeChangedListener mOnSizeChangedListener;
	private TouchInterceptor mTouchInterceptor;

    private final Paint mClipPaint = new Paint();
    private int mBottomClip;

	public ProfileBannerImageView(final Context context) {
		this(context, null);
	}

	public ProfileBannerImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProfileBannerImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
        setScaleType(ScaleType.CENTER_CROP);
        mClipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        } else {
            setOutlineProvider(new CropOutlineProvider(this));
            setClipToOutline(true);
	    }
    }

	@Override
    public boolean hasOverlappingRendering() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    @Override
    public final boolean dispatchTouchEvent(@NonNull final MotionEvent event) {
		if (mTouchInterceptor != null) {
			final boolean ret = mTouchInterceptor.dispatchTouchEvent(this, event);
			if (ret) return true;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
    public final boolean onTouchEvent(@NonNull final MotionEvent event) {
		if (mTouchInterceptor != null) {
			final boolean ret = mTouchInterceptor.onTouchEvent(this, event);
			if (ret) return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public final void setOnSizeChangedListener(final OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	@Override
	public final void setTouchInterceptor(final TouchInterceptor listener) {
		mTouchInterceptor = listener;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2;
		setMeasuredDimension(width, height);
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}

	@Override
	protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mBottomClip != 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRect(getLeft(), getBottom() - mBottomClip - getTranslationY(), getRight(), getBottom(), mClipPaint);
        }
    }

    public void setBottomClip(int bottom) {
        mBottomClip = bottom;
        invalidate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invalidateOutline();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class CropOutlineProvider extends ViewOutlineProvider {

        private final ProfileBannerImageView mImageView;

        CropOutlineProvider(ProfileBannerImageView imageView) {
            mImageView = imageView;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            final int left = view.getLeft();
            final int top = view.getTop();
            final int right = view.getRight();
            final int bottom = Math.round(view.getBottom() - mImageView.mBottomClip - view.getTranslationY());
            outline.setRect(left, top, right, bottom);
        }
    }
}
