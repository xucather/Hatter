package edu.msu.catherinemadhatter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class ColorSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select);
    }

    @SuppressWarnings("unused")
    public void selectColor(int color) {
        // Get a picture from the gallery
        Intent intent = new Intent();
        intent.putExtra("color",color);
        setResult(RESULT_OK, intent);
        finish();
    }


}