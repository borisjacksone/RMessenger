package com.royal.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;

import com.royal.chat.BuildConfig;
import com.royal.chat.R;
import com.royal.chat.utils.ImageUtils;


public class AppInfoActivity extends BaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, AppInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);

        initUI();
        fillUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.appinfo_title);
    }

    public void fillUI() {

        if (BuildConfig.IS_QA) {
            String appVersion = BuildConfig.VERSION_NAME;
            String versionQACode = String.valueOf(BuildConfig.VERSION_QA_CODE);
            String qaVersion = appVersion + "." + versionQACode;
            Spannable spannable = new SpannableString(qaVersion);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), appVersion.length() + 1,
                    qaVersion.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        ImageView descImageView = findViewById(R.id.imageDesc);
        descImageView.setImageDrawable(getResources().getDrawable(R.drawable.desc_pic));
        ImageUtils.scaleImage(descImageView);
    }
}