package edu.sjsu.cinequest.android;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.WebConnection;

// TODO: Identical to JavaSEWebConnection
public class AndroidWebConnection extends WebConnection {
    private HttpURLConnection connection;
    HttpGet request;
    
    public AndroidWebConnection(String url) throws IOException
    {
        Platform.getInstance().log("Opening connection to " + url);
        /*
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setUseCaches(false);        
    	System.setProperty("http.keepAlive", "false");
        connection.setRequestProperty("connection", "close"); // http://stackoverflow.com/questions/3352424/httpurlconnection-openconnection-fails-second-time
        */
        request = new HttpGet();
        try {
        request.setURI(new URI(url)); 
        }
        catch (URISyntaxException ex) {
        	Platform.getInstance().log(ex.getMessage());
        }
    }
    
    public OutputStream getOutputStream() throws IOException
    {
       connection.setDoOutput(true);
       return connection.getOutputStream();
    }
    
    public InputStream getInputStream() throws IOException
    {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(request);
        return new BufferedInputStream(response.getEntity().getContent());
        
    	// return new BufferedInputStream(connection.getInputStream()); 
    }
    
    public String getHeaderField(String name) throws IOException
    {   return "";
        //return connection.getHeaderField(name);
    }

    public void close() throws IOException
    {
    	/*
        connection.disconnect();
        connection = null;
        */
        Platform.getInstance().log("Closing connection");
    }
}
