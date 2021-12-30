package bgu.spl.net.api;

public class ServerResponse implements Message{

    private short firstOP;
    private short secondOP;
    private char notificationType; //PM or Public
    private String postingUser;
    private String content;
    private String username;
    private short age;
    private short numPosts;
    private short numFollowers;
    private short numFollowing;

    public ServerResponse(short op) {
        firstOP=op;
    }

    public short getFirstOP() {
        return firstOP;
    }

    public short getSecondOP() {
        return secondOP;
    }

    public char getNotificationType() {
        return notificationType;
    }

    public String getPostingUser() {
        return postingUser;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public short getAge() {
        return age;
    }

    public short getNumPosts() {
        return numPosts;
    }

    public short getNumFollowers() {
        return numFollowers;
    }

    public short getNumFollowing() {
        return numFollowing;
    }

    public void setFirstOP(short firstOP) {
        this.firstOP = firstOP;
    }

    public void setSecondOP(short secondOP) {
        this.secondOP = secondOP;
    }

    public void setNotificationType(char notificationType) {
        this.notificationType = notificationType;
    }

    public void setPostingUser(String postingUser) {
        this.postingUser = postingUser;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public void setNumPosts(short numPosts) {
        this.numPosts = numPosts;
    }

    public void setNumFollowers(short numFollowers) {
        this.numFollowers = numFollowers;
    }

    public void setNumFollowing(short numFollowing) {
        this.numFollowing = numFollowing;
    }
}
