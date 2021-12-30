package bgu.spl.net.api;

public class ClientMessage implements Message{

    private short op;
    private String username;
    private String password;
    private char captcha; //1-successful login
    private char follow; //1-unfollow, 0-follow
    private String content;
    private String sendDate;
    private String usersNamesList;
    private String birthday;

    public ClientMessage(short op) {
        this.op=op;
    }

    public short getOp() {
        return op;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public char getCaptcha() {
        return captcha;
    }

    public char getFollow() {
        return follow;
    }

    public String getContent() {
        return content;
    }

    public String getSendDate() {
        return sendDate;
    }

    public String getUsersNamesList() {
        return usersNamesList;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setOp(short op) {
        this.op = op;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCaptcha(char captcha) {
        this.captcha = captcha;
    }

    public void setFollow(char follow) {
        this.follow = follow;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public void setUsersNamesList(String usersNamesList) {
        this.usersNamesList = usersNamesList;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
