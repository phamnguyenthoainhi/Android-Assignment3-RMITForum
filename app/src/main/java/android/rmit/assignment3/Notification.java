package android.rmit.assignment3;

public class Notification {
    //target id = post or reply or comment id
    private String type,user,targetId,content,id,title;
    private long dateTime;
    private boolean seen;

    public Notification(String type, String user, String targetId, String content, String id, String title, long dateTime, boolean seen) {
        this.type = type;
        this.user = user;
        this.targetId = targetId;
        this.content = content;
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.seen = seen;
    }

    public Notification() {
    }

    public Notification(String type, String user, String targetId, String content, String title, long dateTime, boolean seen) {
        this.type = type;
        this.user = user;
        this.targetId = targetId;
        this.content = content;
        this.title = title;
        this.dateTime = dateTime;
        this.seen = seen;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen() {
        this.seen = true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
