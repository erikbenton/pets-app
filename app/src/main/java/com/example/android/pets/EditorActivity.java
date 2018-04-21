/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetProvider;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    // ID for the loader
    private static final int PET_EDIT_LOADER = 1;

    // Boolean for keeping track of if the user has made any edits
    private boolean mPetHasChanged = false;

    /** Pet Cursor Adapter */
    private CursorAdapter mPetCursorAdapter;

    private Uri mContentPetUri;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() and getData() to get the associated URI
        Intent sentIntent = getIntent();

        mContentPetUri = sentIntent.getData();


        // Set the title of the EditorActivity on which situation we have
        // If the EditorActivity was opened using the ListView item, then we will
        // have URI of pet to change app bar to say "Edit Pet"
        // Otherwise if this is a new pet, URI is null so change app to say "Add Pet"
        if(mContentPetUri == null)
        {
            // Clicking on the FAB for adding a pet
            setTitle(R.string.editor_activity_title_new_pet);
        }
        else
        {
            // Clicking on a specific pet
            setTitle(R.string.editor_activity_title_edit_pet);
            // Init the loader
            getLoaderManager().initLoader(PET_EDIT_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener((mTouchListener));
        mBreedEditText.setOnTouchListener((mTouchListener));
        mWeightEditText.setOnTouchListener((mTouchListener));
        mGenderSpinner.setOnTouchListener((mTouchListener));

        setupSpinner();

    }

    /**
     * OnTouchListener that listens for if any edits have been made to the
     * Input fields of the editor so that the user can be warned before
     * leaving editor if their changes have been saved
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            mPetHasChanged = true;
            return true;
        }
    };

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener)
    {
        // Create an alert dialog builder and set the messsage and click listeners for the
        //  positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener()
        {
           public void onClick(DialogInterface dialog, int id)
           {
               // User clicked the "Keep Editing" button, so dismiss the dialog
               // and continue editing the pet
               if(dialog != null)
               {
                   dialog.dismiss();
               }
           }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * If the back button has been pressed
     */
    @Override
    public void onBackPressed()
    {
        // If there haven't been any changes
        if(!mPetHasChanged)
        {
            super.onBackPressed();
            return;
        }

        // Otherwise if there an unsaved changes, setup a dialog to warn the user
        // Create a ClickListener to handle the user confirming that the changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // User clicked the discard button, close the current activity
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * method for saving a pet in the database
     */
    private void savePet()
    {
        // Getting Pet Values
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();


        // Creating an entry for the database
        ContentValues values = createEntry(nameString, breedString, mGender, Integer.parseInt(weightString));

        int rowsAffected = 0;

        if(!TextUtils.isEmpty(nameString) || !TextUtils.isEmpty(breedString) || !TextUtils.isEmpty(weightString))
        {
            if (mContentPetUri == null)
            {
                // Insert the entry
                Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            }
            else
            {
                rowsAffected = getContentResolver().update(mContentPetUri, values, null, null);
            }
        }
        // Show a toast message depending on whether or not the insertion was successful
        if (rowsAffected == 0)
        {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates entry for the database
     * @param name - Name of the pet
     * @param breed - Breed of the pet
     * @param gender - Gender (0 - Unknown, 1 - Male, 2 - Female)
     * @param weight - Weight (in kg) of the pet
     * @return ContentValue for inserting into the database
     */
    private ContentValues createEntry(String name, String breed, int gender, int weight)
    {
        // Creating pet
        ContentValues petValues = new ContentValues();
        petValues.put(PetEntry.COLUMN_PET_NAME, name);
        petValues.put(PetEntry.COLUMN_PET_BREED, breed);
        petValues.put(PetEntry.COLUMN_PET_GENDER, gender);
        petValues.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        return petValues;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert the pet into the database
                savePet();

                // Go back to Catalog
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If there haven't been any changes to the pet
                if(!mPetHasChanged)
                {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise there are unsaved changes
                // Need to create a ClickListener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // User clicked the discard button, navigate to the parent activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user they unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };

        return new CursorLoader(this, mContentPetUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor)
    {
        // Move to first position in cursor
        cursor.moveToFirst();

        // Get the attributes of the pet to fill View with
        String petName = cursor.getString((cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
        String petBreed = cursor.getString((cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)));
        String petWeight = cursor.getString((cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)));
        int petGender = cursor.getInt((cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)));

        // Give the views the appropriate values
        mNameEditText.setText(petName, TextView.BufferType.EDITABLE);
        mBreedEditText.setText(petBreed, TextView.BufferType.EDITABLE);
        mWeightEditText.setText(petWeight, TextView.BufferType.EDITABLE);
        mGenderSpinner.setSelection(petGender);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader)
    {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}