package edu.sjsu.cinequest.android;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.WebConnection;

// TODO: Identical to JavaSEWebConnection
public class AndroidWebConnection extends WebConnection {
	private String url;
	private Hashtable postData;
    private HttpResponse response;
    
    
    public AndroidWebConnection(String url) throws IOException
    {
        Platform.getInstance().log("Opening connection to " + url);
        this.url = url;
    }
    
    
    public void setPostParameters(Hashtable postData) throws IOException {
    	this.postData = postData;
    }
    
    private void execute() throws IOException
    {
    	if (response != null) return;
        HttpClient client = new DefaultHttpClient();
    	if (postData == null) 
    	{
            HttpGet request = new HttpGet(url);
            response = client.execute(request);    		
    	}
    	else
    	{
    		HttpPost request = new HttpPost(url);
            Enumeration keys = postData.keys();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            while (keys.hasMoreElements()) 
            {            	            	
               String key = keys.nextElement().toString();
               String value = postData.get(key).toString();
               nameValuePairs.add(new BasicNameValuePair(key, value));  
            }         
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));                 
            response = client.execute(request);    		            
    	}
    }
        
    public InputStream getInputStream() throws IOException
    {
    	execute();
        return new BufferedInputStream(response.getEntity().getContent());
    }
    
    public String getHeaderField(String name) throws IOException
    {   
    	execute();
    	return response.getFirstHeader(name).getValue();
    }

    public void close() throws IOException
    {
        Platform.getInstance().log("Closing connection");
    }
}