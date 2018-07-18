package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.util.ArrayList;
import java.util.List;

public class ItemProvider extends ContentProvider {

    public static final int ITEMS = 100;
    public static final int ITEM_ID = 101;
    //Uri matcher to determine the uri type if it is for all items or specific item
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    InventoryDbHelper inventoryDbHelper;

    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = inventoryDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case ITEM_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_error) + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                return ProductEntry.CONTENT_LIST_TYPE;

            case ITEM_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.unknown_uri)
                        + uri + getContext().getString(R.string.with_match) + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case ITEMS:
                return insertItem(uri, values);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_error) + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

        boolean productInfoStatus = validateProductInfo(values);

        long newRowId = -1;

        if (productInfoStatus) {
            newRowId = database.insert(ProductEntry.TABLE_NAME, null, values);
        }

        if (newRowId == -1) {
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, newRowId);
    }

    //Validation method for all input data
    private boolean validateProductInfo(ContentValues values) {

        String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        Integer productQuantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        Integer productPrice = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        String supplierPhone = values.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE);

        //Validate each item individually and show corresponding error message for each field
        boolean productNameChk = validateStringValue(productName, getContext().getString(R.string.product_name));
        boolean productQuantityChk = validateIntegerValue(productQuantity, getContext().getString(R.string.product_quantity));
        boolean productPriceChk = validateIntegerValue(productPrice, getContext().getString(R.string.product_price));
        boolean supplierNameChk = validateStringValue(supplierName, getContext().getString(R.string.supplier_name));
        boolean supplierPhoneChk = validateStringValue(supplierPhone, getContext().getString(R.string.supplier_phone));

        if (productNameChk && productQuantityChk && productPriceChk && supplierNameChk &&
                supplierPhoneChk) {
            return true;
        } else {
            return false;
        }
    }

    //Validation for integer based inputs
    private boolean validateIntegerValue(Integer value, String field) {

        if (value == null || value < 0) {
            Toast.makeText(getContext(), field + " is Empty or Invalid", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    //Validation for String based input
    private boolean validateStringValue(String value, String field) {

        if (value == null || TextUtils.isEmpty(value)) {
            Toast.makeText(getContext(), field + getContext().getString(R.string.empty_or_invalid), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int countRowsDeleted = 0;

        switch (match) {

            case ITEMS:
                // Delete all rows that match the selection and selection args
                countRowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                countRowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                Toast.makeText(getContext(), R.string.deletion_failed, Toast.LENGTH_SHORT).show();
                countRowsDeleted = 0;
        }

        if (countRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return countRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);

            case ITEM_ID:
                // For the item_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_error) + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        int countRowsUpdated = 0;

        List<Boolean> updatesValidations = new ArrayList<>();
        int countInvalidUpdates = 0;

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            updatesValidations.add(validateStringValue(productName, getContext().getString(R.string.product_name)));
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer productQuantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            updatesValidations.add(validateIntegerValue(productQuantity, getContext().getString(R.string.product_quantity)));
        }

        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer productPrice = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            updatesValidations.add(validateIntegerValue(productPrice, getContext().getString(R.string.product_price)));
        }

        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
            updatesValidations.add(validateStringValue(supplierName, getContext().getString(R.string.supplier_name)));
        }

        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE);
            updatesValidations.add(validateStringValue(supplierPhone, getContext().getString(R.string.supplier_phone)));
        }

        for (boolean itemStatus : updatesValidations) {
            if (itemStatus == false) {
                countInvalidUpdates++;
            }
        }

        if (countInvalidUpdates > 0) {
            return 0;
        } else {

            // Otherwise, get writable database to update the data
            SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

            //Update the table with the data and return number of rows updated
            countRowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        }

        if (countRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return countRowsUpdated;
    }
}
