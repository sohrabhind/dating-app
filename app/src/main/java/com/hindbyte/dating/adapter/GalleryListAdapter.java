package com.hindbyte.dating.adapter;

import static com.hindbyte.dating.R.id.progressBar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.ViewImageActivity;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.Image;
import com.hindbyte.dating.view.FaceDetectImageView;
import com.hindbyte.dating.view.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;


public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.MyViewHolder> {

    private List<Image> images;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public ProgressBar mProgressBar;
        public RelativeLayout recyclerViewItem;

        public MyViewHolder(View view) {
            super(view);
            recyclerViewItem = view.findViewById(R.id.recyclerViewItem);
            thumbnail = view.findViewById(R.id.thumbnail);
            mProgressBar = view.findViewById(progressBar);
        }
    }


    public GalleryListAdapter(Context context, List<Image> images) {
        mContext = context;
        this.images = images;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_thumbnail, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Image image = images.get(position);
        final ProgressBar progressBar = holder.mProgressBar;
        holder.thumbnail.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        int screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        holder.thumbnail.setMinimumHeight(screenwidth);
        holder.thumbnail.setMaxHeight((int) (screenwidth*1.3f));

        if (image.getItemType() == Constants.GALLERY_ITEM_TYPE_IMAGE) {
            Picasso.get()
                    .load(image.getImgUrl())
                    .placeholder(R.drawable.profile_default_photo)
                    .error(R.drawable.profile_default_photo)
                    .into(holder.thumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }

        holder.recyclerViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImageActivity.class);
                intent.putExtra("itemId", image.getId());
                mContext.startActivity(intent);
            }
        });


        if (image.getOwner().getId() == App.getInstance().getId()) {
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}