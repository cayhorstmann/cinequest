package edu.sjsu.cinequest.android;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.WebConnection;

// TODO: Identical to JavaSEWebConnection
public class AndroidWebConnection extends WebConnection {
    private HttpURLConnection connection;
    
    public AndroidWebConnection(String url) throws IOException
    {
        Platform.getInstance().log("Opening connection to " + url);
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setUseCaches(false);        
        connection.setRequestProperty("connection", "close"); // http://stackoverflow.com/questions/3352424/httpurlconnection-openconnection-fails-second-time 
    }
    
    public OutputStream getOutputStream() throws IOException
    {
       connection.setDoOutput(true);
       return connection.getOutputStream();
    }
    
    public InputStream getInputStream() throws IOException
    {
    	InputStream in = connection.getInputStream(); 
    	Platform.getInstance().log(in.getClass() + " " + (in instanceof BufferedInputStream));
        return in;
    }
    
    public String getHeaderField(String name) throws IOException
    {   
        return connection.getHeaderField(name);
    }

    public void close() throws IOException
    {
        connection.disconnect();
        connection = null;
        Platform.getInstance().log("Closing connection");
    }
}
