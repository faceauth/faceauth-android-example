package live.faceauth.example;

import android.app.Application;
import com.facebook.stetho.Stetho;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import live.faceauth.example.utils.KeyManager;
import live.faceauth.example.utils.SbiApiHelper;
import live.faceauth.sdk.FaceAuth;

public class AapkaBankApplication extends Application {

  public void onCreate() {
    super.onCreate();
    Stetho.initializeWithDefaults(this);
    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    KeyManager.getInstance().initialize(this);

    FaceAuth.getInstance().initialize(this);

    final Picasso picasso = new Picasso.Builder(this)
        .downloader(new OkHttp3Downloader(SbiApiHelper.getHttpClient()))
        .build();
    Picasso.setSingletonInstance(picasso);
  }
}
