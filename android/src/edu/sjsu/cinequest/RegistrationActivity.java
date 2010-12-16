package edu.sjsu.cinequest;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class RegistrationActivity extends Activity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Get the progress bar
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_PROGRESS);	//use this or above commented line to get progress bar
       
        this.setProgressBarVisibility(true);		//set progress bar visiblity to true
        
        setContentView(R.layout.registrationpage_layout);
 
        //get the webview
	   final WebView webview = (WebView) this.findViewById(R.id.webview);
	   //set JavaScript
	   webview.getSettings().setJavaScriptEnabled(true);
	   
	   /*Zoom Control on web (You don't need this if ROM supports Multi-Touch */
	   webview.getSettings().setSupportZoom(true);
	   
	   //Enable Multitouch if supported by ROM
	   webview.getSettings().setBuiltInZoomControls(true); 

	   //workaround so that the default browser doesn't take over on link clicks
	   webview.setWebViewClient(new MyWebViewClient());
	   
	   //Get the registration page URL from the bundle contained within Intent
	   Intent getIntent = getIntent();
       Bundle b = getIntent.getExtras();       
       String url = b.getString("url");
       
       
       final Activity activity = this;
       
       //Display the progress bar of webview using WebChromeClient
       webview.setWebChromeClient(new WebChromeClient(){
    	   		@Override
                public void onProgressChanged(WebView view, int progress) {
                        activity.setTitle("Loading...");
                        activity.setProgress(progress * 100); 
                        //Reset the app title after page loaded
                        if(progress == 100)
                             activity.setTitle(R.string.app_name);
                }
       });
       
       //Load the URL
       webview.loadUrl(url);       
    }
    
    /**
     * Override onConfigurationChanged, so that webview does not reload the page
     * once the screen orientation changes. 
     * Also added android:configChanges="orientation" in manifest file's activity tag.
     * This is cause android to call onConfigurationChanged on orientation change, instead of
     * restarting the activity
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
    }
	   
    /**
     * Private WebViewClient class for webview
     */
	private class MyWebViewClient extends WebViewClient {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return true;
	        }
	}
}
