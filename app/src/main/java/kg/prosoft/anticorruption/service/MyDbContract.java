package kg.prosoft.anticorruption.service;

import android.provider.BaseColumns;

/**
 * Created by ProsoftPC on 7/31/2017.
 */

public class MyDbContract {
    private MyDbContract(){}

    /* Inner class that defines the table contents */
    public static class DbEntry implements BaseColumns {
        public static final String TABLE_NAME = "basket";
        public static final String TABLE_NAME_LOOKUP = "lookup";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_AMOUNT = "amount";
    }
}
