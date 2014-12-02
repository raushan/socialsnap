package com.cmsc436.socialsnap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UploadUI extends Activity implements LocationListener {
	protected LocationManager locationManager;
	protected LocationListener locatioinListener;
	
	private Bitmap photoBitmap;
	private Uri photoUri;
	private String title = "SocialSnap:";
	private String comment;
	private TextView actionText;
	private TextView textView;
	Location mLocation;
	
	private MyImgurUploadTask mImgurUploadTask;
    private String mImgurUrl;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);

		textView = (TextView) findViewById(R.id.location);

		Intent photoIntent = getIntent();
		
		// Retrieve photo image for UI display
		photoBitmap = (Bitmap) photoIntent.getParcelableExtra("photoBitmap");
		ImageView photoView = (ImageView) findViewById(R.id.photoView);
		photoView.setScaleType(ScaleType.FIT_XY);
		photoView.setImageBitmap(photoBitmap);
		
		// Retrieve Uri of photo for upload
		photoUri = Uri.parse(photoIntent.getStringExtra("photoUri"));

		EditText editText = (EditText) findViewById(R.id.comment);
		comment = editText.getText().toString();

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
				actionText.setText(Integer.toString(80 - s.toString().length()));
			}
		});

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Boolean isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		mLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {
			if (mLocation == null) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, this);
				Log.d("GPS", "GPS Enabled");
				if (locationManager != null) {
					mLocation = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}
		}

		// List<Address> address = getLocation();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Geocoder.isPresent()) {
			(new GetAddressTask(this)).execute(mLocation);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upload, menu);
		MenuItem item = menu.findItem(R.id.char_counter);
		actionText = (TextView) item.getActionView();
		actionText.setText("80");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_upload) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

			// Setting Dialog Message
			alertDialog
					.setMessage("Are you sure you want to upload?");

			// On pressing Settings button
			alertDialog.setPositiveButton("Upload",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						    (new MyImgurUploadTask(photoUri, title, comment)).execute();
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
		return super.onOptionsItemSelected(item);
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
                return ("IO Exception trying to get address");
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
            } else {
                mImgurUrl = null;
                Toast.makeText(UploadUI.this, "Failed to upload picture", Toast.LENGTH_LONG).show();
            }
        }
    }
    

}
