package com.example.zhuyixin.myvideoplayer;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class MediaPlaylistFragment extends Fragment{
    private static final String TAG = "TAG" + "MediaPlaylistFrag";
    private ListView mListView;
    private int mPosition;
    private TrackListAdapter mAdapter;
    private ArrayList<Uri> mDataList = new ArrayList<Uri>();
    private boolean mFirst = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.playback_list_layout, container, false);
        mListView = (ListView)view.findViewById(R.id.main_list);
        mListView.setOnItemClickListener(mOnItemClickListener);
        mAdapter = new TrackListAdapter(getActivity());
        mAdapter.setDatalist(mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    public void setDataList(ArrayList<Uri> list) {
        if (list == null) {
            mDataList = new ArrayList<Uri>();
        } else {
            Log.d(TAG, "setDataList=list " + list.size());
            mDataList = list;
        }
        mAdapter.setDatalist(mDataList);
        mAdapter.notifyDataSetChanged();
    }


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            VideoTabActivity activity = (VideoTabActivity)getActivity();
            activity.play(arg2);
        }
    };

    public OnPositionChangedListener mPositionChangedListener = new OnPositionChangedListener() {
        public void onPositionChanged(int position) {
            mPosition = position;
            if (mAdapter != null) {
                mListView.smoothScrollToPosition(position);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public interface OnPositionChangedListener {
        public void onPositionChanged(int position);
    }


    private class TrackListAdapter extends ListBaseAdapter<Uri, TrackListAdapter.ViewHolder> {

        public TrackListAdapter(Context context) {
            super(context, R.layout.playback_list_item);
        }

        @Override
        public ViewHolder getViewHolder() {
            return new ViewHolder();
        }

        @Override
        public void bindView(ViewHolder viewHolder, View convertView) {
            Log.d(TAG, "bindView");
            viewHolder.name = (TextView)convertView.findViewById(R.id.name);
        }

        @Override
        public void setViewContent(ViewHolder viewHolder,
                                   Uri uri, View convertView, int position) {
            if (mPosition == position) {
                convertView.setActivated(true);
            } else {
                convertView.setActivated(false);
            }
            File file = new File(uri.getPath());
            viewHolder.name.setText(getString(R.string.list_position, position + 1) + file.getName());
        }

        public final class ViewHolder {
            TextView name;
        }
    }
}
