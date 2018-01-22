package com.splice.metadata;


import com.splice.test.BuildConfig;

public class ApiUrl {

  private static String API_AND = "&";
  private static String API_METHOD = "flickr.interestingness.getList" + API_AND;
  private static String API_JSON_FORMAT = "format=";
  private static String API_CALL_BACK = "nojsoncallback=1";
  private static final String DOMAIN = "https://api.flickr.com/services/rest/?method=";

  public static String getPhotoAPIURL(int numberofpages, int currentpage) {
    return ApiUrl.DOMAIN + ApiUrl.API_METHOD + "api_key=" + BuildConfig.APIKEY + ApiUrl.API_AND + "per_page=" + numberofpages + ApiUrl.API_AND + "page=" + currentpage + API_AND + "format=json&nojsoncallback=1";
  }
}
