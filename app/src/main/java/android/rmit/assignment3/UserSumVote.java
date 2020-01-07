package android.rmit.assignment3;

public class UserSumVote {
    String userid;
    Long vote;
    String imageuri;
    String username;

    public UserSumVote() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Long getVote() {
        return vote;
    }

    public void setVote(Long vote) {
        this.vote = vote;
    }

    public String getImageuri() {
        return imageuri;
    }

    public void setImageuri(String imageuri) {
        this.imageuri = imageuri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserSumVote{" +
                "userid='" + userid + '\'' +
                ", vote=" + vote +
                ", imageuri='" + imageuri + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
