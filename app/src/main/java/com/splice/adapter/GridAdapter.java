package com.splice.adapter;

/*
 * MIT License
 *
 * Copyright (c) 2017 Piyush
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.splice.model.PhotoModel;
import com.splice.test.MainActivity;
import com.splice.test.R;
import com.splice.util.Constant;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Piyush on 1/10/2017.
 */

public class GridAdapter extends RecyclerView.Adapter implements Filterable {

  List<PhotoModel> dataViews;
  List<PhotoModel> filteredList;
  private Context context;
  private CustomFilter mFilter;

  public GridAdapter(List<PhotoModel> dataViews, Context context) {
    this.dataViews = dataViews;
    this.filteredList = dataViews;
    this.context = context;
    mFilter = new CustomFilter(GridAdapter.this);
  }

  public void addData(List<PhotoModel> dataViews) {
    this.dataViews.addAll(dataViews);
    notifyDataSetChanged();
//    notifyDataSetChanged();
  }

  public PhotoModel getItemAtPosition(int position) {
    return dataViews.get(position);
  }

  public void addLoadingView() {
    //add loading item
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        dataViews.add(null);
        notifyItemInserted(dataViews.size() - 1);
      }
    });
  }

  public void removeLoadingView() {
    //Remove loading item
    dataViews.remove(dataViews.size() - 1);
    notifyItemRemoved(dataViews.size());
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == Constant.VIEW_TYPE_ITEM) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_photo_item, parent, false);
      return new GridHolder(view);
    } else if (viewType == Constant.VIEW_TYPE_LOADING) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_spinner, parent, false);
      return new LoadingHolder(view);
    }
    return null;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof GridHolder) {
      final PhotoModel user = dataViews.get(position);
      GridHolder userViewHolder = (GridHolder) holder;
      userViewHolder.tvName.setText(user.getPhoto_tittle());
      Log.i("valueees", user.getPhoto_image());

      Picasso.with(context).load(user.getPhoto_image()).into(userViewHolder.ivImagePhoto);

    } else if (holder instanceof LoadingHolder) {
      LoadingHolder loadingViewHolder = (LoadingHolder) holder;
      loadingViewHolder.progressBar.setIndeterminate(true);
    }

  }

  @Override
  public int getItemCount() {
    return dataViews == null ? 0 : dataViews.size();
  }

  @Override
  public int getItemViewType(int position) {
    return dataViews.get(position) == null ? Constant.VIEW_TYPE_LOADING : Constant.VIEW_TYPE_ITEM;
  }

  @Override
  public Filter getFilter() {
    return mFilter;
  }

  public class GridHolder extends RecyclerView.ViewHolder {
    TextView tvName;
    ImageView ivImagePhoto;

    public GridHolder(View itemView) {
      super(itemView);
      tvName = (TextView) itemView.findViewById(R.id.tvTittle);

      ivImagePhoto = (ImageView) itemView.findViewById(R.id.ivImagePhoto);
    }
  }

  public class LoadingHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;

    public LoadingHolder(View itemView) {
      super(itemView);
      progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
    }
  }

  public class CustomFilter extends Filter {
    private GridAdapter mAdapter;

    private CustomFilter(GridAdapter mAdapter) {
      super();
      this.mAdapter = mAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      filteredList.clear();
      final FilterResults results = new FilterResults();
      if (constraint.length() == 0) {
        filteredList.addAll(dataViews);
      } else {
        final String filterPattern = constraint.toString().toLowerCase().trim();
        for (final PhotoModel photo : dataViews) {
          if (photo.getPhoto_tittle().toLowerCase().startsWith(filterPattern)) {
            filteredList.add(photo);
          }
        }
      }
      results.values = filteredList;
      results.count = filteredList.size();
      return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      ((List<PhotoModel>) results.values).size();
      this.mAdapter.notifyDataSetChanged();
    }
  }
}
