package com.example.android.inventoryapp.data;


import android.content.Context;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL );";

        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Not implemented yet.
    }
}
