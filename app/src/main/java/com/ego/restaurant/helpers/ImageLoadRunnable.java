package com.ego.restaurant.helpers;

import android.app.Activity;
import android.widget.ImageView;

public class ImageLoadRunnable implements Runnable {
    private String imageUrl;
    private ImageView imageView;
    private Activity context;
    public ImageLoadRunnable(String imageUrl, ImageView imageView, Activity context) {
        this.imageUrl = imageUrl;
        this.imageView = imageView;
        this.context = context;
    }

    @Override
    public void run() {
    }
}