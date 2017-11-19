package live.faceauth.example;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.example.models.Account;
import live.faceauth.example.models.InsertResponse;
import live.faceauth.example.utils.SbiApiHelper;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

  public static final String ACCOUNT_NUMBER = "EXTRA_ACCOUNT_NAME";
  private static final String TAG = "DASHBOARD_ACTIVITY";

  @BindView(R.id.profilePicture) ImageView profilePicture;
  @BindView(R.id.account_number) TextView mAccountNumberView;
  @BindView(R.id.account_holder_name) TextView mAccountHolderNameView;
  @BindView(R.id.register_card) CardView mRegisterCard;
  @BindView(R.id.registered_card) CardView mRegisteredCard;

  private ProgressDialog mProgressDialog;
  private String mAccountNumber;
  private DatabaseReference mAccountRef;
  private Account mAccount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage("Loading...");

    ButterKnife.bind(this);
    mAccountNumber = getIntent().getStringExtra(ACCOUNT_NUMBER);
    mAccountNumberView.setText(getString(R.string.account_no, mAccountNumber));

    mAccountRef =
        FirebaseDatabase.getInstance().getReference().child("accounts").child(mAccountNumber);

    DB.setLastLoginAccount(this, mAccountNumber);

    setProfilePic();
    checkIfFaceIdRegistered();
  }

  @OnClick(R.id.registerFaceButton)
  void registerFace() {
    FaceAuth.getInstance().register(this);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    FaceAuth.getInstance().handleRegistration(requestCode, resultCode, data,
        new FaceAuth.RegistrationCallback() {
          @Override public void onSuccess(UUID registeredFaceId, Uri imageUri) {

            mProgressDialog.show();
            checkAndInsertFace(registeredFaceId.toString(), imageUri);
          }

          @Override public void onError(Exception e) {

          }
        });

    super.onActivityResult(requestCode, resultCode, data);
  }

  private void setProfilePic() {
    Picasso.with(this)
        .load(SbiApiHelper.getProfileUrl(mAccountNumber))
        .placeholder(R.drawable.profile_placeholder)
        .into(profilePicture);
  }

  private void checkIfFaceIdRegistered() {
    mProgressDialog.show();
    mAccountRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot snap) {
        if (snap != null && snap.exists() && snap.getValue(Account.class) != null) {
          Account account = snap.getValue(Account.class);
          mAccount = account;
          mAccountHolderNameView.setText(mAccount.name);

          if (account.faceId != null && !TextUtils.isEmpty(account.faceId)) {
            // face id exists
            mRegisterCard.setVisibility(View.GONE);
            mRegisteredCard.setVisibility(View.VISIBLE);
          } else {
            mRegisterCard.setVisibility(View.VISIBLE);
            mRegisteredCard.setVisibility(View.GONE);
          }
        } else {
          android.util.Log.e(TAG, "data doesn't exist");
        }
        mProgressDialog.hide();
      }

      @Override public void onCancelled(DatabaseError databaseError) {
        android.util.Log.e(TAG, "onFailure", databaseError.toException());
        mProgressDialog.hide();
      }
    });
  }

  private void checkAndInsertFace(String faceId, Uri faceUri) {
    insertFace(faceUri, faceId);
  }

  private void insertFace(Uri uri, final String faceId) {

    InputStream stream = null;
    try {
      stream = getContentResolver().openInputStream(uri);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

    if (stream == null) {
      android.util.Log.e(TAG, "Stream is null");
      return;
    }

    Bitmap bitmap = BitmapFactory.decodeStream(stream);

    if (bitmap.getWidth() > 360) {
      bitmap = Bitmap.createScaledBitmap(
          bitmap,
          360,
          bitmap.getHeight() * 360 / bitmap.getWidth(),
          false
      );
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);

    SbiApiHelper.insertFace(mAccount.name, mAccountNumber, bos.toByteArray(),
        new Callback<InsertResponse>() {
          @Override
          public void onResponse(Call<InsertResponse> call, Response<InsertResponse> response) {
            if (response.isSuccessful() && response.body() == null) {
              Toast.makeText(DashboardActivity.this, "Registration Succesful",
                  Toast.LENGTH_LONG).show();

              // hide faceauth card
              mRegisterCard.setVisibility(View.GONE);
              mRegisteredCard.setVisibility(View.VISIBLE);

              final long currentTime =
                  Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

              Map<String, Object> vals = new HashMap<>();
              vals.put("faceId", faceId);
              vals.put("updatedAt", currentTime);

              mAccountRef.updateChildren(vals);
              setProfilePic();

            } else {
              android.util.Log.e(TAG, "onUnsuccessful " + response.message());
            }
            mProgressDialog.hide();
          }

          @Override public void onFailure(Call<InsertResponse> call, Throwable t) {
            android.util.Log.e(TAG, "onFailure", t);
            mProgressDialog.hide();


          }
        });
  }

  @OnClick(R.id.transferFundsButton)
  void startFundsTransfer() {
    startActivity(new Intent(this, FundsTransferActivity.class));
  }
}
