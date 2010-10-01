package edu.sjsu.cinequest.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;

import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.MessageDigest;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.WebConnection;

// Must be created on UI thread
public class AndroidPlatform extends Platform {
	private Handler handler;

	public AndroidPlatform() {
		handler = new Handler();
	}

	public WebConnection createWebConnection(String url) throws IOException {
		return new AndroidWebConnection(url);
	}

	@Override
	public Object convert(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLocalImage(String imageName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parse(String url, DefaultHandler handler, Callback callback)
			throws SAXException, IOException {
		// TODO Same as Java SE
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new SAXException(e.toString());
		}
		InputStream in = new URL(url).openStream();
		sp.parse(in, handler);
	}

	@Override
	public void parse(String url, Hashtable postData, DefaultHandler handler,
			Callback callback) throws SAXException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void invoke(final Callback callback, final Object arg) {
		if (callback == null)
			return;
		handler.post(new Runnable() {
			public void run() {
				try {
					callback.invoke(arg);
				} catch (Throwable t) {
					log(t.getMessage());
					// TODO: Show to user
					// Ui.getUiEngine().pushScreen(new
					// ErrorScreen(t.getMessage()));
				}
			}
		});
	}

	@Override
	public void failure(final Callback callback, final Throwable arg) {
		if (callback == null)
			return;
		handler.post(new Runnable() {
			public void run() {
				callback.failure(arg);
			}
		});
	}

	@Override
	public void progress(final Callback callback, final Object arg) {
		// TODO Auto-generated method stub
		if (callback == null)
			return;
		handler.post(new Runnable() {
			public void run() {
				callback.progress(arg);
			}
		});
	}

	@Override
	public void storePersistentObject(long key, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object loadPersistentObject(long key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageDigest getMessageDigestInstance(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector sort(Vector vec, Comparator comp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
}
