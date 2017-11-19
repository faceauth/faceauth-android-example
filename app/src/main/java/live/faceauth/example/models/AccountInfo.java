package live.faceauth.example.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AccountInfo {

  @SerializedName("aadhaar_no")
  public String aadhaarNumber;

  @SerializedName("account_no")
  public String accountNumber;

  public String encoding;

  @SerializedName("i_date")
  public String iDate;

  @SerializedName("img_size")
  public String imgSize;

  public List<Link> links;

  @SerializedName("mime_type")
  public String mimeType;

  public String name;

  @SerializedName("team_id")
  public long teamId;

}
