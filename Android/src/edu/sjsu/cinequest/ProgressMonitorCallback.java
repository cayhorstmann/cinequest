package edu.sjsu.cinequest;

import android.app.ProgressDialog;
import android.content.Context;
import edu.sjsu.cinequest.comm.Callback;

public class ProgressMonitorCallback implements Callback {
   private ProgressDialog dialog;

   public ProgressMonitorCallback(Context context, String progressMessage) {
	   dialog = ProgressDialog.show(context, "Cinequest", progressMessage);
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
