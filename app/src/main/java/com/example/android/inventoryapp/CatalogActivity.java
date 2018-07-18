package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    ItemCursorAdapter itemCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        /*
        This button is to be used in stage 2 of this app.
        For now it opens empty EditorActivity
        */
        FloatingActionButton mFab = findViewById(R.id.fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView itemsListView = findViewById(R.id.inventory_list_view);

        View emptyView = findViewById(R.id.empty_view);

        itemsListView.setEmptyView(emptyView);

        itemCursorAdapter = new ItemCursorAdapter(this, null);

        itemsListView.setAdapter(itemCursorAdapter);

        //Item click listener to edit currently touched item
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    //Inserting dummy data in items table to test the database read and delete methods
    private void insertDummyData() {

        // Create a ContentValues object where column names are the keys,
        // and dummy product's attributes are the values.
        ContentValues contentValues = new ContentValues();

        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, "Metal Fan");
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 10);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, 5);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Mark");
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_PHONE, "555-12345");

        //Get the Uri of the newly inserted row and display it to user in toast message
        Uri newRowUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);

        if (newRowUri == null) {
            Toast.makeText(this, R.string.insert_test_item_failed, Toast.LENGTH_SHORT).show();
        }
    }

    //Inflate the option menu with catalog_options menu resource file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_options, menu);
        return true;
    }

    //Methods to invoke for each chosen option in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            //Insert dummy test data in items table
            case R.id.action_insert_dummy_product:
                insertDummyData();
                return true;

            //Delete all rows in the items table
            case R.id.action_delete_all:
                clearProductsTable();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //This method will delete all rows in the items table without deleting the table
    public void clearProductsTable() {
        int countRowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Toast.makeText(this, String.format(getString(R.string.all_count_rows_deleted), countRowsDeleted), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        itemCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        itemCursorAdapter.swapCursor(null);
    }
}
