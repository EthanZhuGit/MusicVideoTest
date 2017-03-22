package com.example.zhuyixin.myvideoplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class ListBaseAdapter<T, V> extends BaseAdapter {
	protected Context mContext;

	private LayoutInflater mInflater;

	private int mItemLayoutResource;

	private ArrayList<T> mDatalist;

	public ListBaseAdapter(Context context, int itemLayoutResource) {
		super();
		mContext = context;
		mItemLayoutResource = itemLayoutResource;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDatalist = new ArrayList<T>();
	}

	@Override
	public int getCount() {
		return mDatalist.size();
	}

	public void setDatalist(ArrayList<T> mDatalist) {
		this.mDatalist = mDatalist;
	}

	@Override
	public T getItem(int paramInt) {
	    if (paramInt >= mDatalist.size()) {
	        return null;
	    } else {
	        return mDatalist.get(paramInt);
	    }
	}

	@Override
	public long getItemId(int paramInt) {
		return paramInt;
	}

	public ArrayList<T> getDatalist() {
		return mDatalist;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		V holder = null;

		T record = getItem(position);
		if (convertView == null || convertView.getTag() == null) {
			convertView = mInflater.inflate(mItemLayoutResource, null);
			holder = getViewHolder();
			bindView(holder, convertView);
			convertView.setTag(holder);
		} else {
			holder = (V) convertView.getTag();
		}
		view = convertView;
		setViewContent(holder, record, convertView, position);

		return view;
	}

	public abstract V getViewHolder();

	public abstract void bindView(V viewHolder, View convertView);

	public abstract void setViewContent(V viewHolder, T record,
                                        View convertView, int position);

}
