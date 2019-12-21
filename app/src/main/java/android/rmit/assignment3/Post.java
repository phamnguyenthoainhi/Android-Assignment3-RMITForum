package android.rmit.assignment3;

public class Post {

    private String owner, title, content, course,id;
    private int upvote, downvote;
    private long dateTime;

    public Post() {
    }

    public Post(String title, String content) {
        this.owner = "abc";
        this.title = title;
        this.content = content;
        this.course = "COSC2171";
        this.upvote = 0;
        this.downvote = 0;
        this.dateTime = System.currentTimeMillis();
        this.id="";
    }

    public Post(String owner, String title, String content, String course, String id, int upvote, int downvote, long dateTime) {
        this.owner = owner;
        this.title = title;
        this.content = content;
        this.course = course;
        this.id = id;
        this.upvote = upvote;
        this.downvote = downvote;
        this.dateTime = dateTime;
    }

    public Post(String owner, String title, String content, String course, int upvote, int downvote, long dateTime) {
        this.owner = owner;
        this.title = title;
        this.content = content;
        this.course = course;
        this.upvote = upvote;
        this.downvote = downvote;
        this.dateTime = dateTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Post{" +
                "owner='" + owner + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", course='" + course + '\'' +
                ", upvote=" + upvote +
                ", downvote=" + downvote +
                ", dateTime=" + dateTime +
                '}';
    }
}
