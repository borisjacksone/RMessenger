package com.royal.chat.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.quickblox.core.helper.StringUtils;
import com.royal.chat.R;
import com.royal.chat.utils.ToastUtils;

public class ChangeNameDialog extends Dialog {

    private String oldName;
    private OnChangeNameResult mDialogResult;

    public ChangeNameDialog(AppCompatActivity parent, String oldName) {
        super(parent);
        this.oldName = oldName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change_name);

        final EditText editName = findViewById(R.id.edittext_user_name);
        editName.setText(oldName);

        Button buttonOK = findViewById(R.id.button_ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editName.getText().toString();
                if (newName.equals(oldName) || StringUtils.isEmpty(newName)) {
                    ToastUtils.shortToast(R.string.enter_new_name);
                    editName.setText("");
                    return;
                }

                if (mDialogResult != null) {
                    mDialogResult.finish(newName);
                }
                dismiss();
            }
        });
    }

    public void setDialogResult(OnChangeNameResult dialogResult) {
        mDialogResult = dialogResult;
    }

    public interface OnChangeNameResult {
        void finish(String result);
    }
}
