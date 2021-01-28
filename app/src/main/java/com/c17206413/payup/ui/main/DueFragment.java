package com.c17206413.payup.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.c17206413.payup.R;

public class DueFragment extends Fragment {

    public static DueFragment newInstance() {
        DueFragment fragment = new DueFragment();

        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_due, container, false);
        //final TextView textView = root.findViewById(R.id.section_label);
        return root;
    }
}