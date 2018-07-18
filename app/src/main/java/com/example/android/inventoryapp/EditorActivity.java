package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INVENTORY_LOADER = 0;
    EditText productNameEditText;
    EditText productPriceEditText;
    EditText quantityAdjFactor;
    EditText productQuantityEditText;
    EditText supplierNameEditText;
    EditText supplierPhoneEditText;
    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri currentItemUri;
    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mitemHasChanged boolean to true.
     */
    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        productNameEditText = findViewById(R.id.product_name_et);
        productPriceEditText = findViewById(R.id.product_price_et);
        productQuantityEditText = findViewById(R.id.product_quantity_et);
        supplierNameEditText = findViewById(R.id.supplier_name_et);
        supplierPhoneEditText = findViewById(R.id.supplier_phone_et);

        productNameEditText.setOnTouchListener(onTouchListener);
        productQuantityEditText.setOnTouchListener(onTouchListener);
        productPriceEditText.setOnTouchListener(onTouchListener);
        supplierNameEditText.setOnTouchListener(onTouchListener);
        supplierPhoneEditText.setOnTouchListener(onTouchListener);

        //Get the intent to extract the passed Uri if available (null if not)
        Intent intent = getIntent();
        currentItemUri = intent.getData();

        //Get the adjustment factor for the quantity in EditorActivity
        quantityAdjFactor = findViewById(R.id.adjustment_factor_et);

        //Get the increase and decrease button and use the adjustment factor to increase
        //or decrease quantity accordingly
        Button qtyDecreaseBtn = findViewById(R.id.editor_quantity_button_decrease);
        Button qtyIncreaseBtn = findViewById(R.id.editor_quantity_button_increase);

        //Set click listener on the decrease button and decrease the quantity
        //according the adjustment factor, check for validation before changing data
        qtyDecreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCurrentQty = productQuantityEditText.getText().toString().trim();
                int adjustmentFactor = Integer.parseInt(quantityAdjFactor.getText().toString().trim());
                if (!TextUtils.isEmpty(strCurrentQty)) {
                    int currentQty = Integer.parseInt(strCurrentQty);
                    if (currentQty < 0) {
                        return;
                    } else {
                        if (adjustmentFactor > 0) {
                            currentQty = currentQty - adjustmentFactor;
                            if (currentQty >= 0) {
                                productQuantityEditText.setText(String.valueOf(currentQty));
                                mItemHasChanged = true;
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.quantity_less_than_zero, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.adj_factor_less_than_one, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        //Set click listener on the increase button and increase the quantity
        //according the adjustment factor, check for validation before changing data
        qtyIncreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCurrentQty = productQuantityEditText.getText().toString();
                int adjustmentFactor = Integer.parseInt(quantityAdjFactor.getText().toString().trim());
                int currentQty = 0;
                if (adjustmentFactor > 0) {
                    mItemHasChanged = true;
                    if (!TextUtils.isEmpty(strCurrentQty)) {
                        currentQty = Integer.parseInt(strCurrentQty);
                        currentQty = currentQty + adjustmentFactor;
                        productQuantityEditText.setText(String.valueOf(currentQty));
                    } else {
                        currentQty = currentQty + adjustmentFactor;
                        ;
                        productQuantityEditText.setText(String.valueOf(currentQty));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.adj_factor_less_than_one, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Set click listener on the call phone button and check for number validity
        //before starting Action_Dial intent
        Button callSupplierButton = findViewById(R.id.call_supplier_btn);
        callSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strSupplierPhone = supplierPhoneEditText.getText().toString().trim();
                if (strSupplierPhone == null || TextUtils.isEmpty(strSupplierPhone)) {
                    Toast.makeText(getApplicationContext(), R.string.invalid_or_empty_phone_number, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    String uri = "tel:" + strSupplierPhone;
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });

        //Check if the activity has uri and determine the type of process accordingly
        //by setting the title first and then initiating the Loader to grab the currently
        //selected item to be edited
        if (currentItemUri == null) {
            // This is a new item, so change the app bar to say "Add New Item"
            setTitle(getString(R.string.editor_activity_add_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_edit_item));
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_options, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.editor_delete_option);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.editor_save_option:
                // Save product to database
                saveProduct();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.editor_delete_option:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        // Only perform the delete if this is an existing item.
        if (currentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the currentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    //Saving the product into the database
    private void saveProduct() {

        //Get user input from the edit text views in EditorActivity
        String productName = productNameEditText.getText().toString().trim();
        String strProductQuantity = (productQuantityEditText.getText().toString().trim());
        String strProductPrice = (productPriceEditText.getText().toString().trim());
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString().trim();

        Integer productQuantity;
        Integer productPrice;

        if (TextUtils.isEmpty(strProductQuantity) || strProductQuantity == null) {
            productQuantity = null;
        } else {
            productQuantity = Integer.parseInt(strProductQuantity);
        }

        if (TextUtils.isEmpty(strProductPrice) || strProductPrice == null) {
            productPrice = null;
        } else {
            productPrice = Integer.parseInt(strProductPrice);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierName);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);

        if (currentItemUri == null) {

            currentItemUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);

            if (currentItemUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful), Toast.LENGTH_SHORT).show();
                // Exit activity
                finish();
            }
        } else {

            int countUpdatedRows = getContentResolver().update(currentItemUri, contentValues,
                    null, null);
            if (countUpdatedRows == 0) {
                Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                // Exit activity
                finish();
            }
        }
    }

    //Build the confirmation alert dialog to keep editing or discard changes before exiting activity
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Hook the alert dialog to the back button
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,      // Parent activity context
                currentItemUri,                   // Query the content URI for the current item
                projection,                       // Columns to include in the resulting Cursor
                null,                     // No selection clause
                null,                  // No selection arguments
                null);                   // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            int productNameIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
            int productQuantityIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productPriceIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER_PHONE);

            String productName = cursor.getString(productNameIndex);
            int productQuantity = cursor.getInt(productQuantityIndex);
            int productPrice = cursor.getInt(productPriceIndex);
            String supplierName = cursor.getString(supplierNameIndex);
            String supplierPhone = cursor.getString(supplierPhoneIndex);

            productNameEditText.setText(productName);
            productQuantityEditText.setText(String.valueOf(productQuantity));
            productPriceEditText.setText(String.valueOf(productPrice));
            supplierNameEditText.setText(supplierName);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEditText.setText("");
        productQuantityEditText.setText("");
        productPriceEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");

    }
}


