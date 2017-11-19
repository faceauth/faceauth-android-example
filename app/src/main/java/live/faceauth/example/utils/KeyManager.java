package live.faceauth.example.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class KeyManager {

  private static final String TAG = "KEY_MANAGER";
  private static final String SBI_API_KEY = "sbi-api-key";
  private static KeyManager sInstance;

  private static Object sLock = new Object();

  private String mApiKey;


  public static KeyManager getInstance() {
    if (sInstance == null) {
      synchronized (sLock) {
        sInstance = new KeyManager();
      }
    }
    return sInstance;
  }

  private static String readApiKey(Context context) {
    try {
      final ApplicationInfo ai = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

      return ai.metaData.getString(SBI_API_KEY);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
    } catch (NullPointerException e) {
      Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
    }
    return "";
  }

  public void initialize(Context context) {
    final String apiKey = readApiKey(context);

    if (apiKey != null && !apiKey.isEmpty()) {
      Log.d(TAG, "FaceAuth initialization successful.");
    } else {
      Log.d(TAG, "Failed to read api key. Make sure to add it in Android Manifest as meta-data.");
    }
    this.mApiKey = apiKey;
  }

  public String getApiKey() {
    return mApiKey;
  }

}
