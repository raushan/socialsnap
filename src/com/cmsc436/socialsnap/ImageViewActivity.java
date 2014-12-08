package com.cmsc436.socialsnap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.socialsnap.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ImageViewActivity extends Activity {

	private static final int CAMERA_REQUEST_CODE = 100;
	private String mCurrentPhotoPath;
	private File photoFile;
	private Uri photoUri;
	private ImageView imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF29A6CF));

		setContentView(R.layout.viewimage);
		Intent intent = getIntent();

		imageView = (ImageView) findViewById(R.id.photoView);
		TextView textView = (TextView) findViewById(R.id.caption);

		// Get the ID of the image to display and set it as the image for this
		// ImageView
		String curUrl = intent.getStringExtra("Bitmap");
		(new ImgurViewTask(curUrl)).execute();
		textView.setGravity(Gravity.CENTER);
		textView.setText(intent.getStringExtra("Comment"));
		
		textView.setTextColor(0xFFFFFFFF);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED && requestCode == CAMERA_REQUEST_CODE) {
			// Pass photo Uri to upload activity
			Intent uploadIntent = new Intent(ImageViewActivity.this,
					UploadUI.class);
			uploadIntent.putExtra("photoUri", photoUri);
			Log.i("GridLayoutActivity on result", "Starting upload activity");
			startActivity(uploadIntent);
		}
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
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			if (cameraIntent.resolveActivity(getPackageManager()) != null) {
				try {
					photoFile = createImageFile();
				} catch (IOException e) {
					Toast.makeText(ImageViewActivity.this,
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

	private class ImgurViewTask extends
			AsyncTask<Void, Void, Bitmap> {
		String imageUrl;
		public ImgurViewTask(String url) {
			this.imageUrl = url;
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(imageUrl)
								.getContent());
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return bitmap;

		}

		@Override
		protected void onPostExecute(Bitmap photo) {
			imageView.setImageBitmap(photo);
		}

	}
}