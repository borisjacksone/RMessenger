package com.royal.chat.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.royal.chat.App;
import com.royal.chat.R;
import com.royal.chat.ui.activity.SplashActivity;
import com.royal.chat.utils.SharedPrefsHelper;

import java.util.Locale;

public class LanguageDialog extends Dialog {
    private AppCompatActivity parentActivity;

    public LanguageDialog(AppCompatActivity parent) {
        super(parent);
        parentActivity = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_language);

        final SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        final String currentLocale = sharedPrefsHelper.getLocale();

        RadioButton radioEn = findViewById(R.id.radioEnglish);
        RadioButton radioKo = findViewById(R.id.radioKorean);

        if (currentLocale.equals(App.LOCALE_EN)) {
            radioEn.setChecked(true);
        } else {
            radioKo.setChecked(true);
        }

        Button buttonOK = findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioGroup radioGroup = findViewById(R.id.radioGroupClass);

                int selectedID = radioGroup.getCheckedRadioButtonId();
                RadioButton radioClassButton = findViewById(selectedID);
                String part = radioClassButton.getText().toString();
                String locale = App.LOCALE_EN;
                if (part.equals(parentActivity.getResources().getString(R.string.text_korean))) {
                    locale = App.LOCALE_KO;
                }

                if (locale.equals(currentLocale)) {
                    dismiss();
                    return;
                }

                sharedPrefsHelper.putLocale(locale);
                setAppLocale(locale);
                dismiss();
                parentActivity.finish();

                Intent intent = new Intent(parentActivity, SplashActivity.class);
                parentActivity.startActivity(intent);
            }
        });
    }

    private void setAppLocale(String localeCode) {
        Resources res = parentActivity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(new Locale(localeCode.toLowerCase()));
        }
        res.updateConfiguration(conf, dm);
    }
}
