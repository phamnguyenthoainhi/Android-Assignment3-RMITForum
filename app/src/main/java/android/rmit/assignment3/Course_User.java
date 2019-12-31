package android.rmit.assignment3;

public class Course_User {

    private String courseid;
    private String userid;

    public Course_User(String courseid, String userid) {

        this.courseid = courseid;
        this.userid = userid;
    }



    public Course_User() {
    }




    public String getCourseid() {
        return courseid;
    }

    public void setCourseid(String courseid) {
        this.courseid = courseid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
