package icy.nearad;import java.io.BufferedReader;import java.io.InputStreamReader;import java.util.ArrayList;import java.util.HashMap;import java.util.Map;import java.util.Random;import org.apache.http.HttpEntity;import org.apache.http.HttpResponse;import org.apache.http.client.HttpClient;import org.apache.http.client.methods.HttpPost;import org.apache.http.entity.StringEntity;import org.apache.http.impl.client.DefaultHttpClient;import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import com.baixing.network.api.ApiConfiguration;import com.baixing.network.api.ApiError;import android.annotation.SuppressLint;import android.app.Activity;import android.content.Context;import android.content.Intent;import android.location.Location;import android.location.LocationListener;import android.location.LocationManager;import android.net.wifi.WifiInfo;import android.net.wifi.WifiManager;import android.os.Bundle;import android.os.StrictMode;import android.text.TextUtils;import android.util.Log;import android.widget.Toast;import icy.baixing.api.Api.ApiCallback;import icy.baixing.api.ApiAd;import icy.baixing.entity.Ad;import icy.baixing.entity.AdList;import icy.baixing.entity.Ad.EDATAKEYS;import icy.baixing.util.MyNetworkUtil;/** *  * @author zhaibingjie * */public class SplashCover extends Activity {	private final static int REQUEST_DISTANCE = 5000;	private static double myLat, myLng;	private boolean flag;		public static double getLat() {		return myLat;	}	public static double getLng() {		return myLng;	}	public void onCreate(Bundle savedInstanceState){		super.onCreate(savedInstanceState);		ApiConfiguration.config("www.baixing.com", null, "api_androidbaixing",                "c6dd9d408c0bcbeda381d42955e08a3f", "390", "89aece6589302559");				setContentView(R.layout.splash_mono);		int resource = choosePhoto();		findViewById(R.id.id_splash_cover).setBackgroundResource(resource);				//getAdsByNetState();		requestNetworkLocation();		Toast.makeText(this, "若长时间未响应，请打开gps", Toast.LENGTH_LONG).show();	}		private void getAdsByNetState() {		boolean wifiConnect = MyNetworkUtil.isWifi(this);		if (wifiConnect) {			requestNetworkLocation();			//requestWIFILocation();			//getNearAds(myLat, myLng, 1000);//预加载1000m			return;		}		boolean gpsConnect = MyNetworkUtil.isGpsEnabled(this);		if (gpsConnect) {			requestGPSLocation();//在listener中获取			return;		}		boolean g3Connect = MyNetworkUtil.is3G(this);		if (g3Connect) {			requestNetworkLocation();			return;		}	}	/**	 * {"wifi_towers":[{"mac_address":"00:23:76:AC:41:5D","ssid":"Aspire-NETGEAR"}],"host":"maps.google.com","version":"1.1.0"}	 * {"location":{"latitude":23.129075,"longitude":113.264423,"accuracy":140000.0},"access_token":"2:WRr36ynOz_d9mbw5:pRErDAmJXI8l76MU"}	 */	private void requestWIFILocation() {		/*if (android.os.Build.VERSION.SDK_INT > 9) {		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();		    StrictMode.setThreadPolicy(policy);		    Log.e("", "mode requestWIFILocation");		}*/		WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);		WifiInfo info = wifiMan.getConnectionInfo();		String mac = info.getMacAddress();		String ssid = info.getSSID();		JSONObject wifi = new JSONObject();		try {			wifi.put("mac_address", mac);			wifi.put("ssid", ssid);		} catch (JSONException e) {			e.printStackTrace();		}		JSONArray array = new JSONArray();		array.put(wifi);		JSONObject object = createJSONObject("wifi_towers", array);		requestLocation(object);	}		private void requestLocation(JSONObject object) {		Log.e("", "requestLocation: " + object.toString());		HttpClient client = new DefaultHttpClient();		HttpPost post = new HttpPost("http://www.google.com/loc/json");		try {			StringEntity entity = new StringEntity(object.toString());			post.setEntity(entity);			HttpResponse response = client.execute(post);			HttpEntity entity2 = response.getEntity();			BufferedReader reader = new BufferedReader(new InputStreamReader(entity2.getContent()));			StringBuffer buffer = new StringBuffer();			String result = reader.readLine();			while (result != null) {				buffer.append(result);				result = reader.readLine();				Log.e("", "result: " + result);			}			Log.e("", "result: " + result);		} catch (Exception e) {			Log.e("", "except:" + e.toString());		}	}		private JSONObject createJSONObject(String arrayName, JSONArray array) {        JSONObject object = new JSONObject();        try {            object.put("version", "1.1.0");            object.put("host", "maps.google.com");            object.put(arrayName, array);        } catch (JSONException e) {        	Log.e("", "" + e.getMessage().toString());        }        return object;    }		private LocationListener listener = new LocationListener() {		@Override		public void onStatusChanged(String provider, int status, Bundle extras) {		}				@Override		public void onProviderEnabled(String provider) {					}				@Override		public void onProviderDisabled(String provider) {		}				@Override		public void onLocationChanged(Location location) {			Log.e("", "location changed");			if (!flag) {//只有第一次 titleList.size() == 0 && 				flag = true;				myLat = location.getLatitude();				myLng = location.getLongitude();				getNearAds(myLat, myLng, REQUEST_DISTANCE);			}		}	};	private LocationListener gpsListener = new LocationListener() {		@Override		public void onStatusChanged(String provider, int status, Bundle extras) {		}				@Override		public void onProviderEnabled(String provider) {					}				@Override		public void onProviderDisabled(String provider) {		}				@Override		public void onLocationChanged(Location location) {			Log.e("", "gps location changed");			if (!flag) {				flag = true;				myLat = location.getLatitude();				myLng = location.getLongitude();				getNearAds(myLat, myLng, REQUEST_DISTANCE);			}		}	};	private void requestGPSLocation() {		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gpsListener);	}	private void requestNetworkLocation() {		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000*60, 100, listener);		            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);             if(location != null){                 myLat = location.getLatitude(); //经度                 myLng = location.getLongitude(); //纬度            flag = true;            getNearAds(myLat, myLng, REQUEST_DISTANCE);            return;        }        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);        if(location != null){           	Log.e("", "last gps");            myLat = location.getLatitude(); //经度                 myLng = location.getLongitude(); //纬度            flag = true;            getNearAds(myLat, myLng, REQUEST_DISTANCE);            return;        }        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, listener); 		requestGPSLocation();	}		//获取网络数据	public synchronized void getNearAds(double latitude, double longitude, int distance) {		Log.e("", "lat: lng: " + latitude + longitude);		String latKey = "coordinate[lat]";		String lngKey = "coordinate[lng]";		String disKey = "coordinate[distance]";		Map<String, String> coordinateParams = new HashMap<String, String>();		coordinateParams.put(latKey, String.valueOf(latitude));		coordinateParams.put(lngKey, String.valueOf(longitude));		coordinateParams.put(disKey, String.valueOf(distance));		ApiAd.getAdsByGraph(this, new ApiCallback() {						@Override			public void handleSuccess(String apiName, Object result) {				//not ui thread				AdList adList = (AdList) result;				ArrayList<Ad> arrayList = new ArrayList<Ad>();				arrayList = (ArrayList<Ad>) adList.getData();				for (int i = 0; i < arrayList.size(); i++) {					Ad detail = arrayList.get(i);					if (TextUtils.isEmpty(detail.getValueByKey(EDATAKEYS.EDATAKEYS_LAT)) || TextUtils.isEmpty(detail.getValueByKey(EDATAKEYS.EDATAKEYS_LON))) {						Log.e("lon lat:", "empty");						continue;					}					int topMargin = (int) (Math.random() * 400);					detail.setTopMargin(topMargin);					double adLat = Double.parseDouble(detail.getValueByKey(EDATAKEYS.EDATAKEYS_LAT));					double adLon = Double.parseDouble(detail.getValueByKey(EDATAKEYS.EDATAKEYS_LON));					double degree = Math.atan2(adLon - myLng, adLat - myLat) * 180 / Math.PI;					//分三种情况					double adDegree;					if (degree <= 0) {						adDegree = 90 + Math.abs(degree);					} else if (degree <= 90 && degree > 0) {						adDegree = 90 - degree;					} else {						adDegree = 270 + (180 - degree);					}					detail.setDegree(adDegree);					double distance = getDistance(adLat, adLon);					detail.setDistance(distance * 800);					ads.add(detail);				}				startTextActivity();			}			@Override			public void handleFail(String apiName, ApiError error) {				Log.e("", "fail: " + error.getMsg().toString());			}		}, "ershou", coordinateParams, 60);	}	public static ArrayList<Ad> ads = new ArrayList<Ad>();	public static ArrayList<Ad> getAdsList() {		return ads;	}	public int choosePhoto() {		Random random=  new Random();		int i = random.nextInt(10) + 1;		switch (i) {		case 1:			return R.drawable.p1;		case 2:			return R.drawable.p2;		case 3:			return R.drawable.p3;		case 4:			return R.drawable.p4;		case 5:			return R.drawable.p5;		case 6:			return R.drawable.p6;		case 7:			return R.drawable.p7;		case 8:			return R.drawable.p8;		case 9:			return R.drawable.p9;		case 10:			return R.drawable.p10;		default:			return R.drawable.p1;		}	}	private void startMainActivity() {		Intent intent = new Intent();		//intent.setClass(SplashCover.this, MainActivity.class);		startActivity(intent);		finish();	}	private void startTextActivity() {		Intent intent = new Intent();		intent.setClass(SplashCover.this, TextActivity.class);		startActivity(intent);		finish();	}	private void startMainActivityByIntent(Intent intent) {		//intent.setClass(SplashCover.this, MainActivity.class);		startActivity(intent);		finish();	}		private final double EARTH_RADIUS = 6378.137;	private static double rad(double d)	{	   return d * Math.PI / 180.0;	}	/**	 * 根据经纬度计算两点距离	 */	public double getDistance(double lat2, double lng2)	{	   double radLat1 = rad(myLat);	   double radLat2 = rad(lat2);	   double a = radLat1 - radLat2;	   double b = rad(myLng) - rad(lng2);	   double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + 	    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));	   s = s * EARTH_RADIUS;	   s = Math.round(s * 10000) / 10000;	   return shakeDistance(s);	}	/**加入正负500m以内的抖动 0.5 *///由于得到的数值太平均了	private double shakeDistance(double s) {		double shaked = s - 0.5 + Math.random();		return shaked;	}}
