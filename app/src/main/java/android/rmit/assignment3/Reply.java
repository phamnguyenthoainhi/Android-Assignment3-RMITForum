package android.rmit.assignment3;

public class Reply {

    private String post, owner, content,id;
    private int upvote;
    private long dateTime;

    public Reply() {
    }

    public Reply(String post, String content) {
        this.post = post;
        this.owner = "def";
        this.content = content;
        this.upvote = 0;
        this.dateTime = System.currentTimeMillis();
        this.id="";
    }

    public Reply(String post, String owner, String content, int upvote, long dateTime) {
        this.post = post;
        this.owner = owner;
        this.content = content;
        this.upvote = upvote;
        this.dateTime = dateTime;
    }

    public Reply(String post, String owner, String content, String id, int upvote, long dateTime) {
        this.post = post;
        this.owner = owner;
        this.content = content;
        this.id = id;
        this.upvote = upvote;
        this.dateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUpvote() {
        return upvote;
    }

    public void increaseUpvote() {
        this.upvote+=1;
    }
    public void decreaseUpvote(){this.upvote-=1;}

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
