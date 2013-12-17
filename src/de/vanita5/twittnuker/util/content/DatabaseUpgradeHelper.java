/*
 *			Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util.content;

import static org.mariotaku.querybuilder.SQLQueryBuilder.alterTable;
import static org.mariotaku.querybuilder.SQLQueryBuilder.createTable;
import static org.mariotaku.querybuilder.SQLQueryBuilder.drop;
import static org.mariotaku.querybuilder.SQLQueryBuilder.insertInto;
import static org.mariotaku.querybuilder.SQLQueryBuilder.select;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.NewColumn;
import org.mariotaku.querybuilder.Tables;
import org.mariotaku.querybuilder.query.SQLInsertIntoQuery;
import org.mariotaku.querybuilder.query.SQLInsertIntoQuery.OnConflict;
import org.mariotaku.querybuilder.query.SQLSelectQuery;
import de.vanita5.twittnuker.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DatabaseUpgradeHelper {

	public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newCols,
			final String[] newTypes, final boolean dropDirectly, final Map<String, String> colAliases) {

		if (newCols == null || newTypes == null || newCols.length != newTypes.length)
			throw new IllegalArgumentException("Invalid parameters for upgrading table " + table
					+ ", length of columns and types not match.");

		// First, create the table if not exists.
		db.execSQL(createTable(true, table).columns(NewColumn.createNewColumns(newCols, newTypes)).buildSQL());

		// We need to get all data from old table.
		final String[] oldCols = getColumnNames(db, table);
		if (oldCols == null || ArrayUtils.contentMatch(newCols, oldCols)) return;
		if (dropDirectly) {
			db.beginTransaction();
			db.execSQL(drop(table).getSQL());
			db.execSQL(createTable(false, table).columns(NewColumn.createNewColumns(newCols, newTypes)).buildSQL());
			db.setTransactionSuccessful();
			db.endTransaction();
			return;
		}
		final String tempTable = String.format("temp_%s_%d", table, System.currentTimeMillis());
		db.beginTransaction();
		db.execSQL(alterTable(table).renameTo(tempTable).buildSQL());
		db.execSQL(createTable(true, table).columns(NewColumn.createNewColumns(newCols, newTypes)).buildSQL());
		db.execSQL(createInsertDataQuery(table, tempTable, newCols, oldCols, colAliases));
		db.execSQL(drop(tempTable).getSQL());
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private static String createInsertDataQuery(final String table, final String tempTable, final String[] newCols,
			final String[] oldCols, final Map<String, String> colAliases) {
		final SQLInsertIntoQuery.Builder qb = insertInto(OnConflict.REPLACE, table);
		final List<String> newInsertColsList = new ArrayList<String>();
		for (final String newCol : newCols) {
			final String oldAliasedCol = colAliases.get(newCol);
			if (ArrayUtils.contains(oldCols, newCol) || oldAliasedCol != null
					&& ArrayUtils.contains(oldCols, oldAliasedCol)) {
				newInsertColsList.add(newCol);
			}
		}
		final String[] newInsertCols = newInsertColsList.toArray(new String[newInsertColsList.size()]);
		qb.columns(newInsertCols);
		final Columns.Column[] oldDataCols = new Columns.Column[newInsertCols.length];
		for (int i = 0, j = oldDataCols.length; i < j; i++) {
			final String newCol = newInsertCols[i];
			final String oldAliasedCol = colAliases.get(newCol);
			if (oldAliasedCol != null && ArrayUtils.contains(oldCols, oldAliasedCol)) {
				oldDataCols[i] = new Columns.Column(oldAliasedCol, newCol);
			} else {
				oldDataCols[i] = new Columns.Column(newCol);
			}
		}
		final SQLSelectQuery.Builder selectOldBuilder = select(new Columns(oldDataCols));
		selectOldBuilder.from(new Tables(tempTable));
		qb.select(selectOldBuilder.build());
		return qb.buildSQL();
	}

	private static String[] getColumnNames(final SQLiteDatabase db, final String table) {
		final Cursor cur = db.query(table, null, null, null, null, null, null, "1");
		if (cur == null) return null;
		try {
			return cur.getColumnNames();
		} finally {
			cur.close();
		}
	}

}
