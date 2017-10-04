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
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "anticor.db";
    public static final String KEY_ID=MyDbContract.DbEntry._ID;

    public static final String TABLE_VOC=MyDbContract.DbEntry.TABLE_VOCABULARY;
    public static final String COLUMN_VOC_ID=MyDbContract.DbEntry.COLUMN_VOC_ID;
    public static final String COLUMN_VOC_KEY=MyDbContract.DbEntry.COLUMN_VOC_KEY;
    public static final String COLUMN_VOC_VALUE=MyDbContract.DbEntry.COLUMN_VOC_VALUE;
    public static final String COLUMN_VOC_PARENT=MyDbContract.DbEntry.COLUMN_VOC_PARENT;
    public static final String COLUMN_VOC_ORDER=MyDbContract.DbEntry.COLUMN_VOC_ORDER;

    public static final String TABLE_AUTH=MyDbContract.DbEntry.TABLE_AUTHORITY;
    public static final String COLUMN_AUTH_ID=MyDbContract.DbEntry.COLUMN_AUTH_ID;
    public static final String COLUMN_AUTH_TITLE=MyDbContract.DbEntry.COLUMN_AUTH_TITLE;
    public static final String COLUMN_AUTH_TEXT=MyDbContract.DbEntry.COLUMN_AUTH_TEXT;
    public static final String COLUMN_AUTH_IMAGE=MyDbContract.DbEntry.COLUMN_AUTH_IMAGE;
    public static final String COLUMN_AUTH_PID=MyDbContract.DbEntry.COLUMN_AUTH_PARENT_ID;

    public MyDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_VOC + " (" +
                MyDbContract.DbEntry._ID + " INTEGER PRIMARY KEY," +
                COLUMN_VOC_ID + " INTEGER," +
                COLUMN_VOC_KEY + " TEXT," +
                COLUMN_VOC_VALUE + " TEXT," +
                COLUMN_VOC_PARENT + " INTEGER," +
                COLUMN_VOC_ORDER + " INTEGER" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_AUTH + " (" +
                MyDbContract.DbEntry._ID + " INTEGER PRIMARY KEY," +
                COLUMN_AUTH_ID + " INTEGER," +
                COLUMN_AUTH_TITLE + " TEXT," +
                COLUMN_AUTH_TEXT + " TEXT," +
                COLUMN_AUTH_IMAGE + " TEXT," +
                COLUMN_AUTH_PID + " INTEGER" +
                ")");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTH);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**Vocabulary**/
    public void clearVocabulary(SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOC, null,null);
        //db.close();
    }

    // Getting vocabulary table count
    public int getVocCount(SQLiteDatabase db) {
        String countQuery = "SELECT  * FROM " + TABLE_VOC;
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Adding to vocabulary table
    public void addVocItem(Vocabulary voc,SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_VOC_ID, voc.getId());
        values.put(COLUMN_VOC_KEY, voc.getKey());
        values.put(COLUMN_VOC_VALUE, voc.getValue());
        values.put(COLUMN_VOC_PARENT, voc.getParent());
        values.put(COLUMN_VOC_ORDER, voc.getOrder());

        // Inserting Row
        db.insert(TABLE_VOC, null, values);
        //db.close(); // Closing database connection
    }

    // Getting vocabulary contents
    public List<Vocabulary> getVocContents(SQLiteDatabase db) {
        List<Vocabulary> vocList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM "+TABLE_VOC;

        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Vocabulary voc = new Vocabulary();
                voc.setRowId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))));
                voc.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VOC_ID)));
                voc.setKey(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOC_KEY)));
                voc.setValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOC_VALUE)));
                voc.setParent(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VOC_PARENT)));
                voc.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VOC_ORDER)));
                // Adding contact to list
                vocList.add(voc);
            } while (cursor.moveToNext());
        }
        return vocList;
    }


    /**Authority**/
    public void clearAuthority(SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_AUTH, null,null);
        //db.close();
    }

    public int getAuthCount(SQLiteDatabase db) {
        String countQuery = "SELECT  * FROM " + TABLE_AUTH;
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Adding to authority table
    public void addAuthItem(Authority auth,SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_AUTH_ID, auth.getId());
        values.put(COLUMN_AUTH_TITLE, auth.getTitle());
        values.put(COLUMN_AUTH_TEXT, auth.getText());
        values.put(COLUMN_AUTH_IMAGE, auth.getImage());
        values.put(COLUMN_AUTH_PID, auth.getParentId());

        // Inserting Row
        db.insert(TABLE_AUTH, null, values);
        //db.close(); // Closing database connection
    }

    // Getting vocabulary contents
    public List<Authority> getAuthContents(SQLiteDatabase db) {
        List<Authority> authList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM "+TABLE_AUTH;

        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Authority auth = new Authority();
                auth.setRowId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))));
                auth.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTH_ID)));
                auth.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTH_TITLE)));
                auth.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTH_TEXT)));
                auth.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTH_IMAGE)));
                auth.setParentId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTH_PID)));
                // Adding contact to list
                authList.add(auth);
            } while (cursor.moveToNext());
        }
        return authList;
    }
}

