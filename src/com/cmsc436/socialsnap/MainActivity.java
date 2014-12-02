package com.cmsc436.socialsnap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

	private static final int CAMERA_REQUEST_CODE = 100;
	private Bitmap photo = null;
	private Uri photoUri = null;
	private LocationManager locationManager;
	// Test
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button takePictureButton = (Button) findViewById(R.id.take_picture_button);
		takePictureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Boolean isGPSEnabled = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				//Check if GPS is enabled before starting new activity
				if (isGPSEnabled) {
				    try {
				        File photoFile = createImageFile();
				        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				        photoUri = Uri.fromFile(photoFile);
				        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
	                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Unable to write image" , Toast.LENGTH_LONG).show();
                    }
				} else {
					showSettingsAlert();
				}
			}
		});

		final Button viewPicturesButton = (Button) findViewById(R.id.view_pictures_button);
		viewPicturesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent galleryIntent = new Intent(MainActivity.this,
						GridLayoutActivity.class);
				startActivity(galleryIntent);
			}
		});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Obtain photo from camera
			photo = (Bitmap) data.getExtras().get("data");
			// Pass photo to upload activity
			Intent uploadIntent = new Intent(MainActivity.this, UploadUI.class);
			uploadIntent.putExtra("photoBitmap", photo);
			uploadIntent.putExtra("photoUri", photoUri.toString());
			startActivity(uploadIntent);
		}
	}
	
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
	    return image;
	}

	@Override
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
				startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
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
