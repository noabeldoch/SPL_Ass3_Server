package bgu.spl.net.api;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Client {

    private String username;
    private String password;
    private boolean isLoggedIn;
    private short age;
    private ConcurrentSkipListSet<String> followers; //usernames who follow me
    private ConcurrentSkipListSet<String> following; //usernames I follow
    private short numPosts; //public posts
    private ConcurrentLinkedQueue<Message> unreadMessages; //Messages from other users that were sent while we were logged out
    private int connId;
    private LinkedList<String> usersIBlocked;
    private ConcurrentLinkedQueue<String> userBlockedMe; //maybe readers writers lock - for the case our user going over this list, and another user i blocking us and adding himself

    //connId will be saved in the connectionHandler after accept
    public Client(String username, String password, String birthday, int connId) {
        this.username = username;
        this.password = password;
        this.age = calcAge(birthday);
        this.connId = connId;
        followers = new ConcurrentSkipListSet<>();
        following = new ConcurrentSkipListSet<>();
        unreadMessages = new ConcurrentLinkedQueue<>();
        usersIBlocked = new LinkedList<>();
        userBlockedMe = new ConcurrentLinkedQueue<>();
    }

    private short calcAge(String birthday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate birthday1 = LocalDate.parse(birthday, formatter);
        Period period = Period.between(birthday1, LocalDate.now());
        return (short)period.getYears();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public ConcurrentSkipListSet<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ConcurrentSkipListSet<String> followers) {
        this.followers = followers;
    }

    public ConcurrentSkipListSet<String> getFollowing() {
        return following;
    }

    public void setFollowing(ConcurrentSkipListSet<String> following) {
        this.following = following;
    }

    public short getNumPosts() {
        return numPosts;
    }

    public void setNumPosts(short numPosts) {
        this.numPosts = numPosts;
    }

    public ConcurrentLinkedQueue<Message> getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(ConcurrentLinkedQueue<Message> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public int getConnId() {
        return connId;
    }

    public void setConnId(int connId) {
        this.connId = connId;
    }

    public LinkedList<String> getUsersIBlocked() {
        return usersIBlocked;
    }

    public void setUsersIBlocked(LinkedList<String> usersIBlocked) {
        this.usersIBlocked = usersIBlocked;
    }

    public ConcurrentLinkedQueue<String> getUserBlockedMe() {
        return userBlockedMe;
    }

    public void setUserBlockedMe(ConcurrentLinkedQueue<String> userBlockedMe) {
        this.userBlockedMe = userBlockedMe;
    }

    public void addUserToFollowing(String username) {
        following.add(username);
    }

    public void addUserToFollowers(String username) {
        followers.add(username);
    }

    public void removeUserFromFollowing(String username) {
        following.remove(username);
    }

    public void removeUserFromFollowers(String username) {
        followers.remove(username);
    }

    public void increaseNumPosts() {
        this.numPosts++;
    }

    public void addUnreadMessageToQueue(Message message) {
        unreadMessages.add(message);
    }
}
