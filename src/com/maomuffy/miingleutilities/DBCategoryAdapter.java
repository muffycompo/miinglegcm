package com.maomuffy.miingleutilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.TAG;

public class DBCategoryAdapter {

	// DB Fields
	public static final String KEY_ROWID = "_id";
	public static final int COL_ROWID = 0;

	public static final String KEY_CATEGORY_NAME = "category_name";
	public static final String KEY_CATEGORY_ID = "category_id";

	// DB Fields (0 = KEY_ROWID, 1=...)
	public static final int COL_CATEGORY_NAME = 1;
	public static final int COL_CATEGORY_ID = 2;

	public static final String[] ALL_KEYS = new String[] { KEY_ROWID,
			KEY_CATEGORY_NAME, KEY_CATEGORY_ID };

	// DB info: it's name, and the table we are using (just one).
	public static final String DATABASE_NAME = "miingle_categories";
	public static final String DATABASE_TABLE = "categories";
	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_SQL = "create table "
			+ DATABASE_TABLE + " (" + KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_CATEGORY_ID + " integer, " 
			+ KEY_CATEGORY_NAME + " text"
			// Rest of creation:
			+ ");";

	// Context of application who uses us.
	private final Context ct;

	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	// ///////////////////////////////////////////////////////////////////
	// Public methods:
	// ///////////////////////////////////////////////////////////////////

	public DBCategoryAdapter(Context ctx) {
		this.ct = ctx;
		myDBHelper = new DatabaseHelper(ct);
	}

	// Open the database connection.
	public DBCategoryAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}

	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}

	// Add a new set of values to the database.
	public long insertRow(int categoryId, String categoryName) {

		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CATEGORY_ID, categoryId);
		initialValues.put(KEY_CATEGORY_NAME, categoryName);

		// Insert it into the database.
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	// Delete a row from the database, by rowId (primary key)
	public boolean deleteRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE, where, null) != 0;
	}

	public void deleteAll() {
		Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));
			} while (c.moveToNext());
		}
		c.close();
	}

	// Return all data in the database.
	public Cursor getAllRows() {
		String where = null;
		Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS, where, null, null,
				null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS, where, null, null,
				null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Change an existing row to be equal to new data.
	public boolean updateRow(long rowId, int categoryId, String categoryName) {
		String where = KEY_ROWID + "=" + rowId;

		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_CATEGORY_ID, categoryId);
		newValues.put(KEY_CATEGORY_NAME, categoryName);

		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}

	// ///////////////////////////////////////////////////////////////////
	// Private Helper Classes:
	// ///////////////////////////////////////////////////////////////////

	/**
	 * Private class which handles database creation and upgrading. Used to
	 * handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);			
			_db.execSQL("insert into " + DATABASE_TABLE + " ("+ KEY_CATEGORY_ID +", "+ KEY_CATEGORY_NAME +") values (1,'Science');");
			_db.execSQL("insert into " + DATABASE_TABLE + " ("+ KEY_CATEGORY_ID +", "+ KEY_CATEGORY_NAME +") values (2,'Fashion');");
			_db.execSQL("insert into " + DATABASE_TABLE + " ("+ KEY_CATEGORY_ID +", "+ KEY_CATEGORY_NAME +") values (3,'Politics');");
			_db.execSQL("insert into " + DATABASE_TABLE + " ("+ KEY_CATEGORY_ID +", "+ KEY_CATEGORY_NAME +") values (4,'Sports');");
			_db.execSQL("insert into " + DATABASE_TABLE + " ("+ KEY_CATEGORY_ID +", "+ KEY_CATEGORY_NAME +") values (5,'Fiction');");
			_db.execSQL("insert into " + DATABASE_TABLE + " ("+ KEY_CATEGORY_ID +", "+ KEY_CATEGORY_NAME +") values (6,'Technology');");

		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

			// Recreate new database:
			onCreate(_db);
		}
	}
}
