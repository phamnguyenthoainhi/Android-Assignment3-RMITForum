package android.rmit.assignment3;

public class Course {
    private String id;
    private String name;

    public Course(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Course() {
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
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
}
