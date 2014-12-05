package com.cmsc436.socialsnap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class GridLayoutActivity extends FragmentActivity implements
ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener,
OnMyLocationButtonClickListener {

	protected static final String EXTRA_RES_ID = "POS";
	
	private LocationManager locationManager;
	private static final int CAMERA_REQUEST_CODE = 100;
	private Bitmap photo = null;
	private GoogleApiClient mGoogleApiClient;
	private GoogleMap mMap;
	
	private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	private ArrayList<Integer> mThumbIdsFlowers = new ArrayList<Integer>(
			Arrays.asList(R.drawable.image1, R.drawable.image2,
					R.drawable.image3, R.drawable.image4, R.drawable.image5,
					R.drawable.image6, R.drawable.image7, R.drawable.image8,
					R.drawable.image9, R.drawable.image10, R.drawable.image11,
					R.drawable.image12));

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
		setUpMapIfNeeded();
		setUpGoogleApiClientIfNeeded();
		mGoogleApiClient.connect();

		
		
		GridView gridview = (GridView) findViewById(R.id.gridview);

		// Create a new ImageAdapter and set it as the Adapter for this GridView
		gridview.setAdapter(new ImageAdapter(this, mThumbIdsFlowers));

		// Set an setOnItemClickListener on the GridView
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				
				//Create an Intent to start the ImageViewActivity
				Intent intent = new Intent(GridLayoutActivity.this,
						ImageViewActivity.class);
				
				// Add the ID of the thumbnail to display as an Intent Extra
				intent.putExtra(EXTRA_RES_ID, (int) id);
				
				// Start the ImageViewActivity
				startActivity(intent);
			}
		});
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Obtain photo from camera
			photo = (Bitmap) data.getExtras().get("data");

//			try {
//				photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			Log.i("ACTIVITY RESULT","PhotUri: "+photoUri.toString());
//			ImageView view = (ImageView) findViewById(R.id.imageViewTest);
//			view.setImageBitmap(photo);
			// Pass photo to upload activity
			Intent uploadIntent = new Intent(GridLayoutActivity.this, UploadUI.class);
			uploadIntent.putExtra("photoBitmap", photo);
			//uploadIntent.putExtra("photoUri", photoUri.toString());
			startActivity(uploadIntent);
		}
	}
	
	
	
	
	
	
	
	/* ============ GOOGLE MAP ============= */
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
    }
	
    
    private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
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
		Double lat = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude();
		Double lng = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude();
		
		String place = "Marker";
		
		Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = gcd.getFromLocation(lat, lng, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (addresses.size() > 0) {
		    place = addresses.get(0).getLocality();
		}
		
		mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(place));
		return false;
	}

	
	@Override
	public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);
		
		Double lat = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude();
		Double lng = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude();
		
		LatLng a = new LatLng(lat, lng);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(a, 18));
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
			// DO THIS

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Boolean isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			//Check if GPS is enabled before starting new activity
			if (isGPSEnabled) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, 100);
			} else {
				showSettingsAlert();
			}
			return true;
		}
		if (id == R.id.refresh) {
			// DO THIS
			makeToast("Refreshes images by grabbing updates from database");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
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

	private void makeToast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

}