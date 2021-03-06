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

package de.vanita5.twittnuker.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.ListAction;

public class ListActionAdapter extends ArrayAdapter<ListAction> {

	public ListActionAdapter(final Context context) {
		super(context, R.layout.list_action_item, android.R.id.text1);
	}

	public ListAction findItem(final long id) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (id == getItemId(i)) return getItem(i);
		}
		return null;
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).getOrder();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final TextView summary_view = (TextView) view.findViewById(android.R.id.text2);
		final String summary = getItem(position).getSummary();
		summary_view.setText(summary);
		summary_view.setVisibility(!TextUtils.isEmpty(summary) ? View.VISIBLE : View.GONE);
		return view;
	}
}
