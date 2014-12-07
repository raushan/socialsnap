package com.cmsc436.socialsnap;

import android.R.integer;
import android.app.Activity;
import android.database.CursorJoiner.Result;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse; 
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;


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
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://cmsc436socialsnapapp.appspot.com");
		Double latitude = mLocation.getLatitude();
		Double longitude = mLocation.getLongitude();
		int statuscode = -99;
		try{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("latitude",latitude.toString() ));
			nameValuePairs.add(new BasicNameValuePair("longitude", longitude.toString()));
			nameValuePairs.add(new BasicNameValuePair("image_url",mImgurUrl ));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			statuscode = response.getStatusLine().getStatusCode();
			

	    } catch (IOException e) {
	       return statuscode;
	    }
		
		return statuscode;
	}

	@Override
	protected void onPostExecute(Integer output) {
		// Display the results of the lookup.
		//
		
		if(output == 200){
			Toast.makeText(mActivity.getApplicationContext(), "Upload Sucessful", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(mActivity.getApplicationContext(), "Upload Failed", Toast.LENGTH_LONG).show();
		}
	}

	public class DatabaseRetrieveTask extends AsyncTask<Void, Void, JSONArray>{
		int statuscode = -99;
		String result = "";
		
		private Location mLocation;


		public DatabaseRetrieveTask(Location location) {
			this.mLocation = location;
		}
		
		@Override
		protected JSONArray doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://cmsc436socialsnapapp.appspot.com");
			Double latitude = mLocation.getLatitude();
			Double longitude = mLocation.getLongitude();
			try{
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("latitude",latitude.toString() ));
				nameValuePairs.add(new BasicNameValuePair("longitude", longitude.toString()));
				nameValuePairs.add(new BasicNameValuePair("source","script" ));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				statuscode = response.getStatusLine().getStatusCode();
				
				HttpEntity entity = response.getEntity();
				InputStream instream = entity.getContent();
				result = getStringFromStream(instream);
				

		    } catch (IOException e) {
		      return null;
		    }
			
			try {
				return new JSONArray(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(mActivity.getApplicationContext(), "Error retrieving images", Toast.LENGTH_LONG).show();
			}
			
			return new JSONArray();
			
		}
		
		@Override
		protected void onPostExecute(JSONArray output) {
			
			
		}
	
		private String getStringFromStream(InputStream s){
			BufferedReader reader = new BufferedReader(new InputStreamReader(s));
	        StringBuilder out = new StringBuilder();
	        String line;
	        try {
				while ((line = reader.readLine()) != null) {
				    out.append(line);
				}
		        reader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return out.toString();
		}
		
	}
	
	
}

