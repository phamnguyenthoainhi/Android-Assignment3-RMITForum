package android.rmit.assignment3;

public class PostUser {
    private String user;
    private String post;

    public PostUser(String user, String post) {
        this.user = user;
        this.post = post;
    }

    public PostUser() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
