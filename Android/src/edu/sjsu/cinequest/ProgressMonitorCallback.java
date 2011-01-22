package edu.sjsu.cinequest;

import android.app.ProgressDialog;
import android.content.Context;
import edu.sjsu.cinequest.comm.Callback;

public class ProgressMonitorCallback implements Callback {
   private ProgressDialog dialog;
   private Context context;
   private String message;
	
   public ProgressMonitorCallback(Context context) {
	   this(context, "Fetching Data");
   }

   public ProgressMonitorCallback(Context context, String message) {
	   this.context = context;
	   this.message = message;
   }

   @Override
	public void starting() {
	   if (dialog == null)
		   dialog = ProgressDialog.show(context, "Cinequest", message);
	}
   
	@Override
	public void invoke(Object result) {
		if (dialog != null)	dialog.dismiss();
		dialog = null;
	}

	@Override
	public void progress(Object value) {
	}

	@Override
	public void failure(Throwable t) {
		if (dialog != null) dialog.dismiss();
		dialog = null;
    	// TODO: For some classes of Throwable, just pop the dialog?
    	// E.g. user canceling login dialog
		
		DialogPrompt.showDialog(context, 
				"Application Error: " + t.getMessage());
	}
}
