package android.rmit.assignment3;

public class Course {
    private String id;
    private String name;
    private String docid;

    public Course(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Course() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course(String id, String name, String docid) {
        this.id = id;
        this.name = name;
        this.docid = docid;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    @Override
    public String toString() {
        return "Course " +
                "id  '" + id + '\'' +
                ", name  " + name + '\'' +
                ", docid  " + docid + '\'' ;
    }
}
