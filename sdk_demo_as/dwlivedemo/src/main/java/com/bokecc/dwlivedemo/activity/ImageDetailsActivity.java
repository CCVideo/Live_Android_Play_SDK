package com.bokecc.dwlivedemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.bokecc.dwlivedemo.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_details_activity);
        String imageUrl = getIntent().getStringExtra("imageUrl");
        PhotoView view = findViewById(R.id.photo_view);
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(view);
        }
    }
}
