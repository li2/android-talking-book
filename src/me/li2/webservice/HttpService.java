package me.li2.webservice;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HttpService {
  
  private static final int CONNECTION_TIMEOUT = 20000; // 20 seconds
  private static final int SOCKET_TIMEOUT = 20000; // 20 seconds
  
  private String mWebSiteURL = null;
  private HttpClient mHttpClient = null;
  
  public HttpService(String websiteURL) {
    mWebSiteURL = websiteURL;
    
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
    HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
    
    mHttpClient = new DefaultHttpClient(params);
  }
  
  private String getEncodedParams(Map<String, String> params) {
    String result = "";
    try {
      for (Entry<String, String> param : params.entrySet()) {
        String paramString = param.getKey() + "=" +URLEncoder.encode(param.getValue(), "UTF-8");
        if (result.isEmpty()) {
          result += paramString;
        } else {
          result += "&"+paramString;
        }
      }
    } catch (UnsupportedEncodingException e) {
      result = "";
      e.printStackTrace();
    }
    return result;
  }
  
  public interface DataGotCallback {
    public void onGetData(boolean success, byte[] data, JSONObject errorJSON);
  };
  
  public interface DataArrayGotCallback {
    public void onGetDataArray(boolean success, byte[] data, JSONArray errorJSONArray);
  };
  
  public void webGetData(final String path, final Map<String, String> params, final DataGotCallback callback) {
    new Thread() {
      @Override
      public void run() {
        String queryString = getEncodedParams(params);
        String requestURL = mWebSiteURL+"/"+path+"?"+queryString;
        HttpGet httpGet = new HttpGet(requestURL);
        boolean success = false;
        byte[] data = null;
        JSONObject errorJSON = null;
        try {
          HttpResponse response = mHttpClient.execute(httpGet);
          if (response.getStatusLine().getStatusCode() > 400) {
            String errorContent = EntityUtils.toString(response.getEntity());
            errorJSON = new JSONObject(errorContent);
          } else {
            success = true;
            data = EntityUtils.toByteArray(response.getEntity());
          }
        } catch (Exception e) {
          success = false;
          data = null;
          errorJSON = null;
          e.printStackTrace();
        }
        if (callback != null) {
          callback.onGetData(success, data, errorJSON);
        }
      }
    }.start();
  }
  
  public void webGetData(final String path, final Map<String, String> params, final DataArrayGotCallback callback) {
    new Thread() {
      @Override
      public void run() {
        String queryString = getEncodedParams(params);
        String requestURL = mWebSiteURL+"/"+path+"?"+queryString;
        HttpGet httpGet = new HttpGet(requestURL);
        boolean success = false;
        byte[] data = null;
        JSONArray errorJSONArray = null;
        try {
          HttpResponse response = mHttpClient.execute(httpGet);
          if (response.getStatusLine().getStatusCode() > 400) {
            String errorContent = EntityUtils.toString(response.getEntity());
            errorJSONArray = new JSONArray(errorContent);
          } else {
            success = true;
            data = EntityUtils.toByteArray(response.getEntity());
          }
        } catch (Exception e) {
          success = false;
          data = null;
          errorJSONArray = null;
          e.printStackTrace();
        }
        if (callback != null) {
          callback.onGetDataArray(success, data, errorJSONArray);
        }
      }
    }.start();
  }
  
  public interface JSONGotCallback {
    public void onGetJSON(boolean success, JSONObject resultJSON);
  };

  public interface JSONArrayGotCallback {
    public void onGetJSONArray(boolean success, JSONArray resultJSONArray);
  };
  
  public interface JSONRawStringGotCallback {
    public void onGetJSONRawString(boolean success, String resultJSONRawString);
  }
  
  public void webGet(final String path, final Map<String, String> params, final JSONGotCallback callback) {
    webGetData(path, params, new DataGotCallback(){
      @Override
      public void onGetData(boolean success, byte[] data, JSONObject errorJSON) {
        JSONObject result = errorJSON;
        if (errorJSON == null) {
          try {
            if (data != null) {
              result = new JSONObject(new String(data, Charset.forName("UTF-8")));
            }
          } catch (JSONException e) {
            result = null;
            e.printStackTrace();
          }
        }
        if (callback != null) {
          callback.onGetJSON(success, result);
        }
      }
    });
  }
  
  public void webGet(final String path, final Map<String, String> params, final JSONArrayGotCallback callback) {
    webGetData(path, params, new DataArrayGotCallback(){
      @Override
      public void onGetDataArray(boolean success, byte[] data, JSONArray errorJSONArray) {
        JSONArray result = errorJSONArray;
        if (errorJSONArray == null) {
          try {
            if (data != null) {
              result = new JSONArray(new String(data, Charset.forName("UTF-8")));
            }
          } catch (JSONException e) {
            result = null;
            e.printStackTrace();
          }
        }
        if (callback != null) {
          callback.onGetJSONArray(success, result);
        }
      }
    });
  }
  
  public void webGet(final String path, final Map<String, String> params, final JSONRawStringGotCallback callback) {
    webGetData(path, params, new DataGotCallback() {
      @Override
      public void onGetData(boolean success, byte[] data, JSONObject errorJSON) {
        String result = null;
        if (data != null) {
          result = new String(data, Charset.forName("UTF-8"));
        }
        if (callback != null) {
          callback.onGetJSONRawString(success, result);
        }
      }
    });
  }
}