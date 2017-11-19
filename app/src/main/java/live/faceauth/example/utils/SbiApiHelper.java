package live.faceauth.example.utils;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import live.faceauth.example.models.AccountResponse;
import live.faceauth.example.models.InsertResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SbiApiHelper {

  private static final String BASE_URL =
      "http://apiplatformcloudse-gseapicssbisecond-uqlpluu8.srv.ravcloud.com:8001/";

  public static OkHttpClient getHttpClient() {
    return new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
          @Override
          public Response intercept(Chain chain) throws IOException {
            Request.Builder request = chain.request().newBuilder();
            //request.addHeader("Accept", "application/json");
            request.addHeader("api-key", KeyManager.getInstance().getApiKey());
            return chain.proceed(request.build());
          }
        })
        .addNetworkInterceptor(new StethoInterceptor())
        .build();
  }

  private static SbiService service() {
    final OkHttpClient okHttpClient = getHttpClient();

    Gson gson =
        new GsonBuilder().setLenient()
            .registerTypeAdapter(InsertResponse.class, new JsonDeserializer<InsertResponse>() {
              @Override
              public InsertResponse deserialize(JsonElement json, Type typeOfT,
                  JsonDeserializationContext context)
                  throws JsonParseException {
                final String error = json.getAsString();
                InsertResponse response = new InsertResponse();
                response.error = error;

                return response;
              }
            }).create();
    GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);

    final Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .baseUrl(BASE_URL)
        .build();

    return retrofit.create(SbiService.class);
  }

  public static void insertFace(String name, String accountNumber, byte[] data,
      Callback<InsertResponse> callback) {
    service()
        .insertFace(name, accountNumber, getOctetStreamBody(data))
        .enqueue(callback);
  }

  public static void getFaceByTeamAccount(String accountNumber,
      Callback<AccountResponse> callback) {
    service()
        .getFaceByTeamAccount(accountNumber)
        .enqueue(callback);
  }

  public static String getProfileUrl(String accountNumber) {
    return BASE_URL + "FaceRecogInfo/1094404805/" + accountNumber + "/FACE_IMAGE";
  }

  private static RequestBody getOctetStreamBody(byte[] data) {
    return RequestBody
        .create(MediaType.parse("application/octet-stream"), data);
  }
}