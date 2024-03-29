package com.hindbyte.dating.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.ProfileActivity;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.model.Profile;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;


public class HotgameAdapter extends RecyclerView.Adapter<HotgameAdapter.MyViewHolder> {

	private final Context mContext;
	private final List<Profile> itemList;

	public static class MyViewHolder extends RecyclerView.ViewHolder {

		public TextView title, location, distance;
		public ImageView thumbnail, mProfileLevelIcon;
		public ProgressBar progress;

		public MyViewHolder(View view) {
			super(view);
			title = view.findViewById(R.id.item_name);
			mProfileLevelIcon = view.findViewById(R.id.profileLevelIcon);
			location = view.findViewById(R.id.item_location);
			distance = view.findViewById(R.id.item_distance);
			thumbnail = view.findViewById(R.id.item_image);
			progress = view.findViewById(R.id.progress_bar);
		}
	}


	public HotgameAdapter(Context mContext, List<Profile> itemList) {
		this.mContext = mContext;
		this.itemList = itemList;
	}

	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_swipe_card, parent, false);
		return new MyViewHolder(itemView);
	}


	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {
		holder.progress.setVisibility(View.GONE);
		Profile profileItem = itemList.get(position);
		
		String fullname = profileItem.getFullname();
		if (profileItem.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			fullname = fullname.substring(0, fullname.lastIndexOf(' '));
		}
		holder.title.setText(fullname + ", " + profileItem.getAge());
		holder.distance.setVisibility(View.GONE);

		if (profileItem.getDistance() != 0) {

			holder.distance.setVisibility(View.VISIBLE);

			if (profileItem.getDistance() < 3) {

				holder.distance.setText(mContext.getString(R.string.label_nearby));

			} else {

				holder.distance.setText((int) profileItem.getDistance() + "km");
			}
		}

		holder.location.setVisibility(View.GONE);

		if (profileItem.getLocation().length() != 0) {
			holder.location.setVisibility(View.VISIBLE);
			holder.location.setText(profileItem.getLocation());
		}//

		final ImageView imgView = holder.thumbnail;
		final ProgressBar progressView = holder.progress;

		Picasso.get()
				.load(profileItem.getBigPhotoUrl())
				.placeholder(R.drawable.profile_default_photo)
				.error(R.drawable.profile_default_photo)
				.into(imgView, new Callback() {

					@Override
					public void onSuccess() {
						progressView.setVisibility(View.GONE);
						imgView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onError(Exception e) {
						progressView.setVisibility(View.GONE);
						imgView.setVisibility(View.VISIBLE);
					}
				});

		switch (profileItem.getLevelMode()) {
			case 1:
				holder.mProfileLevelIcon.setVisibility(View.VISIBLE);
				holder.mProfileLevelIcon.setImageResource(R.drawable.level_silver);
				break;
			case 2:
				holder.mProfileLevelIcon.setVisibility(View.VISIBLE);
				holder.mProfileLevelIcon.setImageResource(R.drawable.level_gold);
				break;
			case 3:
				holder.mProfileLevelIcon.setVisibility(View.VISIBLE);
				holder.mProfileLevelIcon.setImageResource(R.drawable.level_diamond);
				break;
			default:
				holder.mProfileLevelIcon.setVisibility(View.GONE);
				break;
		}

		imgView.setOnClickListener(v -> {
			Intent intent = new Intent(mContext, ProfileActivity.class);
			intent.putExtra("profileId", profileItem.getId());
			mContext.startActivity(intent);
		});
	}


	@Override
	public int getItemCount() {
		return itemList.size();
	}

}