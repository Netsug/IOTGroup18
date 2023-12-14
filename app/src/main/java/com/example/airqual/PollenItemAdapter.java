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

    private OnPollenItemClickListener mListener;
    private Pollen pollen;

    public interface OnPollenItemClickListener {
        void showCardView(Pollen pollen);
    }

    public PollenItemAdapter(Context context, List<Pollen> pollens, OnPollenItemClickListener listener) {
        super(context, 0, pollens);
        mListener = listener;
    }

    // PUT ONLY ITEMS IN THAT USER SELECTED, IF ITEM NOT IN RESPONSE, SAY ITS LOW POLLEN LEVEL

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        pollen = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pollen_list_item, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.pollen_name);
        TextView tvCategory = convertView.findViewById(R.id.index_category);

        Button btnShowHealthRecommendation = convertView.findViewById(R.id.btn_recommendation);

        tvName.setText(pollen.getDisplayName());
        tvCategory.setText(pollen.getIndexCategory());

        btnShowHealthRecommendation.setOnClickListener(view -> {
            if (mListener != null) {
                pollen = getItem(position);
                mListener.showCardView(pollen);
            }

        });

        // Return the completed view to render on screen
        return convertView;
    }


}
