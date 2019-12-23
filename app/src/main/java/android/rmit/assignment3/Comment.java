package android.rmit.assignment3;

public class Comment {
    private String reply, owner,content,id;
    private int upvote;
    private long dateTime;

    public Comment() {
    }

    public Comment(String reply, String content) {
        this.reply=reply;
        this.owner = "comment person";
        this.content = content;
        this.upvote = 0;
        this.dateTime = System.currentTimeMillis();
        this.id="";
    }

    public Comment(String reply, String owner, String content) {
        this.reply = reply;
        this.owner = owner;
        this.content = content;
        this.upvote = 0;
        this.dateTime = System.currentTimeMillis();
        this.id="";
    }

    public Comment(String reply, String owner, String content, int upvote, long dateTime) {
        this.reply = reply;
        this.owner = owner;
        this.content = content;
        this.upvote = upvote;
        this.dateTime = dateTime;
    }

    public Comment(String reply, String owner, String content, String id, int upvote, long dateTime) {
        this.reply = reply;
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
