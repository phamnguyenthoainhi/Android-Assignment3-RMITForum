package android.rmit.assignment3;

public class Comment {
    private String reply, owner,content;
    private int upvote, downvote;
    private long dateTime;

    public Comment() {
    }

    public Comment(String reply, String content) {
        this.reply=reply;
        this.owner = owner;
        this.content = content;
        this.upvote = 0;
        this.downvote = 0;
        this.dateTime = System.currentTimeMillis();
    }



    public Comment(String reply, String owner, String content, int upvote, int downvote, long dateTime) {
        this.reply = reply;
        this.owner = owner;
        this.content = content;
        this.upvote = upvote;
        this.downvote = downvote;
        this.dateTime = dateTime;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
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

    public void setUpvote(int upvote) {
        this.upvote = upvote;
    }

    public int getDownvote() {
        return downvote;
    }

    public void setDownvote(int downvote) {
        this.downvote = downvote;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
