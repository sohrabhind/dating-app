package com.hindbyte.dating.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.model.Profile;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdvancedPeopleListAdapter extends RecyclerView.Adapter<AdvancedPeopleListAdapter.MyViewHolder> {

	private Context mContext;
	private List<Profile> itemList;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Profile obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

	public static class MyViewHolder extends RecyclerView.ViewHolder {

		public TextView mTitle, mSubtitle;
		public CircularImageView mOnlineImage, mGenderImage;
		public ImageView mProfileLevelIcon;

        public ImageView mSquarePhotoImage;

		public CardView mParent;
		public ProgressBar mProgressBar;

		public MyViewHolder(View view) {
			super(view);
			mParent = view.findViewById(R.id.parent);
			mSquarePhotoImage = view.findViewById(R.id.photo_image);
			mOnlineImage = view.findViewById(R.id.online_image);
			mProfileLevelIcon = view.findViewById(R.id.profileLevelIcon);
			mGenderImage = view.findViewById(R.id.gender_image);
			mTitle = view.findViewById(R.id.title_label);
			mSubtitle = view.findViewById(R.id.subtitle_label);
			mProgressBar = view.findViewById(R.id.progressBar);
		}
	}


	public AdvancedPeopleListAdapter(Context mContext, List<Profile> itemList) {
		this.mContext = mContext;
		this.itemList = itemList;
	}

	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
		itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_thumbnail_square, parent, false);
		return new MyViewHolder(itemView);
	}

	@SuppressLint({"DefaultLocale", "SetTextI18n"})
	@Override
	public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
		final Profile item = itemList.get(position);
        holder.mSubtitle.setVisibility(View.GONE);

        holder.mOnlineImage.setVisibility(View.GONE);
        holder.mProfileLevelIcon.setVisibility(View.GONE);
		holder.mGenderImage.setVisibility(View.GONE);

		holder.mProgressBar.setVisibility(View.VISIBLE);
		holder.mSquarePhotoImage.setVisibility(View.VISIBLE);

		if (item.getBigPhotoUrl() != null && item.getBigPhotoUrl().length() > 0 && (App.getInstance().getSettings().isAllowShowNotModeratedProfilePhotos() || App.getInstance().getId() == item.getId() || item.getPhotoModerateAt() != 0)) {
			final ImageView img;
			img = holder.mSquarePhotoImage;

			final ProgressBar progressView = holder.mProgressBar;


			Picasso.get()
					.load(item.getBigPhotoUrl())
					.placeholder(R.drawable.profile_default_photo)
					.error(R.drawable.profile_default_photo)
					.into(img, new Callback() {

						@Override
						public void onSuccess() {
							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);
							holder.mGenderImage.setVisibility(View.VISIBLE);
						}

						@Override
						public void onError(Exception e) {
							progressView.setVisibility(View.GONE);
							img.setImageResource(R.drawable.profile_default_photo);
							img.setVisibility(View.VISIBLE);
							holder.mGenderImage.setVisibility(View.VISIBLE);
						}
					});


		} else {
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mGenderImage.setVisibility(View.VISIBLE);

			holder.mSquarePhotoImage.setVisibility(View.VISIBLE);
			holder.mSquarePhotoImage.setImageResource(R.drawable.profile_default_photo);
		}


		String fullname = item.getFullname();
		if (item.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			fullname = fullname.substring(0, fullname.lastIndexOf(' '));
		}

		if (item.getAge() != 0) {
			holder.mTitle.setText(fullname + ", " + item.getAge());
		} else {
			holder.mTitle.setText(fullname);
		}

		switch (item.getGender()) {
			case 0: {
				holder.mGenderImage.setImageResource(R.drawable.ic_gender_male);
				break;
			}
			case 1: {
				holder.mGenderImage.setImageResource(R.drawable.ic_gender_female);
				break;
			}
			default: {
				holder.mGenderImage.setImageResource(R.drawable.ic_gender_secret);
				break;
			}
		}

		if (item.getDistance() > 0.0) {
			holder.mSubtitle.setVisibility(View.VISIBLE);
			holder.mSubtitle.setText(String.format("%.1f km", item.getDistance()));
		} else {
		    // For guests
		    if (item.getLastVisit().length() > 0) {
                holder.mSubtitle.setVisibility(View.VISIBLE);
                holder.mSubtitle.setText(item.getLastVisit());
            } else {
                holder.mSubtitle.setVisibility(View.GONE);
            }
		}

		if (item.isOnline()) {
			holder.mOnlineImage.setVisibility(View.VISIBLE);
		} else {
			holder.mOnlineImage.setVisibility(View.GONE);
		}

		switch (item.getLevelMode()) {
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


		holder.mParent.setOnClickListener(view -> {
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(view, item, position);
			}
		});
	}

	@Override
	public int getItemCount() {
		return itemList.size();
	}
}