package com.claricom.ccid_nfc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class InfoContentFragment extends Fragment {

    public static String tagId="";

    InfoContentFragment(String tag){
        tagId = tag;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_content, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView serialNumber = (TextView)getActivity().findViewById(R.id.textView_serial_number);
        serialNumber.setText(tagId);
    }
}