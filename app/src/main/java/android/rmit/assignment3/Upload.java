package android.rmit.assignment3;

public class Upload {
    private String userid;
    private String mImageUrl;

    public Upload() {
    }

    public Upload(String userid, String mImageUrl) {

        this.userid = userid;
        this.mImageUrl = mImageUrl;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
