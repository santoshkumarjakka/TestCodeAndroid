package com.splice.test;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.splice.adapter.GridAdapter;
import com.splice.listener.OnLoadMoreListener;
import com.splice.listener.RecyclerViewLoadMoreScroll;
import com.splice.metadata.ApiUrl;
import com.splice.model.PhotoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.splice.util.Constant.VIEW_TYPE_ITEM;
import static com.splice.util.Constant.VIEW_TYPE_LOADING;

public class MainActivity extends AppCompatActivity {

  private RecyclerView rvPhotoListView;
  private List<PhotoModel> mUsers = new ArrayList<>();
  private GridAdapter mUserAdapter;
  private int numberofpages = 30, currentpage = 1, totalpages;
  public static final int CONNECTION_TIMEOUT = 10000;
  public static final int READ_TIMEOUT = 15000;

  private OnLoadMoreListener mOnLoadMoreListener;

  private boolean isLoading;
  private int visibleThreshold = 5;
  private int lastVisibleItem, totalItemCount;
  private SwipeRefreshLayout swipeRefresh;
  private RecyclerViewLoadMoreScroll scrollListener;
  private GridLayoutManager gridLayoutManager;
  private GridAdapter gridAdapter;
  private ProgressBar progressBar;
  private FrameLayout frame;
  private ProgressBar loadingBar;
  private ArrayList<PhotoModel> searchPhotoList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    Initialize();
  }


  private void loadMorePhotos(final int currentpage) {
    Log.i("urls", ApiUrl.getPhotoAPIURL(numberofpages, currentpage));
    loadingBar.setVisibility(View.VISIBLE);
    AndroidNetworking.get(ApiUrl.getPhotoAPIURL(numberofpages, currentpage))
      .setTag("test")
      .setPriority(Priority.HIGH)
      .build()
      .getAsJSONObject(new JSONObjectRequestListener() {
        @Override
        public void onResponse(JSONObject response) throws JSONException {

          JSONObject photos = response.getJSONObject("photos");
          int page = photos.getInt("page");
          int pages = photos.getInt("pages");
          totalpages = pages;
          JSONArray jsonArray = photos.getJSONArray("photo");
          for (int i = 0; i < jsonArray.length(); i++) {
            int farm_id = jsonArray.getJSONObject(i).getInt("farm");
            int server_id = jsonArray.getJSONObject(i).getInt("server");
            String photo_id = jsonArray.getJSONObject(i).getString("id");
            String secret = jsonArray.getJSONObject(i).getString("secret");
            String title = jsonArray.getJSONObject(i).getString("title");
            String image = "https://farm" + farm_id + ".staticflickr.com/" + server_id + "/" + photo_id + "_" + secret + "_m.jpg";
            PhotoModel photoModel = new PhotoModel(photo_id, secret, farm_id, title, image);
            mUsers.add(photoModel);
          }
          if (mUsers.size() != 0) {

            loadingBar.setVisibility(View.GONE);
            setAdapter();
          }


        }

        @Override
        public void onError(ANError anError) {
          loadingBar.setVisibility(View.GONE);
          Snackbar.make(frame, "OOps! Something went wrong,Please Try Again Later", BaseTransientBottomBar.LENGTH_INDEFINITE)
            .setAction("Retry", new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                loadingBar.setVisibility(View.GONE);
                loadMorePhotos(currentpage);
              }
            }).show();

        }
      });
  }


  @Override
  protected void onResume() {
    super.onResume();
    swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            setAdapter();
            swipeRefresh.setRefreshing(false);
          }
        }, 5000);
      }
    });
  }

  private void Initialize() {
    searchPhotoList = new ArrayList<>();
    loadingBar = (ProgressBar) findViewById(R.id.progress);
    frame = (FrameLayout) findViewById(R.id.frame);
    swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
    rvPhotoListView = (RecyclerView) findViewById(R.id.rvPhotoListView);
    rvPhotoListView.setHasFixedSize(true);

    gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
    rvPhotoListView.setLayoutManager(gridLayoutManager);
    loadMorePhotos(currentpage);

//    setAdapter();
    scrollListener = new RecyclerViewLoadMoreScroll(gridLayoutManager);
    scrollListener.setOnLoadMoreListener(new OnLoadMoreListener() {
      @Override
      public void onLoadMoreList() {
        LoadMoreData();
      }
    });

    rvPhotoListView.addOnScrollListener(scrollListener);
    SearchView searchView = (SearchView) findViewById(R.id.searchView);
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (newText.length() != 0) {
          if (searchPhotoList.size() != 0) {
            rvPhotoListView.setAdapter(new GridAdapter(filter(searchPhotoList, newText), MainActivity.this));
          } else {
            new AsyncFetch().execute();
          }
        } else
        {
          gridAdapter = new GridAdapter(mUsers, MainActivity.this);
          rvPhotoListView.setAdapter(gridAdapter);
        }

        return false;
      }
    });
  }

  private static List<PhotoModel> filter(List<PhotoModel> models, String query) {
    final String lowerCaseQuery = query.toLowerCase();

    final List<PhotoModel> filteredModelList = new ArrayList<>();
    for (PhotoModel model : models) {
      final String text = model.getPhoto_tittle().toLowerCase();
      if (text.contains(lowerCaseQuery)) {
        filteredModelList.add(model);
      }
    }
    return filteredModelList;
  }

  private void setAdapter() {
    loadingBar.setVisibility(View.GONE);

    gridAdapter = new GridAdapter(mUsers, MainActivity.this);
    rvPhotoListView.setAdapter(gridAdapter);

    rvPhotoListView.addItemDecoration(new SpacesItemDecoration(4));
    gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        switch (gridAdapter.getItemViewType(position)) {
          case VIEW_TYPE_ITEM:
            return 1;
          case VIEW_TYPE_LOADING:
            return 2; //number of columns of the grid
          default:
            return -1;
        }
      }
    });
  }

  private void LoadMoreData() {
    gridAdapter.addLoadingView();
    currentpage = currentpage + 1;
    if (currentpage <= totalpages) {
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          loadMorePhotos(currentpage);
          gridAdapter.removeLoadingView();
          gridAdapter.addData(mUsers);
//          gridAdapter.notifyDataSetChanged();
          scrollListener.setLoaded();
        }
      }, 5000);

    } else {
      gridAdapter.removeLoadingView();
    }
  }

  public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
      this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
      outRect.left = space;
      outRect.right = space;
      outRect.bottom = space;
      outRect.top = space;
    }
  }

  private class AsyncFetch extends AsyncTask<String, String, String> {

    ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
    HttpURLConnection conn;
    URL url = null;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      //this method will be running on UI thread
      pdLoading.setMessage("\tLoading...");
      pdLoading.setCancelable(false);
      pdLoading.show();

    }

    @Override
    protected String doInBackground(String... params) {
      try {

        // Enter URL address where your php file resides or your JSON file address
        Log.i("valiesss", ApiUrl.getPhotoAPIURL(100, 1));
        url = new URL(ApiUrl.getPhotoAPIURL(100, 1));

      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return e.toString();
      }
      try {

        // Setup HttpURLConnection class to send and receive data from php and mysql
        conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setRequestMethod("GET");

        // setDoOutput to true as we receive data
        conn.setDoOutput(true);
        conn.connect();

      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
        return e1.toString();
      }

      try {

        int response_code = conn.getResponseCode();

        // Check if successful connection made
        if (response_code == HttpURLConnection.HTTP_OK) {

          // Read data sent from server
          InputStream input = conn.getInputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(input));
          StringBuilder result = new StringBuilder();
          String line;

          while ((line = reader.readLine()) != null) {
            result.append(line);
          }

          // Pass data to onPostExecute method
          return (result.toString());

        } else {
          return ("Connection error");
        }

      } catch (IOException e) {
        e.printStackTrace();
        return e.toString();
      } finally {
        conn.disconnect();
      }


    }

    @Override
    protected void onPostExecute(String result) {

      //this method will be running on UI thread

      pdLoading.dismiss();


      if (result.equals("no rows")) {

        // Do some action if no data from database

      } else {

        try {

          JSONObject response = new JSONObject(result);

          JSONObject photos = response.getJSONObject("photos");
          int page = photos.getInt("page");
          int pages = photos.getInt("pages");
          totalpages = pages;
          JSONArray jsonArray = photos.getJSONArray("photo");
          for (int i = 0; i < jsonArray.length(); i++) {
            int farm_id = jsonArray.getJSONObject(i).getInt("farm");
            int server_id = jsonArray.getJSONObject(i).getInt("server");
            String photo_id = jsonArray.getJSONObject(i).getString("id");
            String secret = jsonArray.getJSONObject(i).getString("secret");
            String title = jsonArray.getJSONObject(i).getString("title");
            String image = "https://farm" + farm_id + ".staticflickr.com/" + server_id + "/" + photo_id + "_" + secret + "_m.jpg";
            PhotoModel photoModel = new PhotoModel(photo_id, secret, farm_id, title, image);
            searchPhotoList.add(photoModel);
          }

        } catch (JSONException e) {

          Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
          Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_LONG).show();
        }

      }

    }
  }

}

