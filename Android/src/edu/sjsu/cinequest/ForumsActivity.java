package edu.sjsu.cinequest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ForumsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Forums tab");
        setContentView(textview);
    }
}
