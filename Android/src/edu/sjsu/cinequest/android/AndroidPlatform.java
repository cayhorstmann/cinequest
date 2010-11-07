package edu.sjsu.cinequest.android;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import edu.sjsu.cinequest.comm.Cache;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.MessageDigest;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.WebConnection;

// Must be created on UI thread
public class AndroidPlatform extends Platform {
	// echo -n "edu.sjsu.cinequest.rim.RIMPlatform" | md5sum | cut -c1-16
	private static final long PERSISTENCE_KEY = 0xcfbd786faca62011L;
	private Cache xmlRawBytesCache;
	private static final int MAX_CACHE_SIZE = 50;

	private Handler handler;
	private Context context;

	public AndroidPlatform(Context context) {
		handler = new Handler();
		this.context = context;
		
		xmlRawBytesCache = (Cache) loadPersistentObject(PERSISTENCE_KEY);
		if (xmlRawBytesCache == null)
		{
			xmlRawBytesCache = new Cache(MAX_CACHE_SIZE);
		}	
	}

	public WebConnection createWebConnection(String url) throws IOException {
		return new AndroidWebConnection(url);
	}

	@Override
	// TODO: Give better name to method
	// Returns an android.graphics.BitMap
	public Object convert(byte[] bytes) {		
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	@Override
	// Returns an android.graphics.BitMap
	public Object getLocalImage(String imageName) {
		// TODO Uncomment below and switch to higher version of API
		return new BitmapDrawable(/*context.getResources(), */ imageName).getBitmap();
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
		/*
		 * Android crap--search for http://www.google.com/search?q=android+XML+parser+8859-1
		 * The parser can't infer the character encoding from the xml encoding attribute, so 
		 * we have to hardwire 8859-1 here. 
		 */
		// Reader in = new InputStreamReader(new URL(url).openStream(), "iso-8859-1");
		WebConnection connection = null;
		try {
			connection = createWebConnection(url);
			byte[] xmlSource = (byte[]) connection.getBytes();
	        // Store the xml source
	        xmlRawBytesCache.put(url, xmlSource);
	        InputSource in = new InputSource(new InputStreamReader(
	              new ByteArrayInputStream(xmlSource), "ISO-8859-1"));
			sp.parse(in, handler);
		} 
		// Reading fails. Try to get XML from cache
        catch (IOException e)
        {
           byte[] bytes = (byte[]) xmlRawBytesCache.get(url);
           // XML exists in cache
           if (bytes != null)
           {
              InputSource in  = new InputSource(new InputStreamReader(
                 new ByteArrayInputStream(bytes), "ISO-8859-1"));
              sp.parse(in, handler);
              return;
           } else
              // XML not found on cache.
           throw e;
        }
    }

	@Override
	public void parse(String url, Hashtable postData, DefaultHandler handler,
			Callback callback) throws SAXException, IOException {
       SAXParserFactory spf = SAXParserFactory.newInstance();
       SAXParser sp;
       try
       {
           sp = spf.newSAXParser();
       } catch (ParserConfigurationException e)
       {
           throw new SAXException(e.toString());
       }
       WebConnection connection = createWebConnection(url);
       
       PrintWriter out = new PrintWriter(connection.getOutputStream());
       boolean first = true;
       Enumeration keys = postData.keys();
       while (keys.hasMoreElements()) 
       {
          if (first) first = false;
          else out.print('&');
          String key = keys.nextElement().toString();
          String value = postData.get(key).toString();
          out.print(key);
          out.print('=');
          out.print(URLEncoder.encode(value, "UTF-8"));               
       }         
       out.close();
       byte[] response = connection.getBytes();
       sp.parse(new ByteArrayInputStream(response), handler);
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
	// TODO: Add hint whether this is a cache or a truly persistent object
	public void storePersistentObject(long key, Object object) {
		try {
			OutputStream out = context.openFileOutput(key + ".ser", Context.MODE_PRIVATE);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(object);
			oout.close();
		} catch (Exception e) {
			log(e.getMessage());
		}
	}

	@Override
	public Object loadPersistentObject(long key) {
		try {
			InputStream in = context.openFileInput(key + ".ser");
			ObjectInputStream oin = new ObjectInputStream(in);
			Object ret = oin.readObject();
			oin.close();
			return ret;
		} catch (Exception e) {
			log(e.getMessage());
			return null;
		}
	}

	@Override
	public MessageDigest getMessageDigestInstance(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector sort(Vector vec, final Comparator comp) {
		Vector ret = new Vector(vec);
		Collections.sort(ret, new java.util.Comparator<Object>() {
		
			public int compare(Object a, Object b) {
				return comp.compare(a, b);
			}
		});
		return ret;
	}

	@Override
	public void log(String message) {
		Log.i("Cinequest", message);
	}

	@Override
	public void close() {
		storePersistentObject(PERSISTENCE_KEY, xmlRawBytesCache);
	}
}
