package com.example.airqual;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class PollenItemAdapter extends ArrayAdapter<Pollen> {
    private final OnPollenItemClickListener buttonRecListener;
    private Pollen pollen;

    public interface OnPollenItemClickListener {
        void showPollenCardView(Pollen pollen);
    }

    public PollenItemAdapter(Context context, List<Pollen> pollens, OnPollenItemClickListener listener) {
        super(context, 0, pollens);
        buttonRecListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        pollen = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pollen_list_item, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.pollen_name);
        TextView tvCategory = convertView.findViewById(R.id.index_category);
        Button btnShowHealthRecommendation = convertView.findViewById(R.id.btn_recommendation);

        tvName.setText(pollen.getDisplayName());
        tvCategory.setText(pollen.getIndexCategory());

        btnShowHealthRecommendation.setOnClickListener(view -> {
            if (buttonRecListener != null) {
                pollen = getItem(position);
                buttonRecListener.showPollenCardView(pollen);
            }
        });

        return convertView;
    }
}