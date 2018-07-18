package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import com.example.android.inventoryapp.data.ProductContract;

public class ItemCursorAdapter extends CursorAdapter {

    Context currentContext;

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        currentContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView productIdTextView = view.findViewById(R.id.product_id_tv);
        TextView productNameTextView = view.findViewById(R.id.product_name_tv);
        TextView productQuantityTextView = view.findViewById(R.id.product_quantity_tv);
        TextView productPriceTextView = view.findViewById(R.id.product_price_tv);

        int productIdIndex = cursor.getColumnIndexOrThrow(ProductEntry._ID);
        int productNameIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
        int productQuantityIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productPriceIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);

        int productId = cursor.getInt(productIdIndex);
        String productName = cursor.getString(productNameIndex);
        int productQuantity = cursor.getInt(productQuantityIndex);
        int productPrice = cursor.getInt(productPriceIndex);

        productIdTextView.setText(String.valueOf(productId));
        productNameTextView.setText(productName);
        productQuantityTextView.setText(String.valueOf(productQuantity));
        productPriceTextView.setText(String.valueOf(productPrice));
    }

    //Override the getView method to hook OnClickListener to the Sale button in CatalogActivity
    //Button decrease quantity by one and validation is performed to make sure
    //we don't reduce less than zero quantity
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = super.getView(position, convertView, parent);
        final TextView currentIdTextView = view.findViewById(R.id.product_id_tv);
        final TextView currentItemQuantityTV = view.findViewById(R.id.product_quantity_tv);


        Button btn = view.findViewById(R.id.sale_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strProductId = currentIdTextView.getText().toString();
                long _id = Long.parseLong(strProductId);
                Uri currentItemUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, _id);
                String strCurrentItemQuantity = currentItemQuantityTV.getText().toString();
                int currentItemQuantity = Integer.parseInt(strCurrentItemQuantity);
                currentItemQuantity--;
                ContentValues newQuantityValue = new ContentValues();
                newQuantityValue.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentItemQuantity);
                int rowUpdated = currentContext.getContentResolver().update(currentItemUri, newQuantityValue, null, null);
                if (rowUpdated != 0) {
                    currentItemQuantityTV.setText(String.valueOf(currentItemQuantity));
                    Toast.makeText(currentContext, R.string.item_sold, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(currentContext, R.string.item_not_sold, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
