package com.cmsc436.socialsnap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageViewActivity extends Activity {

	private LocationManager locationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.viewimage);
	
		Intent intent = getIntent();
		
		ImageView imageView = (ImageView) findViewById(R.id.photoView);
		TextView textView = (TextView) findViewById(R.id.caption);
		
		// Get the ID of the image to display and set it as the image for this ImageView
		imageView.setImageResource(intent.getIntExtra(GridLayoutActivity.EXTRA_RES_ID, 0));
		textView.setText("caption here");
		
		
		
	}
	
	
	
	
	
	/* ============ ACTION BAR ============= */
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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