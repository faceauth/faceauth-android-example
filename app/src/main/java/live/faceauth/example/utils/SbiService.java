package live.faceauth.example.utils;

import live.faceauth.example.models.AccountResponse;
import live.faceauth.example.models.InsertResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SbiService {

  @GET("FaceRecogInfo/1094404805/{accountNum}")
  Call<AccountResponse> getFaceByTeamAccount(@Path("accountNum") String accountNumber);

  @Headers({
      "TEAM_ID: 1094404805",
      "MIME_TYPE: image/jpeg",
  })
  @POST("FaceRecogCreate")
  Call<InsertResponse> insertFace(@Header("NAME") String name, @Header("ACCOUNT_NO") String accountNumber,
      @Body RequestBody body);
}
