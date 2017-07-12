package com.example.android.bicycleshop.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by izzystannett on 09/07/2017.
 */

//the contract class, used to define the layout of the SQL database

public final class BicycleContract {


    //empty constructor
    private BicycleContract() {};

    //construct the URI by defining it's content authority, schema and path
    public static final String CONTENT_AUTHORITY = "com.example.android.bicycles";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BICYCLES = "bicycles";

    //the full content URI to access the pet data in the provider
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BICYCLES);

    //inner class that defines constant values for the table
    //each entry is a single Bicycle
    public static final class BicycleEntry implements BaseColumns {
        //TODO define content URI to access the pet data in the provider

        //set up MIME types to pass into the Content Provider's getType method.
        //for whole table
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BICYCLES;
        //and for single pet
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BICYCLES;

        //name of database
        public static final String TABLE_NAME = "bicycles";
        //unique ID for each bicycle, of type integer
        public static final String _ID = BaseColumns._ID;
        //TODO insert image column as text, see hackathon notes
        //bicycle model, of type string
        public static final String COLUMN_BIKE_MODEL = "model";
        //bicycle type, of type String
        public static final String COLUMN_BIKE_TYPE = "type";
        //quantity in stock, of type integer
        public static final String COLUMN_QUANTITY = "quantity";
        //price of the bike, of type integer
        public static final String COLUMN_PRICE = "price";
        //supplier email address, of type String
        public static final String COLUMN_SUPPLIER = "supplier";

        //possible values for Bike Type
        public static final int TYPE_TOURING = 0;
        public static final int TYPE_ROAD = 1;
        public static final int TYPE_TRIATHLON = 2;
        public static final int TYPE_MOUNTAIN = 3;
        public static final int TYPE_HYBRID = 4;
        public static final int TYPE_UNKNOWN = 5;

        //returns whether the bike is a given type
        public static boolean isValidType(int type){
            if (type == TYPE_TOURING || type == TYPE_ROAD || type == TYPE_TRIATHLON || type == TYPE_MOUNTAIN
                || type == TYPE_HYBRID || type == TYPE_UNKNOWN){
                return true;
            }
            return false;
            }
        }
    }

