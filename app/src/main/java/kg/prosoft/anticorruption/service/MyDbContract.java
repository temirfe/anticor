package kg.prosoft.anticorruption.service;

import android.provider.BaseColumns;

/**
 * Created by ProsoftPC on 7/31/2017.
 */

public class MyDbContract {
    private MyDbContract(){}

    /* Inner class that defines the table contents */
    public static class DbEntry implements BaseColumns {
        public static final String TABLE_VOCABULARY = "vocabulary";
        public static final String COLUMN_VOC_ID = "voc_id";
        public static final String COLUMN_VOC_KEY = "voc_key";
        public static final String COLUMN_VOC_VALUE = "voc_value";
        public static final String COLUMN_VOC_PARENT = "voc_parent";
        public static final String COLUMN_VOC_ORDER = "voc_order";

        public static final String TABLE_AUTHORITY = "authority";
        public static final String COLUMN_AUTH_ID = "auth_id";
        public static final String COLUMN_AUTH_TITLE = "auth_title";
        public static final String COLUMN_AUTH_TEXT = "auth_text";
        public static final String COLUMN_AUTH_IMAGE = "auth_image";
        public static final String COLUMN_AUTH_PARENT_ID = "auth_parent_id";
    }
}
