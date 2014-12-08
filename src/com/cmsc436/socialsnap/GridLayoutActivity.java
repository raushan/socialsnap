package com.cmsc436.socialsnap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.socialsnap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class GridLayoutActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener {

	protected static final String EXTRA_RES_ID = "POS";

	private static final int CAMERA_REQUEST_CODE = 100;
	private GoogleApiClient mGoogleApiClient;
	private GoogleMap mMap;
	private File photoFile = null;
	private String mCurrentPhotoPath;
	private Uri photoUri;
	private JSONArray json;
	private Context mContext;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



	private ArrayList<Bitmap> mPhotos = new ArrayList<Bitmap>();
	private ArrayList<String> mPhotoLinks = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		setContentView(R.layout.gallery);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF29A6CF));

		setUpMapIfNeeded();
		setUpGoogleApiClientIfNeeded();
		mGoogleApiClient.connect();

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Pass photo Uri to upload activity
			Intent uploadIntent = new Intent(GridLayoutActivity.this,
					UploadUI.class);
			uploadIntent.putExtra("photoUri", photoUri);
			Log.i("GridLayoutActivity on result", "Starting upload activity");
			startActivity(uploadIntent);
		}
	}

	/* ============ GOOGLE MAP ============= */

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera. In this case, we just add a marker near Africa.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.setOnMyLocationButtonClickListener(this);
	}

	private void setUpGoogleApiClientIfNeeded() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API).addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpGoogleApiClientIfNeeded();
		mGoogleApiClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		Double lat = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient).getLatitude();
		Double lng = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient).getLongitude();

		String place = "Marker";

		Geocoder gcd = new Geocoder(getApplicationContext(),
				Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = gcd.getFromLocation(lat, lng, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (addresses.size() > 0) {
			place = addresses.get(0).getLocality();
		}

		mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
				.title(place));
		return false;
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, REQUEST, this);

		Double lat = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient).getLatitude();
		Double lng = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient).getLongitude();
		// Fill arraylist with images from database
		(new DatabaseRetrieveTask(lat, lng)).execute();

		LatLng a = new LatLng(lat, lng);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(a, 15));
	}

	@Override
	public void onLocationChanged(Location location) {
		mMap.clear();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnectionSuspended(int cause) {

	}

	/* ============ ACTION BAR ============= */

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_camera) {
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			if (cameraIntent.resolveActivity(getPackageManager()) != null) {
				try {
					photoFile = createImageFile();
				} catch (IOException e) {
					Toast.makeText(GridLayoutActivity.this,
							"Unable to write image", Toast.LENGTH_LONG).show();
				}
				if (photoFile != null) {
					photoUri = Uri.fromFile(photoFile);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
					startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

				}
			}
			return true;
		}

		if (id == R.id.refresh) {

			/* ADD REFRESH STUFF */

			makeToast("Refreshes images by grabbing updates from database");
			return true;
		}

		if (id == R.id.info) {
			showInfoAlert();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/* ========== HELPER METHODS ========== */

	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("Location mode isn't supported");

		// Setting Dialog Message
		alertDialog
				.setMessage("Uploading a photo is only available in these location modes:\n- High accuracy \n- Device only");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		// Showing Alert Message
		alertDialog.show();
	}

	public void showInfoAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.info, null);

		builder.setTitle("App Information");
		builder.setView(view);

		builder.setPositiveButton("Close",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";

		// File storageDir = getApplicationContext().getCacheDir();
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		Log.i("CREATE IMAGE FILE", "Entering temp file");
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);
		Log.i("CREATE IMAGE FILE", "Exited temp file");

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		Log.i("CREATE IMAGE FILE", "File ==== " + mCurrentPhotoPath);

		return image;
	}

	private void makeToast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	private class DatabaseRetrieveTask extends AsyncTask<Void, Void, JSONArray> {
		int statuscode = -99;
		String result = "";

		private double latitude;
		private double longitude;

		public DatabaseRetrieveTask(double lat, double lng) {
			this.latitude = lat;
			this.longitude = lng;
		}

		@Override
		protected JSONArray doInBackground(Void... params) {
			Log.i("Database Retrieve", "Entered doInBackground retrieve");

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://cmsc436socialsnapapp.appspot.com");

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("latitude", String
						.valueOf(latitude)));
				nameValuePairs.add(new BasicNameValuePair("longitude", String
						.valueOf(longitude)));
				nameValuePairs.add(new BasicNameValuePair("source", "script"));
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
				Log.i("Database Retrieve", "Trying to return result");

				return new JSONArray(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplicationContext(),
						"Error retrieving images", Toast.LENGTH_LONG).show();
			}
			Log.i("Database Retrieve", "Json = empty");

			return new JSONArray();

		}

		@Override
		protected void onPostExecute(JSONArray output) {
			if (output.length() != 0) {
				Log.i("Database Retrieve", "Json = output");

				json = output;
				JSONObject mJsonObject = new JSONObject();
				for (int i = 0; i < json.length(); i++) {
					try {
						mJsonObject = json.getJSONObject(i);
						Log.i("GridLayoutActivity JSONObject", "Json object : "
								+ mJsonObject.toString());
						mJsonObject = mJsonObject.getJSONObject("fields");
						String url = mJsonObject.getString("image_url");
						String jpgUrl = "http://i." + url.substring(7) + ".jpg";
						mPhotoLinks.add(jpgUrl);
						// Use url to get image from imgur
						Log.i("GridLayoutActivity JSONObject",
								"Json image url : " + url);
					} catch (JSONException e) {
						Toast.makeText(getApplicationContext(),
								"Error retrieving images", Toast.LENGTH_LONG)
								.show();
					}
				}

				(new ImgurViewTask()).execute();
			} else {
				Log.i("Database Retrieve", "Json length = 0");
			}
		}

		private String getStringFromStream(InputStream s) {
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

	private class ImgurViewTask extends
			AsyncTask<Void, Void, ArrayList<Bitmap>> {
		ProgressDialog pdLoading = new ProgressDialog(GridLayoutActivity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pdLoading.setMessage("Loading Images...");
			pdLoading.show();
		}

		@Override
		protected ArrayList<Bitmap> doInBackground(Void... params) {
			InputStream is;
			for (String stringurl : mPhotoLinks) {

				Log.i("ImgurViewTask", "Getting image from :" + stringurl);

				try {
					Bitmap bitmap = BitmapFactory
							.decodeStream((InputStream) new URL(stringurl)
									.getContent());
					mPhotos.add(bitmap);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return mPhotos;

		}

		@Override
		protected void onPostExecute(ArrayList<Bitmap> photos) {
	        pdLoading.dismiss();

			GridView gridview = (GridView) findViewById(R.id.gridview);

			// Create a new ImageAdapter and set it as the Adapter for this
			// GridView
			gridview.setAdapter(new ImageAdapter(mContext, photos));

			// Set an setOnItemClickListener on the GridView
			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {

					// Create an Intent to start the ImageViewActivity
					Intent intent = new Intent(GridLayoutActivity.this,
							ImageViewActivity.class);

					// Add the ID of the thumbnail to display as an Intent Extra
					intent.putExtra(EXTRA_RES_ID, (int) id);

					// Start the ImageViewActivity
					startActivity(intent);
				}
			});

		}

	}

}