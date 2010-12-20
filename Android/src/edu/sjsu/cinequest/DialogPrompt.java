package edu.sjsu.cinequest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogPrompt {
	
	/**
	 * Shows a general purpose dialog
	 * @param context the context which is requesting the prompt
	 * @param message the message to display
	 */
	public static void showDialog(Context context, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
		       .setCancelable(true)
		       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                return;    		                
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/**
	 * Shows a confirmation dialog with YES/NO options
	 * @param context the context which is requesting the prompt
	 * @param message the message to display
	 * @param pButton the text of positive button
	 * @param pListener the OnClickListener for positive button
	 * @param nButton the text of negative button
	 * @param nListener the OnClickListener for negative button
	 */
	public static boolean showOptionDialog(Context context, String message, 
									String pButton, DialogInterface.OnClickListener pListener,
									String nButton, DialogInterface.OnClickListener nListener){
		final Boolean result = false;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
		       .setCancelable(true)
		       .setPositiveButton(pButton, pListener)
		       .setNegativeButton(nButton, nListener);
		AlertDialog alert = builder.create();
		alert.show();
		
		return result;
	}
	
	/**
	 * Shows a confirmation dialog with YES/NO options
	 * @param context the context which is requesting the prompt
	 * @param message the message to display
	 * @param firstButton the text of first button
	 * @param firstListener the OnClickListener for first button
	 * @param secondButton the text of second button
	 * @param secondListener the OnClickListener for second button
	 * @param thirdButton the text of third button
	 * @param thirdListener the OnClickListener for third button
	 */
	public static boolean showOptionDialog(Context context, String message, 
			String firstButton, DialogInterface.OnClickListener firstListener,
			String secondButton, DialogInterface.OnClickListener secondListener,
			String thirdButton, DialogInterface.OnClickListener thirdListener){
		
		final Boolean result = false;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
		       .setCancelable(true)
		       .setPositiveButton(firstButton, firstListener)
		       .setNegativeButton(secondButton, secondListener)
		       .setNeutralButton(thirdButton, thirdListener);
		AlertDialog alert = builder.create();
		alert.show();
		
		return result;    		
	}

}
