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

/**
 * A placeholder fragment containing a simple view.
 */
public class DueFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static DueFragment newInstance(int index) {
        DueFragment fragment = new DueFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_due, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        return root;
    }
}