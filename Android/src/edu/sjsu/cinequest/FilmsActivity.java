package edu.sjsu.cinequest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FilmsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Films tab");
        setContentView(textview);
    }
}
