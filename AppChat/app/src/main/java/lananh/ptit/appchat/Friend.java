package lananh.ptit.appchat;

public class Friend {
    private String uid, date;

    public Friend() {
    }

    public Friend(String uid, String date) {
        this.uid = uid;
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
