package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider
{
    // Constants for URI matcher
    private static final int PETS = 100;
    private static final int PET_ID = 101;

    // Creating Uri matcher
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        // Creating Uri patterns for given paths
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    // Database helper object
    private PetDbHelper mDbHelper;

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Make cursor for holding data
        Cursor cursor;

        // Get the Uri match
        int match = sUriMatcher.match(uri);

        // Determine what to do with the Uri
        switch (match)
        {
            case PETS:
                // Perform query to get all the rows from the database for our Cursor
                cursor = db.query(PetEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // Setup the selection
                selection = PetEntry._ID + "=?";

                // Get the pet id from the Uri
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Make the query with the given pet id
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match)
        {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database
     */
    private Uri insertPet(Uri uri, ContentValues values)
    {

        // Data validation checks
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        Integer weight  = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);

        // Check the name
        if(name == null)
        {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // Check the gender
        if(gender == null || !PetEntry.isValidGender(gender))
        {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // Check the weight
        if(weight != null && weight < 0)
        {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // Get the writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Get the id of the entry
        long id = db.insert(PetEntry.TABLE_NAME, null, values);

        // Insert data into table
        if(id == -1)
        {
            Log.v("INSERTING PET INFO", "Unable to insert " + values.get(PetEntry.COLUMN_PET_NAME).toString());
            return null;
        }
        else
        {
            Log.v("CatalogActivity", "New row ID: " + id);
        }
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
}