package com.example.android.bicycleshop;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bicycleshop.data.BicycleContract;
import com.example.android.bicycleshop.data.BicycleContract.BicycleEntry;

import static android.R.attr.name;
import static android.R.attr.type;

/**
 * Created by izzystannett on 09/07/2017.
 */

public class BicycleCursorAdapter extends CursorAdapter {

    //set up constructor
    public BicycleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //make a blank list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //binds data to the list item view
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.list_image_view);
        TextView modelTextView = (TextView)view.findViewById(R.id.model_list_item);
        TextView priceTextView = (TextView)view.findViewById(R.id.price_list_item);
        TextView quantityTextView = (TextView)view.findViewById(R.id.quantity_list_item);



        //find the columns of the attributes that we want to display
        int pictureColumnIndex = cursor.getColumnIndexOrThrow(BicycleEntry.COLUMN_IMAGE);
        int modelColumnIndex = cursor.getColumnIndexOrThrow(BicycleEntry.COLUMN_BIKE_MODEL);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(BicycleEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(BicycleEntry.COLUMN_QUANTITY);

        //retrieve data from the appropriate columns
        String bicyclePicture = cursor.getString(pictureColumnIndex);
        String bicycleModel = cursor.getString(modelColumnIndex);
        String bicyclePrice = cursor.getString(priceColumnIndex);
        final int bicycleQuantity = cursor.getInt(quantityColumnIndex);

        //convert the picture to URI
        Uri imageUri;
        if (bicyclePicture != null) {
            imageUri = Uri.parse(bicyclePicture);
        } else {
            imageUri = null;
        }

        // Check if the image uri is null
        if (imageUri != null) {
            // Show the image on the imageView
            pictureImageView.setImageURI(imageUri);
        } else {
            // Show the placeholder instead
            pictureImageView.setImageDrawable(context.getResources().getDrawable(R.drawable
                    .bicycle_shadow, null));
        }

        //update the text views with the values from the database
        modelTextView.setText(bicycleModel);
        String bicyclePriceWithSign = context.getString(R.string.pound_sign) + bicyclePrice;
        priceTextView.setText(bicyclePriceWithSign);
        String quantityAvailable = bicycleQuantity + " " + context.getString(R.string.left_in_stock);
        quantityTextView.setText(quantityAvailable);

        //get current URI
        final Uri uri = ContentUris.withAppendedId(BicycleContract.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(BicycleEntry._ID)));

        //method for when Sale button is clicked, to reduce quantity by one if one in stock
        //and save the new quantity into the database
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bicycleQuantity > 0) {
                    int reducedQuantity = bicycleQuantity - 1;

                    // Create a new ContentValue object with reduced quantity
                    ContentValues values = new ContentValues();
                    values.put(BicycleEntry.COLUMN_QUANTITY, reducedQuantity);
                    // Pass the new values into the database
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    // Inform the user that quantity is zero and can't be updated
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
