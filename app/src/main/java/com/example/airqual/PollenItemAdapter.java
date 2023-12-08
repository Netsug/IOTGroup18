package com.example.airqual;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.airqual.PollenType;

import java.util.List;

public class PollenItemAdapter extends ArrayAdapter<PollenType> {

    public PollenItemAdapter(Context context, List<PollenType> pollenTypes) {
        super(context, 0, pollenTypes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PollenType pollenType = getItem(position);

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
                displayCardView(pollenType.getHealthRecommendations());
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void displayCardView(String healthRecommendation) {
        // Implement the logic to display the CardView here
        // This might involve updating a CardView in your layout,
        // or opening a new fragment or activity, depending on your design
    }
}
