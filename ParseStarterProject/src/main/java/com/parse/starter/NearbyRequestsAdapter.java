package com.parse.starter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class NearbyRequestsAdapter extends BaseAdapter {

    private Context context;
    private List<String> distances;

    public NearbyRequestsAdapter(Context context, List<String> distances) {
        this.context = context;
        this.distances = distances;
    }

    @Override
    public int getCount() {
        if (distances != null) {
            return distances.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return distances.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.nearby_requests_list_item, parent, false);
        }

        TextView distanceTextView = convertView.findViewById(R.id.request_distance_text_view);
        distanceTextView.setText(distances.get(position));

        return convertView;
    }
}