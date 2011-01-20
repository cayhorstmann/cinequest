package edu.sjsu.cinequest;

import android.app.ProgressDialog;
import android.content.Context;
import edu.sjsu.cinequest.comm.Callback;

public class ProgressMonitorCallback implements Callback {
   private ProgressDialog dialog;
	
   public ProgressMonitorCallback(Context context) {
	   this(context, "Fetching Data");
   }

   public ProgressMonitorCallback(Context context, String message) {
	   dialog = ProgressDialog.show(context, "Cinequest", message);
   }
	
	@Override
	public void invoke(Object result) {
		dialog.dismiss();
	}

	@Override
	public void progress(Object value) {
	}

	@Override
	public void failure(Throwable t) {
		dialog.dismiss();		
		// TODO: Notify user of failure
	}
}
