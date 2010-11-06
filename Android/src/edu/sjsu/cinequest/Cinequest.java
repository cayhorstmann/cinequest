package edu.sjsu.cinequest;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import edu.sjsu.cinequest.android.AndroidPlatform;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.QueryManager;
public class Cinequest extends Activity {
	private QueryManager queryManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Platform.setInstance(new AndroidPlatform(getApplicationContext()));
        
        final TextView tv = new TextView(this);
        
        queryManager.getFestivalDates(new Callback() {
			public void progress(Object value) {
				tv.setText(tv.getText() + ".");
			}
			
			public void invoke(Object result) {
				String[] dates = (String[]) result;
				tv.setText(Arrays.toString(dates));
			}
			
			public void failure(Throwable t) {
				tv.setText(t.toString() + " :-(");
			}
		});
        
        tv.setText("Hello, Android");
        setContentView(tv);
    }
}
