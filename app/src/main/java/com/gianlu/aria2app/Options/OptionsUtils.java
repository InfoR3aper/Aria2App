package com.gianlu.aria2app.Options;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.gianlu.aria2app.Adapters.OptionsAdapter;
import com.gianlu.aria2app.NetIO.Aria2.Option;
import com.gianlu.aria2app.R;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.SuperTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public final class OptionsUtils {

    @NonNull
    public static JSONObject toJson(@Nullable Map<String, String> options) throws JSONException {
        if (options == null) return new JSONObject();

        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (Objects.equals(entry.getKey(), "header") || Objects.equals(entry.getKey(), "index-out")) {
                if (entry.getValue().contains(";")) {
                    json.put(entry.getKey(), CommonUtils.toJSONArray(entry.getValue().split(";")));
                    continue;
                }
            }

            json.put(entry.getKey(), entry.getValue());
        }

        return json;
    }

    // FIXME: Apply this to the other dialog, scrolling is slow!!
    private static void _showDialog(final ActivityWithDialog activity, final OptionsView layout, final AlertDialog.Builder builder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = builder.create();
                final Window window = dialog.getWindow();
                if (window != null) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    ViewTreeObserver vto = layout.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                            params.copyFrom(window.getAttributes());
                            params.width = dialog.getWindow().getDecorView().getWidth();
                            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            dialog.getWindow().setAttributes(params);

                            layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                }

                activity.showDialog(builder);
            }
        });
    }

    @NonNull
    public static AlertDialog.Builder getEditOptionDialog(@NonNull Context context, final Option option, final OptionsAdapter adapter) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_edit_option, null, false);

        SuperTextView value = layout.findViewById(R.id.editOptionDialog_value);
        value.setHtml(R.string.currentValue, option.value == null ? "not set" : option.value);

        final EditText edit = layout.findViewById(R.id.editOptionDialog_edit);
        edit.setText(option.value);

        layout.findViewById(R.id.editOptionDialog_multipleHelp)
                .setVisibility(Objects.equals(option.name, "header") || Objects.equals(option.name, "index-out") ? View.VISIBLE : View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout)
                .setTitle(option.name)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        option.setNewValue(edit.getText().toString());
                        if (adapter != null) adapter.optionChanged(option);
                    }
                });

        return builder;
    }
}
