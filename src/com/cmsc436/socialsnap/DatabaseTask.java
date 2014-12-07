package com.cmsc436.socialsnap;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class DatabaseTask extends AsyncTask<Void, Void, Integer> {
    
 
    private Activity mActivity;
    private Location mLocation;
    private String mImgurUrl;
    
    public DatabaseTask(String imgurURL, Location location, Activity activity) {
        this.mActivity = activity;
        this.mLocation = location;
        this.mImgurUrl = imgurURL;
    }
    
    @Override
    protected Integer doInBackground(Void... params) {
    	HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL("http://cmsc436socialsnapapp.appspot.com").openConnection();
			conn.setDoOutput(true);
			Double latitude = mLocation.getLatitude();
			Double longitude = mLocation.getLongitude();
			
			StringBuilder postParams = new StringBuilder();
            postParams.append("&");
            postParams.append(URLEncoder.encode("image_url", "UTF-8"));
            postParams.append("=");
            postParams.append(URLEncoder.encode(mImgurUrl, "UTF-8"));
            
            postParams.append("&");
            postParams.append(URLEncoder.encode("latitude", "UTF-8"));
            postParams.append("=");
            postParams.append(URLEncoder.encode(latitude.toString(), "UTF-8"));
            
            postParams.append("&");
            postParams.append(URLEncoder.encode("longitude", "UTF-8"));
            postParams.append("=");
            postParams.append(URLEncoder.encode(longitude.toString(), "UTF-8"));
            conn.setRequestProperty("Content-Length", "" + postParams.length());
            
            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(postParams.toString());
            out.flush();
            out.close();

		} catch (MalformedURLException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
		
      try {
		return conn.getResponseCode();
	} catch (IOException e) {
		//  Auto-generated catch block
		e.printStackTrace();
	}
      	//failure
        return -99;
    }

    @Override
	protected void onPostExecute(Integer output) {
		// Display the results of the lookup.
    	//
    	if(output == 200){
    		Toast.makeText(mActivity.getApplicationContext(), "Data image metadata added to database", Toast.LENGTH_SHORT).show();
    	}else{
    		Toast.makeText(mActivity.getApplicationContext(), "Failed to add image metadata to database", Toast.LENGTH_SHORT).show();
    		
    	}
	}
  
   
    
}