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

public class PollutantItemAdapter extends ArrayAdapter<Pollutant> {

    private TextView tvTitle;
    private OnPollutantItemClickListener mListener;
    Pollutant pollutant;

    public interface OnPollutantItemClickListener {
        void showPollutantCardView(Pollutant pollutant);
    }

    public PollutantItemAdapter(Context context, List<Pollutant> pollutants, OnPollutantItemClickListener listener) {
        super(context, 0, pollutants);
        mListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        pollutant = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.air_pollutant_list_item, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.pollutant_name);
        TextView tvConcentrationUnitValue = convertView.findViewById(R.id.pollutant_value_unit);

        Button btnShowInfo = convertView.findViewById(R.id.btn_pollutant_info);

        tvName.setText(pollutant.getName());
        tvConcentrationUnitValue.setText(pollutant.getConcentrationValue() + ": " + pollutant.getConcentrationUnit());

        btnShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.showPollutantCardView(pollutant);
                }
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }


}
