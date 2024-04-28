package android.example.smd_assignment3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PasswordManagerDB {

    private final String DATABASE_NAME = "PasswordManagerDB";
    private final String DATABASE_TABLE = "UserTable";
    private final String USER_ENTRIES_TABLE = "userEntries";
    private final String RECYCLE_BIN_TABLE = "RecycleBin";
    private final int DATABASE_VERSION = 4;

    public static final String ROW_ID = "_id";
    public static final String ROW_USERNAME = "_username";
    public static final String ROW_PASSWORD = "_password";

    public static final String ROW_ENTRY_ID = "entry_id";
    public static final String ROW_WEBSITE_URL = "website_url";
    public static final String ROW_ID_NAME = "id_name";
    public static final String ROW_PASSCODE = "passcode";
    public static final String ROW_USER = "user";

    private DBHelper ourHelper;
    private SQLiteDatabase ourDB;
    public Context ourContext;

    public PasswordManagerDB(Context context) {
        ourContext = context;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String query = "CREATE TABLE " + DATABASE_TABLE + "(" +
                    ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ROW_USERNAME + " TEXT NOT NULL," +
                    ROW_PASSWORD + " TEXT NOT NULL);";
            sqLiteDatabase.execSQL(query);

            String userEntriesQuery = "CREATE TABLE " + USER_ENTRIES_TABLE + "(" +
                    ROW_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ROW_WEBSITE_URL + " TEXT NOT NULL," +
                    ROW_ID_NAME + " TEXT NOT NULL," +
                    ROW_PASSCODE + " TEXT NOT NULL," +
                    ROW_USER + " TEXT NOT NULL);";
            sqLiteDatabase.execSQL(userEntriesQuery);

            String recycleBinQuery = "CREATE TABLE " + RECYCLE_BIN_TABLE + "(" +
                    ROW_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ROW_WEBSITE_URL + " TEXT NOT NULL," +
                    ROW_ID_NAME + " TEXT NOT NULL," +
                    ROW_PASSCODE + " TEXT NOT NULL," +
                    ROW_USER + " TEXT NOT NULL);";
            sqLiteDatabase.execSQL(recycleBinQuery);
        }


        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + USER_ENTRIES_TABLE);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RECYCLE_BIN_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    public void open(){
        ourHelper = new DBHelper(ourContext);
        ourDB = ourHelper.getWritableDatabase();
    }

    public void close(){
        ourHelper.close();
    }

    public long addNewUser(String userName, String password){
        ContentValues cv = new ContentValues();

        cv.put(ROW_USERNAME, userName);
        cv.put(ROW_PASSWORD, password);

        return ourDB.insert(DATABASE_TABLE, null, cv);
    }

    public boolean login(String userName, String password) {
        Cursor cursor = ourDB.rawQuery("SELECT * FROM " + DATABASE_TABLE +
                        " WHERE " + ROW_USERNAME + " = ? AND " + ROW_PASSWORD + " = ?",
                new String[]{userName, password});

        boolean result = cursor.getCount() > 0;

        cursor.close();
        return result;
    }

    public boolean userCheck(String userName) {
        Cursor cursor = ourDB.rawQuery("SELECT * FROM " + DATABASE_TABLE +
                " WHERE " + ROW_USERNAME + " = ?", new String[]{userName});

        boolean result = cursor.getCount() > 0;

        cursor.close();
        return result;
    }

    public long addEntry(String websiteUrl, String idName, String passcode, String user) {
        ContentValues cv = new ContentValues();
        cv.put(ROW_WEBSITE_URL, websiteUrl);
        cv.put(ROW_ID_NAME, idName);
        cv.put(ROW_PASSCODE, passcode);
        cv.put(ROW_USER, user);
        return ourDB.insert(USER_ENTRIES_TABLE, null, cv);
    }

    public boolean removeEntry(long entryId) {
        // First, move the entry to the recycle bin
        if (moveToRecycleBin(entryId)) {
            // If successful, delete it from the user entries table
            return ourDB.delete(USER_ENTRIES_TABLE, ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}) > 0;
        }
        return false;
    }

    public boolean moveToRecycleBin(long entryId) {
        // Get the entry details from userEntries table
        Cursor cursor = ourDB.query(USER_ENTRIES_TABLE, new String[]{ROW_WEBSITE_URL, ROW_ID_NAME, ROW_PASSCODE, ROW_USER},
                ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(ROW_WEBSITE_URL, cursor.getString(cursor.getColumnIndex(ROW_WEBSITE_URL)));
            cv.put(ROW_ID_NAME, cursor.getString(cursor.getColumnIndex(ROW_ID_NAME)));
            cv.put(ROW_PASSCODE, cursor.getString(cursor.getColumnIndex(ROW_PASSCODE)));
            cv.put(ROW_USER, cursor.getString(cursor.getColumnIndex(ROW_USER)));

            return ourDB.insert(RECYCLE_BIN_TABLE, null, cv) != -1;
        }
        cursor.close();
        return false;
    }

    public boolean restoreEntry(long entryId) {
        // Get the entry details from the recycle bin
        Cursor cursor = ourDB.query(RECYCLE_BIN_TABLE, new String[]{ROW_WEBSITE_URL, ROW_ID_NAME, ROW_PASSCODE, ROW_USER},
                ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(ROW_WEBSITE_URL, cursor.getString(cursor.getColumnIndex(ROW_WEBSITE_URL)));
            cv.put(ROW_ID_NAME, cursor.getString(cursor.getColumnIndex(ROW_ID_NAME)));
            cv.put(ROW_PASSCODE, cursor.getString(cursor.getColumnIndex(ROW_PASSCODE)));
            cv.put(ROW_USER, cursor.getString(cursor.getColumnIndex(ROW_USER)));

            // Add the entry back to the user entries table
            if (ourDB.insert(USER_ENTRIES_TABLE, null, cv) != -1) {
                // If successful, delete it from the recycle bin
                return ourDB.delete(RECYCLE_BIN_TABLE, ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}) > 0;
            }
        }
        cursor.close();
        return false;
    }

    public boolean permanentlyDeleteEntry(long entryId) {
        // Simply delete the entry from the recycle bin
        return ourDB.delete(RECYCLE_BIN_TABLE, ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}) > 0;
    }

    public boolean updateEntry(long entryId, String websiteUrl, String idName, String passcode) {
        ContentValues cv = new ContentValues();
        cv.put(ROW_WEBSITE_URL, websiteUrl);
        cv.put(ROW_ID_NAME, idName);
        cv.put(ROW_PASSCODE, passcode);
        return ourDB.update(USER_ENTRIES_TABLE, cv, ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}) > 0;
    }

    public Cursor getUserEntries(String user) {
        return ourDB.rawQuery("SELECT * FROM " + USER_ENTRIES_TABLE +
                " WHERE " + ROW_USER + " = ?", new String[]{user});
    }

    public Cursor getRecycleBinEntries(String user) {
        return ourDB.rawQuery("SELECT * FROM " + RECYCLE_BIN_TABLE +
                " WHERE " + ROW_USER + " = ?", new String[]{user});
    }

    public void clearRecycleBin() {
        ourDB.delete(RECYCLE_BIN_TABLE, null, null);
    }

    public boolean removeEntryFromRecycleBin(long entryId) {
        return ourDB.delete(RECYCLE_BIN_TABLE, ROW_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)}) > 0;
    }

}
