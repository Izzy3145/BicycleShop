package com.example.android.bicycleshop;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bicycleshop.data.BicycleContract;
import com.example.android.bicycleshop.data.BicycleContract.BicycleEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.bicycleshop.data.BicycleProvider.LOG_TAG;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //initialize a Cursor loader
    private static final int EDITOR_BICYCLE_LOADER = 0;
    //constant to be used in gallery intent
    private static final int PICK_IMAGE = 0;
    /// Uri of image on the device's storage
    private static Uri mImageUri;
    //initialise the variables that will be used here
    private Uri mCurrentBicycleUri;
    //initialize the spinner
    private Spinner mTypeSpinner;
    //initialize the type
    private int mType = BicycleEntry.TYPE_UNKNOWN;
    //check for valid data
    private boolean validData = true;
    //intialize the views
    private ImageView mImageView;
    private Button mImageButton;
    private EditText mModelEditText;
    private EditText mPriceEditText;
    private int mQuantity;
    private TextView mQuantityTextView;
    private Button mPlusOneStock;
    private Button mLessOneStock;
    private EditText mSupplierEditText;
    //set up the onTouchListener variable, default is false
    private boolean mBicycleHasChanged = false;
    //set up the on TouchListener method, to be used later in onCreate
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBicycleHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //examine the intent that led to opening the editor
        //if the intent does not contain a URI, then the user is trying to add a new bicycle
        //if the intent does contain a URI, then the user is trying to edit an existing bicycle
        Intent intent = getIntent();
        mCurrentBicycleUri = intent.getData();

        if (mCurrentBicycleUri == null) {
            setTitle(R.string.add_new_bicycle);

            //call the onPrepareOptions method defined below, and remove the 'delete' option
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_bicycle);

            //if the user wants to edit existing bicycle data, we need to query the database
            //we need to query the database to obtain existing data
            //so here we intialise the Cursor Loader
            getLoaderManager().initLoader(EDITOR_BICYCLE_LOADER, null, this);
        }

        //set up spinner
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
        setupSpinner();

        //set other views
        mImageView = (ImageView) findViewById(R.id.image_view);
        mImageButton = (Button) findViewById(R.id.image_button);
        mModelEditText = (EditText) findViewById(R.id.model);
        mPriceEditText = (EditText) findViewById(R.id.price);
        mSupplierEditText = (EditText) findViewById(R.id.supplier);
        mQuantityTextView = (TextView) findViewById(R.id.quantity);

        mPlusOneStock = (Button) findViewById(R.id.plus_one_stock);
        mPlusOneStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
                mQuantityTextView.setText(String.valueOf(mQuantity));
            }
        });

        mLessOneStock = (Button) findViewById(R.id.less_one_stock);
        mLessOneStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0) {
                    mQuantity--;
                    mQuantityTextView.setText(String.valueOf(mQuantity));
                } else {
                    Toast.makeText(EditorActivity.this, getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //set up onTouchListeners on each view, so we know when something has been changed
        mImageButton.setOnTouchListener(mTouchListener);
        mModelEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPlusOneStock.setOnTouchListener(mTouchListener);
        mLessOneStock.setOnTouchListener(mTouchListener);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }

                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }

    //onActivityResult method gets called when image view is clicked
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            //make sure request was successful
            if (resultCode == RESULT_OK) {
                mImageUri = data.getData();
                // Check if the image uri is null
                if (mImageUri != null) {
                    // Show the image on the imageView
                    mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
                } else {
                    // Show the placeholder instead
                    mImageView.setImageDrawable(getResources().getDrawable(R.drawable.bicycle_shadow, null));
                }
            }
        }
    }

    //get bitmap from Uri
    //this code taken from https://github.com/crlsndrsjmnz/MyShareImageExample/blob/master/app/src/main/java/co/carlosandresjimenez/android/myshareimageexample/MainActivity.java
    //as suggested in a 1-2-1 session with Kunal
    public Bitmap getBitmapFromUri(Uri mImageUri) {

        if (mImageUri == null || mImageUri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(mImageUri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(mImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    //set up spinner for 'Type'
    private void setupSpinner() {
        //set up array adapter to take spinner options
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //apply spinner to adapter
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_tourer))) {
                        mType = BicycleEntry.TYPE_TOURING;
                    } else if (selection.equals(getString(R.string.type_road))) {
                        mType = BicycleEntry.TYPE_ROAD;
                    } else if (selection.equals(getString(R.string.type_hybrid))) {
                        mType = BicycleEntry.TYPE_HYBRID;
                    } else if (selection.equals(getString(R.string.type_triathlon))) {
                        mType = BicycleEntry.TYPE_TRIATHLON;
                    } else if (selection.equals(getString(R.string.type_mountain))) {
                        mType = BicycleEntry.TYPE_MOUNTAIN;
                    } else {
                        mType = BicycleEntry.TYPE_UNKNOWN;
                    }
                }
            }

            // AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = BicycleEntry.TYPE_UNKNOWN;
            }
        });
    }

    //inflate the menu, with options for save and delete in the Edit Bicycle screen
    //and just the option for delete in the Add Bicycle screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    //onPrepareOptionsMenu is called when invalidateOptionsMenu is called in onCreate
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBicycleUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //define the required actions upon selecting the various options in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //if save selected, save bicycle to database, then exit the activity
            case R.id.action_save:
                saveBicycle();
                if (validData) {
                    finish();
                } else {
                    validData = true;
                }
                return true;
            //if delete selected, show deletion dialogue
            case R.id.action_delete:
                deletionDialogue();
                return true;
            //if home button pressed, check for changes and finish activity
            case android.R.id.home:
                // If some fields have changed, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // "Discard" button clicked, close the current activity.
                                finish();
                            }
                        };

                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If no fields have  changed, continue with handling back button press
        if (!mBicycleHasChanged) {
            super.onBackPressed();
            return;
        }
        // If some fields have changed, setup a dialog to warn the user.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // "Discard" button clicked, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //unsaved changes dialogue, to sometimes be used when back button is pressed
    private void showUnsavedChangesDialog(

            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set initial message
        builder.setMessage(R.string.unsaved_changes_message);
        //set positive and negative response messages, if negative, dismiss dialogue
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.continue_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //set up method for INSERTING or UPDATING a bicycle
    private void saveBicycle() {
        //retrieve input from edit text fields, and check if they're blank
        String modelString = mModelEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        if (mCurrentBicycleUri == null &&
                TextUtils.isEmpty(modelString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(supplierString) && mType == BicycleEntry.TYPE_UNKNOWN) {
            //if blank, exit as no changes need be changed
            return;
        }

        //before entering values into the ContentValues object, check for null values
        if (TextUtils.isEmpty(modelString)) {
            Toast.makeText(this, getString(R.string.valid_model), Toast.LENGTH_SHORT).show();
            validData = false;
        } else if (mType < 0) {
            Toast.makeText(this, getString(R.string.valid_type), Toast.LENGTH_SHORT).show();
            validData = false;
        } else if (mQuantity < 0) {
            Toast.makeText(this, getString(R.string.valid_quantity), Toast.LENGTH_SHORT).show();
            validData = false;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.valid_price), Toast.LENGTH_SHORT).show();
            validData = false;
        } else if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, getString(R.string.valid_supplier), Toast.LENGTH_SHORT).show();
            validData = false;
        }

        //to enable us to pass data into the database, we need to create a ContentValues object
        ContentValues values = new ContentValues();
        //when error checking passed, enter values into ContentValues object
        values.put(BicycleEntry.COLUMN_BIKE_MODEL, modelString);
        values.put(BicycleEntry.COLUMN_BIKE_TYPE, mType);
        values.put(BicycleEntry.COLUMN_QUANTITY, mQuantity);
        values.put(BicycleEntry.COLUMN_IMAGE, String.valueOf(mImageUri));
        values.put(BicycleEntry.COLUMN_PRICE, priceString);
        values.put(BicycleEntry.COLUMN_SUPPLIER, supplierString);

        //if this is a new bicycle, insert into database
        //notify the user of the success/failure of insertion
        if (mCurrentBicycleUri == null && validData) {
            Uri newBicycleUri = getContentResolver().insert(BicycleContract.CONTENT_URI, values);

            if (newBicycleUri == null) {
                Toast.makeText(this, getString(R.string.error_inserting),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insertion_complete),
                        Toast.LENGTH_SHORT).show();
            }
            //if this is an existing bicycle, update the database
            //notify the user of the success/failure of update
        } else if (mCurrentBicycleUri != null && validData) {
            int updatedRows = getContentResolver().update(mCurrentBicycleUri, values, null, null);
            if (updatedRows == 0) {
                Toast.makeText(this, getString(R.string.error_updating),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_complete),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //set up a method to DELETE a bicycle from the database
    private void deleteBicycle() {
        //can only delete an existing bicycle, so exit the activity otherwise
        if (mCurrentBicycleUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBicycleUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_complete),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    //use CursorLoader to QUERY the database, needed when editing an existing bicycle
    //first get the details of that particular bicycle
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BicycleEntry.COLUMN_IMAGE,
                BicycleEntry.COLUMN_BIKE_MODEL,
                BicycleEntry.COLUMN_BIKE_TYPE,
                BicycleEntry.COLUMN_PRICE,
                BicycleEntry.COLUMN_QUANTITY,
                BicycleEntry.COLUMN_SUPPLIER};

        return new CursorLoader(this,
                mCurrentBicycleUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        //extract data from first (and only) row of cursor
        if (data.moveToFirst()) {
            int pictureColumnIndex = data.getColumnIndexOrThrow(BicycleEntry.COLUMN_IMAGE);
            int modelColumnIndex = data.getColumnIndex(BicycleEntry.COLUMN_BIKE_MODEL);
            int typeColumnIndex = data.getColumnIndex(BicycleEntry.COLUMN_BIKE_TYPE);
            int quantityColumnIndex = data.getColumnIndex(BicycleEntry.COLUMN_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(BicycleEntry.COLUMN_PRICE);
            int supplierColumnIndex = data.getColumnIndex(BicycleEntry.COLUMN_SUPPLIER);

            String bicyclePicture = data.getString(pictureColumnIndex);
            String bikeModel = data.getString(modelColumnIndex);
            int bikeType = data.getInt(typeColumnIndex);
            mQuantity = data.getInt(quantityColumnIndex);
            String bikePrice = data.getString(priceColumnIndex);
            String bikeSupplier = data.getString(supplierColumnIndex);

            // Convert the picture to uri
            if (bicyclePicture != null) {
                mImageUri = Uri.parse(bicyclePicture);
            } else {
                mImageUri = null;
            }

            // Check if the image uri is null
            if (mImageUri != null) {
                // Show the image on the imageButton
                mImageView.setImageURI(mImageUri);
            } else {
                // Use the placeholder image instead
                mImageView.setImageDrawable(getDrawable(R.drawable.bicycle_shadow));
            }

            mModelEditText.setText(bikeModel);
            mPriceEditText.setText(bikePrice);
            mQuantityTextView.setText(String.valueOf(mQuantity));
            mSupplierEditText.setText(bikeSupplier);
            //set up a switch statement for type, that will display the right spinner selection
            switch (bikeType) {
                case BicycleEntry.TYPE_TOURING:
                    mTypeSpinner.setSelection(0);
                    break;
                case BicycleEntry.TYPE_ROAD:
                    mTypeSpinner.setSelection(1);
                    break;
                case BicycleEntry.TYPE_TRIATHLON:
                    mTypeSpinner.setSelection(2);
                    break;
                case BicycleEntry.TYPE_MOUNTAIN:
                    mTypeSpinner.setSelection(3);
                    break;
                case BicycleEntry.TYPE_HYBRID:
                    mTypeSpinner.setSelection(4);
                    break;
                default:
                    mTypeSpinner.setSelection(5);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //reset fields if loader is invalidated
        mModelEditText.setText("");
        mTypeSpinner.setSelection(5);
        mPriceEditText.setText("");
        mQuantityTextView.setText(String.valueOf(0));
        mSupplierEditText.setText("");
    }

    //dialogue that requires user to confirm the deletion of a bicycle
    private void deletionDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_confirmation);
        //if confirmed, delete the Bicycle
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBicycle();
            }
        });
        //if cancelled, dismiss the dialogue and return to edit screen
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //set method for ordering supplier to order more stock
    public void emailSupplier(View v) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"example@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Bicycle Order");
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        }
    }
}

