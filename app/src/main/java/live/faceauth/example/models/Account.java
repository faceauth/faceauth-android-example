package live.faceauth.example.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Account {

  public String accountNumber;

  public long createdAt;

  public long updatedAt;

  public String faceId;

  public String name;

  public String passwordHash;
}
