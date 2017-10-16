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
    public static final int DATABASE_VERSION = 18;
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
    public static final String COLUMN_AUTH_RATING=MyDbContract.DbEntry.COLUMN_AUTH_RATING;
    public static final String COLUMN_AUTH_COMMENTS=MyDbContract.DbEntry.COLUMN_AUTH_COMMENTS;
    public static final String COLUMN_AUTH_REPORTS=MyDbContract.DbEntry.COLUMN_AUTH_REPORTS;

    public static final String TABLE_NEWS=MyDbContract.DbEntry.TABLE_NEWS;
    public static final String COLUMN_NEWS_ID=MyDbContract.DbEntry.COLUMN_NEWS_ID;
    public static final String COLUMN_NEWS_TITLE=MyDbContract.DbEntry.COLUMN_NEWS_TITLE;
    public static final String COLUMN_NEWS_TEXT=MyDbContract.DbEntry.COLUMN_NEWS_TEXT;
    public static final String COLUMN_NEWS_DESC=MyDbContract.DbEntry.COLUMN_NEWS_DESC;
    public static final String COLUMN_NEWS_DATE=MyDbContract.DbEntry.COLUMN_NEWS_DATE;
    public static final String COLUMN_NEWS_IMG=MyDbContract.DbEntry.COLUMN_NEWS_IMG;
    public static final String COLUMN_NEWS_CTG=MyDbContract.DbEntry.COLUMN_NEWS_CTG;
    public static final String COLUMN_NEWS_VIEWS=MyDbContract.DbEntry.COLUMN_NEWS_VIEWS;

    public static final String TABLE_REPORT=MyDbContract.DbEntry.TABLE_REPORT;
    public static final String COLUMN_REPORT_ID=MyDbContract.DbEntry.COLUMN_REPORT_ID;
    public static final String COLUMN_REPORT_TITLE=MyDbContract.DbEntry.COLUMN_REPORT_TITLE;
    public static final String COLUMN_REPORT_TEXT=MyDbContract.DbEntry.COLUMN_REPORT_TEXT;
    public static final String COLUMN_REPORT_DESC=MyDbContract.DbEntry.COLUMN_REPORT_DESC;
    public static final String COLUMN_REPORT_DATE=MyDbContract.DbEntry.COLUMN_REPORT_DATE;
    public static final String COLUMN_REPORT_AUTHORITY_ID=MyDbContract.DbEntry.COLUMN_REPORT_AUTHORITY_ID;
    public static final String COLUMN_REPORT_CATEGORY_ID=MyDbContract.DbEntry.COLUMN_REPORT_CATEGORY_ID;
    public static final String COLUMN_REPORT_CITY_ID=MyDbContract.DbEntry.COLUMN_REPORT_CITY_ID;
    public static final String COLUMN_REPORT_CITY_TITLE=MyDbContract.DbEntry.COLUMN_REPORT_CITY_TITLE;
    public static final String COLUMN_REPORT_TYPE_ID=MyDbContract.DbEntry.COLUMN_REPORT_TYPE_ID;
    public static final String COLUMN_REPORT_USER_ID=MyDbContract.DbEntry.COLUMN_REPORT_USER_ID;
    public static final String COLUMN_REPORT_LAT=MyDbContract.DbEntry.COLUMN_REPORT_LAT;
    public static final String COLUMN_REPORT_LNG=MyDbContract.DbEntry.COLUMN_REPORT_LNG;

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
                COLUMN_AUTH_PID + " INTEGER," +
                COLUMN_AUTH_RATING + " INTEGER," +
                COLUMN_AUTH_COMMENTS + " INTEGER," +
                COLUMN_AUTH_REPORTS + " INTEGER" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_NEWS + " (" +
                MyDbContract.DbEntry._ID + " INTEGER PRIMARY KEY," +
                COLUMN_NEWS_ID + " INTEGER," +
                COLUMN_NEWS_TITLE + " TEXT," +
                COLUMN_NEWS_TEXT + " TEXT," +
                COLUMN_NEWS_IMG + " TEXT," +
                COLUMN_NEWS_DESC + " TEXT," +
                COLUMN_NEWS_DATE + " TEXT," +
                COLUMN_NEWS_CTG + " INTEGER," +
                COLUMN_NEWS_VIEWS + " INTEGER" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_REPORT + " (" +
                MyDbContract.DbEntry._ID + " INTEGER PRIMARY KEY," +
                COLUMN_REPORT_ID + " INTEGER," +
                COLUMN_REPORT_TITLE + " TEXT," +
                COLUMN_REPORT_TEXT + " TEXT," +
                COLUMN_REPORT_DESC + " TEXT," +
                COLUMN_REPORT_DATE + " TEXT," +
                COLUMN_REPORT_CATEGORY_ID + " INTEGER," +
                COLUMN_REPORT_AUTHORITY_ID + " INTEGER," +
                COLUMN_REPORT_CITY_ID + " INTEGER," +
                COLUMN_REPORT_CITY_TITLE + " TEXT," +
                COLUMN_REPORT_TYPE_ID + " INTEGER," +
                COLUMN_REPORT_USER_ID + " INTEGER," +
                COLUMN_REPORT_LAT + " REAL," +
                COLUMN_REPORT_LNG + " REAL" +
                ")");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT);
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
        values.put(COLUMN_AUTH_RATING, auth.getRating());
        values.put(COLUMN_AUTH_COMMENTS, auth.getCommentCount());
        values.put(COLUMN_AUTH_REPORTS, auth.getReportCount());

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
                auth.setRating(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTH_RATING)));
                auth.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTH_COMMENTS)));
                auth.setReportCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTH_REPORTS)));
                // Adding contact to list
                authList.add(auth);
            } while (cursor.moveToNext());
        }
        return authList;
    }

    /**News**/
    public void clearNews(SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NEWS, null,null);
        //db.close();
    }

    public void addNewsItem(News news,SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NEWS_ID, news.getId());
        values.put(COLUMN_NEWS_TITLE, news.getTitle());
        values.put(COLUMN_NEWS_TEXT, news.getText());
        values.put(COLUMN_NEWS_DESC, news.getDescription());
        values.put(COLUMN_NEWS_DATE, news.getRawDate());
        values.put(COLUMN_NEWS_IMG, news.getImage());
        values.put(COLUMN_NEWS_CTG, news.getCategoryId());
        values.put(COLUMN_NEWS_VIEWS, news.getViews());

        // Inserting Row
        db.insert(TABLE_NEWS, null, values);
        //db.close(); // Closing database connection
    }

    public ArrayList<News> getNewsContents(SQLiteDatabase db) {
        ArrayList<News> mList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+TABLE_NEWS;

        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                News news = new News();
                news.setRowId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))));
                news.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NEWS_ID)));
                news.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEWS_TITLE)));
                news.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEWS_DESC)));
                news.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEWS_TEXT)));
                news.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEWS_DATE)));
                news.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEWS_IMG)));
                news.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NEWS_CTG)));
                news.setViews(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NEWS_VIEWS)));
                // Adding contact to list
                mList.add(news);
            } while (cursor.moveToNext());
        }
        return mList;
    }

    /**Report**/
    public void clearReport(SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REPORT, null,null);
        //db.close();
    }

    public void addReportItem(Report report,SQLiteDatabase db) {
        //SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_REPORT_ID, report.getId());
        values.put(COLUMN_REPORT_TITLE, report.getTitle());
        values.put(COLUMN_REPORT_TEXT, report.getText());
        values.put(COLUMN_REPORT_DESC, report.getDescription());
        values.put(COLUMN_REPORT_DATE, report.getRawDate());
        values.put(COLUMN_REPORT_USER_ID, report.getUserId());
        values.put(COLUMN_REPORT_CATEGORY_ID, report.getCategoryId());
        values.put(COLUMN_REPORT_AUTHORITY_ID, report.getAuthorityId());
        values.put(COLUMN_REPORT_TYPE_ID, report.getAuthorityId());
        values.put(COLUMN_REPORT_CITY_ID, report.getCityId());
        values.put(COLUMN_REPORT_CITY_TITLE, report.getCityTitle());
        values.put(COLUMN_REPORT_LAT, report.getLat());
        values.put(COLUMN_REPORT_LNG, report.getLng());

        // Inserting Row
        db.insert(TABLE_REPORT, null, values);
        //db.close(); // Closing database connection
    }

    public ArrayList<Report> getReportContents(SQLiteDatabase db) {
        ArrayList<Report> mList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+TABLE_REPORT;

        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Report report = new Report();
                report.setRowId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))));
                report.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPORT_ID)));
                report.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORT_TITLE)));
                report.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORT_DESC)));
                report.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORT_TEXT)));
                report.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORT_DATE)));
                report.setAuthorityId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPORT_AUTHORITY_ID)));
                report.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPORT_CATEGORY_ID)));
                report.setTypeId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPORT_TYPE_ID)));
                report.setCityId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPORT_CITY_ID)));
                report.setCityTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORT_CITY_TITLE)));
                report.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPORT_USER_ID)));
                report.setLat(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REPORT_LAT)));
                report.setLng(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REPORT_LNG)));
                // Adding contact to list
                mList.add(report);
            } while (cursor.moveToNext());
        }
        return mList;
    }
}

