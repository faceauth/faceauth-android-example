package live.faceauth.example;

import android.content.Context;
import android.content.SharedPreferences;

public class DB {

  private static final String DB_NAME = "FaceAuthDB";

  private static final String LAST_LOGIN_ACCOUNT = "LAST_LOGIN_ACCOUNT";

  private static SharedPreferences get(Context context) {
    return context.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE);
  }

  static void setLastLoginAccount(Context context, String accountNumber) {
    get(context).edit().putString(LAST_LOGIN_ACCOUNT, accountNumber).apply();
  }

  static String getLastLoginAccount(Context context) {
    return get(context).getString(LAST_LOGIN_ACCOUNT, null);
  }
}
