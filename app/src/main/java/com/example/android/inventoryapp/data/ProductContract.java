package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/items/ is a valid path for
     * looking at item data. content://com.example.android.inventory/stuff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "stuff".
     */
    public static final String PATH_ITEMS = "items";

    //Constructor made private because instantiating an object of this class is forbidden
    private ProductContract() {
    }

    //Table name is called items and its columns are listed below
    public static final class ProductEntry implements BaseColumns {

        /**
         * The content URI to access the items data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public final static String TABLE_NAME = "items";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_NAME = "product_name";

        public final static String COLUMN_PRODUCT_PRICE = "product_price";

        public final static String COLUMN_PRODUCT_QUANTITY = "product_quantity";

        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";

        public final static String COLUMN_SUPPLIER_PHONE = "supplier_phone";
    }
}
