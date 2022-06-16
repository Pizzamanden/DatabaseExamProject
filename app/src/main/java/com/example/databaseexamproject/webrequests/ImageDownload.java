package com.example.databaseexamproject.webrequests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ImageDownload extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView;

    public ImageDownload(ImageView ImageView){
        this.imageView = ImageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        // This is called on .execute()
        Bitmap bitmap = null; // We have to return something
        try {
            InputStream inputStream = new java.net.URL(strings[0]).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // This is called when we are done
        imageView.setImageBitmap(bitmap);
    }
}
