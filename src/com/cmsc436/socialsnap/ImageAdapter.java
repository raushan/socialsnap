package com.cmsc436.socialsnap;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	//private static final int PADDING = 0;
	private static final int WIDTH = 300;
	private static final int HEIGHT = 300;
	private Context mContext;
	private List<Bitmap> mPhotos;

	// Store the list of image IDs
	public ImageAdapter(Context c, List<Bitmap> photos) {
		mContext = c;
		this.mPhotos = photos;
	}

	// Return the number of items in the Adapter
	@Override
	public int getCount() {
		return mPhotos.size();
	}

	// Return the data item at position
	@Override
	public Object getItem(int position) {
		return mPhotos.get(position);
	}

	// Will get called to provide the ID that
	// is passed to OnItemClickListener.onItemClick()
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void clear() {
		mPhotos.clear();
	}

	// Return an ImageView for each item referenced by the Adapter
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ImageView imageView = (ImageView) convertView;

		// if convertView's not recycled, initialize some attributes
		if (imageView == null) {
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(WIDTH, HEIGHT));
			//imageView.setPadding(PADDING, PADDING, PADDING, PADDING);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		}
		Log.i("ImageAdpapter", "Setting image bitmap");
		imageView.setImageBitmap(mPhotos.get(position));
		return imageView;
	}
}
