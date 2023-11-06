package edu.msu.catherinemadhatter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

@SuppressWarnings("Convert2Lambda")
public class HatterActivity extends AppCompatActivity {

    private static final String PARAMETERS = "parameters";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        getHatterView().putToBundle(PARAMETERS, outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hatter);

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        /*
         * Set up the spinner
         */

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hats_spinner, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        getSpinner().setAdapter(adapter);

        getSpinner().setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view,
                                       int pos, long id) {
                getHatterView().setHat(pos);
                getColorButton().setEnabled(getHatterView().getHat() == 2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        /*
         * Restore any state
         */
        if(savedInstanceState != null) {
            getHatterView().getFromBundle(PARAMETERS, savedInstanceState);
        }

        updateUI();


    }

    /**
     * Ensure the user interface components match the current state
     */
    private void updateUI() {

        // Setting spinner component
        getSpinner().setSelection(getHatterView().getHat());

        // Setting feather component
        boolean featherState = getHatterView().getFeatherState();
        getHatterView().setFeatherState(featherState);
        getFeatherCheck().setChecked(featherState);

    }

    @SuppressWarnings("unused")
    public void onStartColorPicker(View view)
    {
        Intent intent = new Intent(this, ColorSelectActivity.class);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        pickColorResultLauncher.launch(intent);
    }

    /**
     * The hatter view object
     */
    private HatterView getHatterView() {
        return findViewById(R.id.hatterView);
    }

    /**
     * The color select button
     */
    private Button getColorButton() {
        return findViewById(R.id.buttonColor);
    }

    /**
     * The feather checkbox
     */
    private CheckBox getFeatherCheck() {
        return findViewById(R.id.checkFeather);
    }

    /**
     * The hat choice spinner
     */
    private Spinner getSpinner() {
        return findViewById(R.id.spinnerHat);
    }

    /**
     * Handle a Picture button press
     */
    @SuppressWarnings("unused")
    public void onPicture(View view) {
        // Get a picture from the gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        pickImageResultLauncher.launch(intent);

    }

    final ActivityResultLauncher<Intent> pickImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        getHatterView().setImageUri(uri);
                    }
                }
            });

    final ActivityResultLauncher<Intent> pickColorResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        int color = data.getIntExtra("color",0);
                        getHatterView().setColor(color);
                    }
                }
            });

    /**
     * Handle a feather check
     */
    @SuppressWarnings("unused")
    public void onFeather(View view) {
        getHatterView().handleFeather();

    }

}