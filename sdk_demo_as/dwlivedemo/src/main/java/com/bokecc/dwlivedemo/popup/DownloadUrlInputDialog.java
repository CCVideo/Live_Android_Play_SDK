package com.bokecc.dwlivedemo.popup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.dwlivedemo.R;


/**
 * Created by liufh on 2017/2/22.
 */

public class DownloadUrlInputDialog extends DialogFragment {


    private TextView addNewUrl;
    private EditText urlInput;

    private AddUrlListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.add_download_layout, container);
        addNewUrl = view.findViewById(R.id.id_add_new_url);
        urlInput = view.findViewById(R.id.id_url_input);

        addNewUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = urlInput.getText().toString().trim();
                if (url.startsWith("http") && url.endsWith("ccr")) {
                    urlInput.setText("");
                    if (mListener != null) {
                        mListener.onUrlAdd(url);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "请输入正确的url", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }


    public void setAddUrlListener(AddUrlListener l) {
        this.mListener = l;
    }

    public interface AddUrlListener {
        void onUrlAdd(String url);
    }
}
