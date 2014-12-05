package com.cmsc436.socialsnap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
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
import android.text.AndroidCharacter;
import android.util.Log;
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
	private Uri photoUri = null;
	private LocationManager locationManager;
	private String mCurrentPhotoPath;
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
				    //try {
				        //Intent to open camera
				        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				        File photoFile = createImageFile();
				        if(photoFile != null){
				        	photoUri = Uri.fromFile(photoFile);
				            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
				            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
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
				//Check if GPS is enabled before starting new activity
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Obtain photo from camera
			

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
			
			//photo = (Bitmap) data.getExtras().get("data");
			ContentResolver c = getContentResolver();
			Bitmap photo = null;
			try {
				photo = android.provider.MediaStore.Images.Media.getBitmap(c, photoUri);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Intent uploadIntent = new Intent(MainActivity.this, UploadUI.class);
			uploadIntent.putExtra("photoBitmap", photo);
			uploadIntent.putExtra("photoUri", photoUri.toString());
			startActivity(uploadIntent);
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private File createImageFile() {
	    // Create an image file name
		File photoFile=null;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		//check if media is mounted, and create a file for the image if it is using filename from above
		Log.i("CREATE IMAGE FILE","Creating image file");
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			photoFile = new File(android.os.Environment.getExternalStorageDirectory(), imageFileName);
		}else{
			photoFile = new File(getApplicationContext().getCacheDir(),imageFileName);
			
		}

	    // Save a file: path for use with ACTION_VIEW intents
	    mCurrentPhotoPath = "file:" + photoFile.getAbsolutePath();
	    return photoFile;
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
		if (id == R.id.action_camera) {
			// TODO THIS

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
