package com.example.zhuyixin.mymusic;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class BrowserFragment extends Fragment {
    private ListView lvMusic;

    public BrowserFragment() {
        // Required empty public constructor
    }

    public static BrowserFragment newInstance() {
        return new BrowserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.browser_fragment, container, false);
        lvMusic = (ListView) view.findViewById(R.id.lv_music);
        return view;
    }

    public void initLv(List<MusicItem> musicItemList) {
        MusicAdapter adapter = new MusicAdapter(getContext(), R.layout.music_item, musicItemList);
        lvMusic.setAdapter(adapter);
    }

    class MusicAdapter extends ArrayAdapter<MusicItem> {
        private int resourceId;
        public MusicAdapter(Context context,int textViewResourceId,List<MusicItem> objects){
            super(context, textViewResourceId, objects);
            resourceId=textViewResourceId;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MusicItem item = getItem(position);
            View view;
            class ViewHolder{
               private TextView txtTitle;
            }
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                viewHolder=new ViewHolder();
                viewHolder.txtTitle = (TextView) view.findViewById(R.id.txt_title);
                view.setTag(viewHolder);
            }else {
                view=convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.txtTitle.setText(item.getTitle());
            return view;
        }
    }
}
