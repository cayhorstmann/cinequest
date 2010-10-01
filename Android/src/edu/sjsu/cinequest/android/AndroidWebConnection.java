package edu.sjsu.cinequest.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.sjsu.cinequest.comm.WebConnection;

// TODO: Identical to JavaSEWebConnection
public class AndroidWebConnection extends WebConnection {
    private HttpURLConnection connection;
    
    public AndroidWebConnection(String url) throws IOException
    {
        connection = (HttpURLConnection) new URL(url).openConnection();
    }
    
    public OutputStream getOutputStream() throws IOException
    {
       connection.setDoOutput(true);
       return connection.getOutputStream();
    }
    
    public InputStream getInputStream() throws IOException
    {
        return connection.getInputStream();
    }
    
    public String getHeaderField(String name) throws IOException
    {   
        return connection.getHeaderField(name);
    }

    public void close() throws IOException
    {
        connection.disconnect();
        connection = null;
    }
}
