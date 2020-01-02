package android.rmit.assignment3;

public class SumVote {
    Long sum;
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SumVote(Long sum) {
        this.sum = sum;
    }

    public Long getSum() {
        return sum;
    }

    public void setSum(Long sum) {
        this.sum = sum;
    }

    public SumVote() {
    }
}
