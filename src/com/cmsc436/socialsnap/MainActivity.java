package com.cmsc436.socialsnap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.socialsnap.R;

public class MainActivity extends Activity {

	private static final int CAMERA_REQUEST_CODE = 100;
	private Bitmap photo = null;
	private File photoFile = null;
	private Uri photoUri = null;
	private LocationManager locationManager;
	private String mCurrentPhotoPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);

		final Button takePictureButton = (Button) findViewById(R.id.take_picture_button);
		takePictureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Boolean isGPSEnabled = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				// Check if GPS is enabled before starting new activity
				if (isGPSEnabled
						&& cameraIntent.resolveActivity(getPackageManager()) != null) {
					try {
						photoFile = createImageFile();
					} catch (IOException e) {
						Toast.makeText(MainActivity.this,
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
				} else {
					showSettingsAlert();
				}
			}
		});

		final Button viewPicturesButton = (Button) findViewById(R.id.view_pictures_button);
		viewPicturesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Boolean isGPSEnabled = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				// Check if GPS is enabled before starting new activity
				if (isGPSEnabled) {
					Intent galleryIntent = new Intent(MainActivity.this,
							GridLayoutActivity.class);
					startActivity(galleryIntent);
				} else {
					showSettingsAlert();
				}

			}
		});

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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Pass photo Uri to upload activity
			Intent uploadIntent = new Intent(MainActivity.this, UploadUI.class);
			uploadIntent.putExtra("photoUri", photoUri);
			//Log.i("MainActivity on result", "Starting upload activity");
			startActivity(uploadIntent);
		}
	}

	/* ============ ACTION BAR ============= */

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
		if (id == R.id.info) {
			showInfoAlert();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/* ============ HELPER METHODS ============= */

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

	private void makeToast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

}
