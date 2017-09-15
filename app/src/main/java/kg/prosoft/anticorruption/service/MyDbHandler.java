package kg.prosoft.anticorruption.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ProsoftPC on 7/31/2017.
 */

public class MyDbHandler extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "anticor.db";
    public static final String KEY_ID=MyDbContract.DbEntry._ID;
    public static final String KEY_PROD_ID=MyDbContract.DbEntry.COLUMN_NAME_PRODUCT_ID;
    public static final String KEY_AMOUNT=MyDbContract.DbEntry.COLUMN_NAME_AMOUNT;
    public static final String TABLE_BASKET=MyDbContract.DbEntry.TABLE_NAME;

    public MyDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        /*db.execSQL("CREATE TABLE " + TABLE_BASKET + " (" +
                MyDbContract.DbEntry._ID + " INTEGER PRIMARY KEY," +
                KEY_PROD_ID + " INTEGER," +
                KEY_AMOUNT + " TEXT)");

        db.execSQL("CREATE TABLE lookup (" +
                MyDbContract.DbEntry._ID + " INTEGER PRIMARY KEY," +
                "lookup_id INTEGER, title TEXT, `text` TEXT)");*/
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + MyDbContract.DbEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS lookup");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void clearLookup(SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();
        db.delete("lookup", null,null);
        //db.close();
    }


    // Getting contacts Count
    public int getBasketCount(SQLiteDatabase db) {
        String countQuery = "SELECT  * FROM " + TABLE_BASKET;
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }



    // Adding to basket
    public void addLookupItem(Lookup lookup,SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("lookup_id", lookup.getLookupId());
        values.put("title", lookup.getTitle());
        values.put("text", lookup.getText());

        // Inserting Row
        db.insert("lookup", null, values);
        //db.close(); // Closing database connection
    }
    // Getting basket contents
    public List<Lookup> getLookupContents(SQLiteDatabase db) {
        List<Lookup> basketList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM lookup";

        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Lookup basket = new Lookup();
                basket.setID(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))));
                basket.setLookupId(cursor.getInt(cursor.getColumnIndexOrThrow("lookup_id")));
                basket.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                basket.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
                // Adding contact to list
                basketList.add(basket);
            } while (cursor.moveToNext());
        }

        // return contact list
        return basketList;
    }
}

