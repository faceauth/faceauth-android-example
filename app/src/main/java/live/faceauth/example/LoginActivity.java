package live.faceauth.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import live.faceauth.sdk.FaceAuth;
import live.faceauth.example.models.Account;
import live.faceauth.example.utils.NameUtils;
import live.faceauth.example.utils.Security;
import java.util.Calendar;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity implements FaceAuth.AuthenticationCallback {

  @BindView(R.id.username) EditText mUsername;
  @BindView(R.id.password) EditText mPassword;

  private static final boolean AUTO_MODE = true;
  private DatabaseReference mRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);
    String lastLoginAccount = DB.getLastLoginAccount(this);
    mRef = FirebaseDatabase.getInstance().getReference().child("accounts");
    if (lastLoginAccount != null) {
      mUsername.setText(lastLoginAccount);
      mRef.child(lastLoginAccount).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot snap) {
          if (snap != null && snap.getValue(Account.class) != null) {
            Account account = snap.getValue(Account.class);
            if (account.faceId != null && !TextUtils.isEmpty(account.faceId)) {
              FaceAuth.getInstance().authenticate(LoginActivity.this, account.faceId, AUTO_MODE);
            }
          }
        }

        @Override public void onCancelled(DatabaseError databaseError) {

        }
      });
    }
  }

  @OnClick(R.id.signInButton)
  void signIn() {
    final String username = mUsername.getText().toString();
    final String password = mPassword.getText().toString();

    mUsername.setError(null);
    mPassword.setError(null);

    if (username.isEmpty()) {
      mUsername.setError("Please enter an account number");
    } else if (password.isEmpty()) {
      mPassword.setError("Please enter password");
    } else {
      mRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot snap) {
          if (snap == null || !snap.exists()) {
            // Account doesn't exist - create account
            createFirebaseAccount(username, password);
          } else {
            Account account = snap.getValue(Account.class);
            if (account != null && Security.SHA1(password).equals(account.passwordHash)) {
              goToDashboardActivity(username);
            } else {
              // password didn't match
              mPassword.setError("Password is wrong.");
            }
          }
        }

        @Override public void onCancelled(DatabaseError databaseError) {

        }
      });
    }
  }

  private void createFirebaseAccount(final String username, String password) {
    Account account = new Account();
    // random name
    account.name = NameUtils.generate();
    account.accountNumber = username;
    account.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    account.passwordHash = Security.SHA1(password);

    mRef.child(username).setValue(account).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
          goToDashboardActivity(username);
        }
      }
    });
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
      final Intent result) {
    boolean authResultHandled =
        FaceAuth.getInstance().handleAuthentication(requestCode, resultCode, result, this);

    if (!authResultHandled) {
      super.onActivityResult(requestCode, resultCode, result);
    }
  }

  private void goToDashboardActivity(String username) {
    Intent dashboardActivity = new Intent(this, DashboardActivity.class);
    dashboardActivity.putExtra(DashboardActivity.ACCOUNT_NUMBER, username);
    startActivity(dashboardActivity);
    finish();
  }

  private void showAuthResultDialog(String message, final boolean success) {
    new AlertDialog.Builder(this)
        .setMessage(message)
        .setTitle("Face Authentication Result")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialogInterface, int i) {
            if (success) {
              goToDashboardActivity(DB.getLastLoginAccount(LoginActivity.this));
            }
          }
        })
        .show();
  }

  @Override public void onSuccess(int confidence, double score) {
    if (AUTO_MODE) {
      Toast.makeText(this, "Confidence: " + confidence + "%\nReal face: " + score + "%", Toast.LENGTH_SHORT).show();
      goToDashboardActivity(DB.getLastLoginAccount(LoginActivity.this));
    } else {
      String message = "Match found. You have been successfully authenticated. Confidence: "
          + confidence + "%, Real face:" + score + "%";
      showAuthResultDialog(message, true);
    }
  }

  @Override public void onFailure(int confidence, double score) {
    String message = "Authentication failed!\nMatch confidence: " + confidence + "%\nReal face: " + score + "%";
    showAuthResultDialog(message, false);
  }

  @Override public void onError(Exception e) {
  }
}
