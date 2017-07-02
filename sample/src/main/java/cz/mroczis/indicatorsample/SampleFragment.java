package cz.mroczis.indicatorsample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment in view pager which shows its position in pager with correction for humans (:
 * Created by Michal on 29.06.17.
 */

public class SampleFragment extends Fragment {

    private static final String ARG_PAGE_NUMBER = "page_number";

    public static SampleFragment getInstance(int pageNumber) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_NUMBER, pageNumber);

        SampleFragment fragment = new SampleFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView pageNum = view.findViewById(R.id.page_number);
        pageNum.setText(String.format(getString(R.string.page_number), getArguments().getInt(ARG_PAGE_NUMBER, 0)));
    }
}
