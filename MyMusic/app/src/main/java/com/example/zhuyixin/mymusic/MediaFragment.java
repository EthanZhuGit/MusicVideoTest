
package com.example.zhuyixin.mymusic;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaFragment extends Fragment {

    private static final String TAG = "TAG " + MediaFragment.class.getSimpleName();

    //    private GroupListAdapter mArrayAdapter;
    private MusicAdapter mArrayAdapter;
    private Store mStore;
    private ListView mListView;
    private View mView;

    private MusicBrowserActivity musicBrowserActivity;

    private ArrayList<Uri> list = null;

    public MediaFragment() {

    }
//    public MediaFragment(Store store) {
//        mStore = store;
//    }

    public static MediaFragment newInstance(String uriString, boolean mounted) {
        MediaFragment mediaFragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uriString", uriString);
        bundle.putBoolean("mounted", mounted);
        mediaFragment.setArguments(bundle);
        return mediaFragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String uriString = getArguments().getString("uriString");
        boolean mounted = getArguments().getBoolean("mounted");
        Uri uri = Uri.parse(uriString);
        File file = new File(uri.getPath());
        Log.d(TAG, "onCreate: " + uri.toString() + " " + uri.getPath() + mounted);
        mStore = new MediaStoreBase(Uri.parse(uriString), file, mounted);
        MediaModel.getInstance().registerDataChangedListener(mStore, mDataListener);



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MediaModel.getInstance().unregisterDataChangedListener(mStore, mDataListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "=onCreateView=");
        mView = inflater.inflate(R.layout.video_group_list_layout, container, false);
        mListView = (ListView) mView.findViewById(R.id.main_list);
        mListView.setOnItemClickListener(mOnItemClickListener);

        if (list != null) {
            mArrayAdapter=new MusicAdapter(getContext(),R.layout.music_item,list);
            mListView.setAdapter(mArrayAdapter);
        }
        list = MediaModel.getInstance().startAllVideoLoader(mStore, mListener);
        if (list == null) {
            mView.findViewById(R.id.all_list).setVisibility(View.GONE);
        } else if (list.size() == 0) {
            mView.findViewById(R.id.all_list).setVisibility(View.GONE);
        } else {
            if (mArrayAdapter==null) {
                mArrayAdapter=new MusicAdapter(getContext(),R.layout.music_item,list);
                mListView.setAdapter(mArrayAdapter);
            }else {
                mArrayAdapter.notifyDataSetChanged();
            }
        }
        return mView;
    }

    private void updateDataList(Store store, ArrayList<Uri> dataList) {
            list=dataList;
        if (mArrayAdapter==null){
            mArrayAdapter=new MusicAdapter(getContext(),R.layout.music_item,list);
            mListView.setAdapter(mArrayAdapter);
        }else {
            mArrayAdapter.notifyDataSetChanged();
        }
            Log.i(TAG, "List= " + list.size());
            if (list.size() == 0) {
                mView.findViewById(R.id.all_list).setVisibility(View.GONE);
            } else {
                mView.findViewById(R.id.all_list).setVisibility(View.VISIBLE);
            }
    }

    private MediaModel.IDataChangedListener mDataListener = new MediaModel.IDataChangedListener() {
        @Override
        public void onDataChanged(Store store, ArrayList<Uri> dataList) {
            updateDataList(store, dataList);
        }
    };

    private MediaModel.OnAllVideoLoadListener mListener = new MediaModel.OnAllVideoLoadListener() {
        @Override
        public void onLoadFinished(Store store, ArrayList<Uri> dataList) {
            updateDataList(store, dataList);
        }
    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            MusicBrowserActivity activity = (MusicBrowserActivity) getActivity();
            MediaModel.getInstance().setPlayingIndex(arg2);
            MediaModel.getInstance().setPlayingStore(mStore);
            Log.d(TAG, "onItemClick: " +arg2);
            activity.switchToPlay(mStore, arg2, false);
        }
    };


    private class MusicAdapter extends ArrayAdapter<Uri> {
        private int resourceId;

        public MusicAdapter(Context context, int textViewResourceId, List<Uri> objects) {
            super(context,textViewResourceId,objects);
            resourceId=textViewResourceId;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Uri uri = getItem(position);
            View view;
            class ViewHolder{
                TextView textView;
            }
            ViewHolder viewHolder;
            if (convertView == null) {
                view=LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
                viewHolder=new ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.txt_title);
                view.setTag(viewHolder);
            }else {
                view=convertView;
                viewHolder=(ViewHolder)view.getTag();
            }
            viewHolder.textView.setText(uri.getPath().substring(uri.getPath().lastIndexOf("/")+1));
            return view;
        }
    }
}
