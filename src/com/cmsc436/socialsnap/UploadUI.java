package com.cmsc436.socialsnap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import com.example.socialsnap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class UploadUI extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {
	protected LocationListener locatioinListener;
	private static final int CAMERA_REQUEST_CODE = 100;
	private Bitmap photoBitmap;
	private Uri photoUri;
	private String title = "SocialSnap:";
	private String comment;
	private TextView charCount;
	private TextView textView;
	LocationClient mLocationClient;
	Location mCurrentLocation;
	LocationRequest mLocationRequest;
	private MyImgurUploadTask mImgurUploadTask;
	private String mImgurUrl;
	ImageView photoView;
	private File photoFile = null;
	private String mCurrentPhotoPath;
	EditText editText;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF29A6CF));
		
		textView = (TextView) findViewById(R.id.location);

		Intent photoIntent = getIntent();

		// Retrieve photo image for UI display
		photoUri = (Uri) photoIntent.getParcelableExtra("photoUri");
		try {
			photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		photoView = (ImageView) findViewById(R.id.photoView);
		//photoView.setScaleType(ScaleType.FIT_XY);
		photoView.setImageBitmap(photoBitmap);

		// Obtain comment from user input
		editText = (EditText) findViewById(R.id.comment);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// this will show characters remaining
				if (charCount != null) {
					charCount.setText(Integer.toString(80 - s.toString()
							.length()));
				}
			}
		});

		// Get Location from GPS

		// Create LocationClient
		mLocationClient = new LocationClient(this, this, this);
		// Create & set LocationRequest for Location update
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(1000 * 5);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(1000 * 1);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
	}

	// GooglePlayServicesClient.OnConnectionFailedListener
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
	}

	// GooglePlayServicesClient.ConnectionCallbacks
	@Override
	public void onConnected(Bundle arg0) {

		if (mLocationClient != null)
			mLocationClient.requestLocationUpdates(mLocationRequest, this);

		if (mLocationClient != null) {
			// Get location
			mCurrentLocation = mLocationClient.getLastLocation();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
					&& Geocoder.isPresent()) {
				(new GetAddressTask(this)).execute(mCurrentLocation);
				ImageView locIcon = (ImageView) findViewById(R.id.icon);
				locIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_location_foundcopy));
			}

		}

	}

	@Override
	public void onDisconnected() {
	}

	// LocationListener
	@Override
	public void onLocationChanged(Location location) {
		mCurrentLocation = mLocationClient.getLastLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upload, menu);
		MenuItem item = menu.findItem(R.id.char_counter);
		charCount = (TextView) item.getActionView();
		charCount.setText("80");
		charCount.setTextColor(0xFFFFFFFF);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_upload) {
			comment = editText.getText().toString();

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

			// Setting Dialog Message
			alertDialog.setMessage("Are you sure you want to upload?");

			// On pressing Settings button
			alertDialog.setPositiveButton("Upload",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							(new MyImgurUploadTask(photoUri, title, comment))
									.execute();
						}
					});

			// On pressing cancel button
			alertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			// Showing Alert Message
			alertDialog.show();
			return true;
		}
		if (id == R.id.action_camera) {
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			
			
			if (cameraIntent.resolveActivity(getPackageManager()) != null) {
				try {
					photoFile = createImageFile();
				} catch (IOException e) {
					Toast.makeText(UploadUI.this,
							"Unable to write image", Toast.LENGTH_LONG)
							.show();
				}
				if (photoFile != null) {
					photoUri = Uri.fromFile(photoFile);
					cameraIntent
							.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
					startActivityForResult(cameraIntent,
							CAMERA_REQUEST_CODE);

				}
			}
			return true;
		}
		
		if (id == R.id.info) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			LayoutInflater inflater= LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.info, null);

			builder.setTitle("App Information");
			builder.setView(view);
			
	        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	dialog.cancel();
	            }
	        });

	        AlertDialog dialog = builder.create();
	        dialog.show();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Obtain photo using Uri
			try {
				photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			//photoView.setScaleType(ScaleType.FIT_XY);
			photoView.setImageBitmap(photoBitmap);

		}
	}

	// AsyncTask to retrieve address
	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected String doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			Location loc = params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				/*
				 * Return 1 address.
				 */
				Double lat = loc.getLatitude();
				Double lon = loc.getLongitude();
				addresses = geocoder.getFromLocation(lat, lon, 1);
			} catch (IOException e1) {
				Log.e("LocationSampleActivity",
						"IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("Network unavailable");
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments "
						+ Double.toString(loc.getLatitude()) + " , "
						+ Double.toString(loc.getLongitude())
						+ " passed to address service";
				Log.e("LocationSampleActivity", errorString);
				e2.printStackTrace();
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				/*
				 * Format the first line of address (if available), city, and
				 * country name.
				 */
				String addressText = String.format(
						"%s, %s, %s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",
						// Locality is usually a city
						address.getLocality(),
						// The country of the address
						address.getCountryName());
				// Return the text
				return addressText;
			} else {
				return "No address found";
			}
		}

		@Override
		protected void onPostExecute(String address) {
			// Display the results of the lookup.
			textView.setText(address);
		}

	}

	// Extends ImgurUploadTask to upload photo to Imgur
	private class MyImgurUploadTask extends ImgurUploadTask {
		public MyImgurUploadTask(Uri imageUri, String title, String comment) {
			super(imageUri, title, comment, UploadUI.this);
		}

		// Add imageUrl to our database at this point
		protected void onPostExecute(String imageId) {
			super.onPostExecute(imageId);
			mImgurUploadTask = null;

			if (imageId != null) {
				mImgurUrl = "http://imgur.com/" + imageId;
				Log.i("Post execute", "ImageURL : "+mImgurUrl+", Lat : "+ mCurrentLocation.getLatitude()+ ", Long : "+mCurrentLocation.getLongitude());
				(new DatabaseTask(mImgurUrl, mCurrentLocation, UploadUI.this)).execute();
				
			} else {
				mImgurUrl = null;
				Toast.makeText(UploadUI.this, "Failed to upload picture",
						Toast.LENGTH_LONG).show();
			}
		}
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

}
