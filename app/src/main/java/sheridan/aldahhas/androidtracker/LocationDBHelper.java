package sheridan.aldahhas.androidtracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Locale;

public class LocationDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "realtimedb";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "realtime";
    public static final String COLUMN_ID = "uniqueId";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DATE_CREATED = "dateCreated"; // New column


    public LocationDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    public void clearStoredLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LATITUDE + " TEXT,"
                + COLUMN_LONGITUDE + " TEXT,"
                + COLUMN_DATE_CREATED + " TEXT" + ")"; // Date as text

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

        public void insertLocation(String latitude, String longitude, String dateCreated) {
//        public void insertLocation(String latitude, String longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_DATE_CREATED, dateCreated); // Get current date
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Get the current date in a suitable format
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateCreated = new Date();
        return dateFormat.format(dateCreated);
    }

    public ArrayList<HashMap<String, String>> getAllLocations() {
        ArrayList<HashMap<String, String>> locations = new ArrayList<>();

        // Select all query
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String latitude = cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE));
                @SuppressLint("Range") String longitude = cursor.getString(cursor.getColumnIndex(COLUMN_LONGITUDE));
                @SuppressLint("Range") String dateCreated = cursor.getString(cursor.getColumnIndex(COLUMN_DATE_CREATED));

                HashMap<String, String> location = new HashMap<>();
                location.put("latitude", latitude);
                location.put("longitude", longitude);
                location.put("dateCreated", dateCreated);
                locations.add(location);

            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return locations;
    }

}
