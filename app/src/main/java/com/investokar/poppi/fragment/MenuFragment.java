package com.investokar.poppi.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.investokar.poppi.R;
import com.investokar.poppi.activity.UpgradeActivity;
import com.investokar.poppi.activity.LikedActivity;
import com.investokar.poppi.activity.LikesActivity;
import com.investokar.poppi.activity.ProfileActivity;
import com.investokar.poppi.activity.SearchActivity;
import com.investokar.poppi.activity.SettingsActivity;
import com.investokar.poppi.activity.WebActivity;
import com.investokar.poppi.util.ToastWindow;
import com.investokar.poppi.view.GradientTextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.investokar.poppi.app.App;
import com.investokar.poppi.constants.Constants;
import com.squareup.picasso.Picasso;


public class MenuFragment extends Fragment implements Constants {


    CircularImageView mProfilePhoto;
    ImageView mProfileLevelIcon, level_icon;
    private TextView mNavProfileFullname, mNavProfileSubhead, plan_type, free_message_count, premium_message_count, like_count;

    private LinearLayout mNavProfile, mNavSearch, mNavLikes, mNavLiked, mNavUpgrades, mNavSettings;



    int productType;

    static String ITEM_PRODUCT_1_AMOUNT = "300";// in usd cents | 3 usd = 300 cents
    static String ITEM_PRODUCT_2_AMOUNT = "600";// in usd cents | 6 usd = 600 cents
    static String ITEM_PRODUCT_3_AMOUNT = "900";// in usd cents | 9 usd = 900 cents

    LinearLayout mBuy1Button, mBuy2Button, mBuy3Button;
    TextView mBuyButton, totalMessages, totalLikes, profileBoost, iap1_google_price, iap2_google_price, iap3_google_price;
    ImageView levelIcon;
    GradientTextView badgeTitle;

    private Boolean loading = false;

    ToastWindow toastWindow = new ToastWindow();
    private String packageName = "";

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        mNavProfile = rootView.findViewById(R.id.nav_profile);
        mNavProfileFullname = rootView.findViewById(R.id.nav_profile_fullname);
        mNavProfileSubhead = rootView.findViewById(R.id.nav_profile_subhead);

        mNavSearch = rootView.findViewById(R.id.nav_search);

        mNavLikes = rootView.findViewById(R.id.nav_likes);
        mNavLiked = rootView.findViewById(R.id.nav_liked);
        mNavUpgrades = rootView.findViewById(R.id.nav_upgrades);
        mNavSettings = rootView.findViewById(R.id.nav_settings);

        mProfilePhoto = rootView.findViewById(R.id.profilePhoto);
        mProfileLevelIcon = rootView.findViewById(R.id.profileLevelIcon);


        plan_type = rootView.findViewById(R.id.plan_type);
        free_message_count = rootView.findViewById(R.id.free_message_count);
        premium_message_count = rootView.findViewById(R.id.premium_message_count);
        like_count = rootView.findViewById(R.id.like_count);
        level_icon = rootView.findViewById(R.id.my_level_icon);

        mNavProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(requireActivity(), ProfileActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                requireActivity().startActivity(i);
            }
        });

        mNavSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(requireActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });


        mNavLikes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(requireActivity(), LikesActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavLiked.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(requireActivity(), LikedActivity.class);
                i.putExtra("itemId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavUpgrades.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(requireActivity(), UpgradeActivity.class);
                startActivity(i);
            }
        });

        mNavSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(requireActivity(), SettingsActivity.class);
                startActivity(i);
            }
        });


        mBuy1Button = rootView.findViewById(R.id.iap1_google_btn);
        mBuy2Button = rootView.findViewById(R.id.iap2_google_btn);
        mBuy3Button = rootView.findViewById(R.id.iap3_google_btn);
        mBuyButton = rootView.findViewById(R.id.iap_google_btn);
        badgeTitle = rootView.findViewById(R.id.get_badge_title);
        levelIcon = rootView.findViewById(R.id.level_icon);
        totalMessages = rootView.findViewById(R.id.total_messages);
        totalLikes = rootView.findViewById(R.id.total_likes);
        profileBoost = rootView.findViewById(R.id.profile_boost);
        iap1_google_price = rootView.findViewById(R.id.iap1_google_price);
        iap2_google_price = rootView.findViewById(R.id.iap2_google_price);
        iap3_google_price = rootView.findViewById(R.id.iap3_google_price);

        badgeTitle.setText("Unlock premium-only benefits today!");
        totalMessages.setText("1500 Messages");
        totalLikes.setText("Unlimted Likes");
        profileBoost.setText("Open 20 Chats Daily");
        levelIcon.setImageResource(R.drawable.level_gold);
        productType = 2;
        mBuy1Button.setBackgroundResource(R.color.md_theme_white_3);
        mBuy2Button.setBackgroundResource(R.drawable.gradation_primary);
        mBuy3Button.setBackgroundResource(R.color.md_theme_white_3);

        mBuy1Button.setOnClickListener(v -> {
            totalMessages.setText("500 Messages");
            totalLikes.setText("Unlimted Likes");
            profileBoost.setText("Open 10 Chats Daily");
            levelIcon.setImageResource(R.drawable.level_silver);
            mBuy1Button.setBackgroundResource(R.drawable.gradation_primary);
            mBuy2Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy3Button.setBackgroundResource(R.color.md_theme_white_3);
            productType = 1;
        });

        mBuy2Button.setOnClickListener(v -> {
            totalMessages.setText("1500 Messages");
            totalLikes.setText("Unlimted Likes");
            profileBoost.setText("Open 20 Chats Daily");
            levelIcon.setImageResource(R.drawable.level_gold);
            mBuy1Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy2Button.setBackgroundResource(R.drawable.gradation_primary);
            mBuy3Button.setBackgroundResource(R.color.md_theme_white_3);
            productType = 2;
        });

        mBuy3Button.setOnClickListener(v -> {
            totalMessages.setText("3000 Messages");
            totalLikes.setText("Unlimted Likes");
            profileBoost.setText("Open 30 Chats Daily");
            levelIcon.setImageResource(R.drawable.level_diamond);
            mBuy1Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy2Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy3Button.setBackgroundResource(R.drawable.gradation_primary);
            productType = 3;
        });

        mBuyButton.setOnClickListener(v -> {
            launchBilling(productType);
        });


        updateView();
        // Inflate the layout for this fragment
        return rootView;
    }


    private void launchBilling(int productType) {
        Intent i = new Intent(getActivity(), WebActivity.class);
        if (productType == 1) {
            i.putExtra("TITLE", "Upgrade Level");
            i.putExtra("URL", "https://hindbyte.com/payment/upgrade.php?level=1&user_id="+String.valueOf(App.getInstance().getId()));
            startActivity(i);
        } else if (productType == 2) {
            i.putExtra("TITLE", "Upgrade Level");
            i.putExtra("URL", "https://hindbyte.com/payment/upgrade.php?level=2&user_id="+String.valueOf(App.getInstance().getId()));
            startActivity(i);
        } else if (productType == 3) {
            i.putExtra("TITLE", "Upgrade Level");
            i.putExtra("URL", "https://hindbyte.com/payment/upgrade.php?level=3&user_id="+String.valueOf(App.getInstance().getId()));
            startActivity(i);
        }
    }
    
    @SuppressLint("SetTextI18n")
    public void updateView() {
        App.getInstance().loadSettings();
        if (App.getInstance().getPhotoUrl() != null && !App.getInstance().getPhotoUrl().isEmpty()) {
            Picasso.get()
            .load(App.getInstance().getPhotoUrl())
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(mProfilePhoto);
        } else {
            mProfilePhoto.setImageResource(R.drawable.profile_default_photo);
        }

        // Level

        switch (App.getInstance().getLevelMode()) {
            case 1:
                mProfileLevelIcon.setVisibility(View.VISIBLE);
                mProfileLevelIcon.setImageResource(R.drawable.level_silver);
                plan_type.setText("Your Plan: Silver");
                free_message_count.setText("Open Chats Today: "+App.getInstance().getFreeMessagesCount());
                premium_message_count.setText("Premium/Reply Messages: "+App.getInstance().getLevelMessagesCount());
                like_count.setText("Daily Likes: Unlimted");
                level_icon.setImageResource(R.drawable.level_silver);
                break;
            case 2:
                mProfileLevelIcon.setVisibility(View.VISIBLE);
                mProfileLevelIcon.setImageResource(R.drawable.level_gold);
                plan_type.setText("Your Plan: Gold");
                free_message_count.setText("Open Chats Today: "+App.getInstance().getFreeMessagesCount());
                premium_message_count.setText("Premium/Reply Messages: "+App.getInstance().getLevelMessagesCount());
                like_count.setText("Daily Likes: Unlimted");
                level_icon.setImageResource(R.drawable.level_gold);
                break;
            case 3:
                mProfileLevelIcon.setVisibility(View.VISIBLE);
                mProfileLevelIcon.setImageResource(R.drawable.level_diamond);
                plan_type.setText("Your Plan: Diamond");
                free_message_count.setText("Open New Chats Today: "+App.getInstance().getFreeMessagesCount());
                premium_message_count.setText("Premium/Reply Messages: "+App.getInstance().getLevelMessagesCount());
                like_count.setText("Daily Likes: Unlimted");
                level_icon.setImageResource(R.drawable.level_diamond);
                break;
            default:
                mProfileLevelIcon.setVisibility(View.GONE);
                plan_type.setText("Your Plan:  Free Plan");
                free_message_count.setText("Open Chats Today: "+App.getInstance().getFreeMessagesCount());
                premium_message_count.setText("Premium/Reply Messages: "+App.getInstance().getLevelMessagesCount());
                like_count.setText("Daily Likes: Unlimted");
                level_icon.setImageResource(R.color.transparent);
                break;
        }

        // Fullname
        mNavProfileFullname.setText(App.getInstance().getFullname());
        mNavProfileSubhead.setText("@" + App.getInstance().getUsername());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.action_filters);
        item.setVisible(false);

        item = menu.findItem(R.id.action_remove_all);
        item.setVisible(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}