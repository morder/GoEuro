package com.test.yanis.goeuro;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Yanis on 09.06.2016.
 */
public class MyAutoAdapter extends BaseAdapter implements Filterable {

    private final GoEuroApi mApi;
    private List<SuggestData> mData = Collections.emptyList();
    private Location mLocation = null;

    public MyAutoAdapter(GoEuroApi api){
        mApi = api;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(mData.get(position).fullName);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new MyFilter();
    }

    public void setCurrentLocation(Location location){
        mLocation = location;
        sortData();
    }

    private void sortData(){
        if (mLocation != null && mData != null && !mData.isEmpty()) {
            Collections.sort(mData, new Comparator<SuggestData>() {
                @Override
                public int compare(SuggestData lhs, SuggestData rhs) {
                    Location location = new Location("");
                    location.setLatitude(lhs.geo_position.latitude);
                    location.setLongitude(lhs.geo_position.longitude);
                    float lDistance = mLocation.distanceTo(location);

                    location.setLatitude(rhs.geo_position.latitude);
                    location.setLongitude(rhs.geo_position.longitude);
                    float rDistance = mLocation.distanceTo(location);

                    return lDistance < rDistance ? -1 : lDistance == rDistance ? 0 : 1;
                }
            });
        }
    }

    class MyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            filterResults.count = 0;
            filterResults.values = Collections.EMPTY_LIST;

            if (constraint != null) {
                Response<ArrayList<SuggestData>> response;
                Call<ArrayList<SuggestData>> call = mApi.suggest(Locale.getDefault().getDisplayLanguage(), constraint.toString());
                try {
                    response = call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return new FilterResults();
                }

                if (response.isSuccessful()) {
                    filterResults = new FilterResults();
                    filterResults.count = response.body().size();
                    filterResults.values = response.body();
                }
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mData = (List<SuggestData>) results.values;
            sortData();
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    private class ViewHolder {
        TextView text;
    }
}
