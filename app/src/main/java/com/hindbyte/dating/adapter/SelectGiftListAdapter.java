package com.hindbyte.dating.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.hindbyte.dating.R;
import com.hindbyte.dating.model.BaseGift;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class SelectGiftListAdapter extends RecyclerView.Adapter<SelectGiftListAdapter.MyViewHolder> {

	private Context mContext;
	private List<BaseGift> itemList;

	public class MyViewHolder extends RecyclerView.ViewHolder {

		public TextView title;
		public ImageView thumbnail;

		public MyViewHolder(View view) {

			super(view);

			title = view.findViewById(R.id.title);
			thumbnail = view.findViewById(R.id.thumbnail);
		}
	}


	public SelectGiftListAdapter(Context mContext, List<BaseGift> itemList) {

		this.mContext = mContext;
		this.itemList = itemList;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.base_gift_list_row, parent, false);


		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {

		BaseGift u = itemList.get(position);
		holder.title.setText(u.getCost() + " " + mContext.getString(R.string.label_level));

		// loading album cover using Glide library
		Picasso.get()
				.load(u.getImgUrl())
				.placeholder(R.drawable.img_loading)
				.error(R.drawable.img_loading)
				.into(holder.thumbnail, new Callback() {
					@Override
					public void onSuccess() {
					}

					@Override
					public void onError(Exception e) {
					}
				});

	}

	@Override
	public int getItemCount() {

		return itemList.size();
	}
}