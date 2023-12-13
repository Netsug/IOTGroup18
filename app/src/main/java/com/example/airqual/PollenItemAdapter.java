package com.example.airqual;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.airqual.PollenType;

import org.w3c.dom.Text;

import java.util.List;

public class PollenItemAdapter extends ArrayAdapter<PollenType> {

    private OnPollenItemClickListener mListener;
    PollenType pollenType;

    public interface OnPollenItemClickListener {
        void showCardView(PollenType pollenType);
    }

    public PollenItemAdapter(Context context, List<PollenType> pollenTypes, OnPollenItemClickListener listener) {
        super(context, 0, pollenTypes);
        mListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        pollenType = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pollen_list_item, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.pollen_type_name);
        TextView tvCategory = convertView.findViewById(R.id.index_category);
        Button btnShowHealthRecommendation = convertView.findViewById(R.id.btn_recommendation);

        tvName.setText(pollenType.getName());
        tvCategory.setText(pollenType.getCategory());

        btnShowHealthRecommendation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    pollenType = getItem(position);
                    mListener.showCardView(pollenType);
                }

            }
        });

        // Return the completed view to render on screen
        return convertView;
    }


}
