package bgu.spl.net.api;

import bgu.spl.net.api.bidi.ConnectionsImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Database {

    ConcurrentHashMap<String, Client> users;
    AtomicInteger connectionIdCounter; //Maybe will be in server and reactor instead
    LinkedList<String> posts; //All the posts and PM that were sent (saved in ClientMessage content)
    Vector<String> filterWords;
    ConnectionsImpl connections = ConnectionsImpl.getInstance();

    private  static class DBHolder{
        private static Database instance = new Database();
    }

    public static Database getInstance() {
        return DBHolder.instance;
    }

    private Database() {
        users = new ConcurrentHashMap<>();
        connectionIdCounter.set(0);
        posts = new LinkedList<>();
        filterWords = new Vector<>(Arrays.asList("war","Trump"));
    }

    public Client getUser(String username) {
        return users.get(username);
    }

    private boolean isUserExist(String username) {
        synchronized (users) {
            return users.containsKey(username);
        }
    }

    private boolean isLoggedIn(String username) {
        Client c = users.get(username);
        return c.isLoggedIn();
    }

    public ServerResponse createError(short secondOP) {
        ServerResponse error = new ServerResponse((short)11);
        error.setSecondOP(secondOP);
        return error;
    }

    public ServerResponse register(ClientMessage message, int connId) {
        ServerResponse response;

        //Check if client already registered
        if(isUserExist(message.getUsername())) {
            return createError(message.getOp());
        }
        else {
            //Create client
            Client c = new Client(message.getUsername(), message.getPassword(), message.getBirthday(), connId);
            users.put(message.getUsername(), c);

            //Create Ack
            response = new ServerResponse((short)10);
            response.setSecondOP(message.getOp());
            return response;
        }
    }

    public ServerResponse login(ClientMessage message){

        //Check if user is not registered || already logged in || wrong password || invalid captcha
        if(!isUserExist(message.getUsername()) ||
           isLoggedIn(message.getUsername()) ||
           message.getPassword()!=users.get(message.getUsername()).getPassword() ||
           message.getCaptcha()!=1) {
            return createError(message.getOp());
        }
        Client client = users.get(message.getUsername());
        client.setLoggedIn(true);

        if (!client.getUnreadMessages().isEmpty()) {
            //TODO Implement - check if the client has unread messages and send them as notifications ??????????
        }

        //Create Ack
        ServerResponse response = new ServerResponse((short)10);
        response.setSecondOP(message.getOp());
        return response;
    }

    public ServerResponse logout(ClientMessage message, String ourUsername) {
        //Check if is not registered || is not logged in
        if(!isUserExist(ourUsername) || !isLoggedIn(ourUsername)) {
            return createError(message.getOp());
        }

        Client client = users.get(ourUsername);
        client.setLoggedIn(false);

        //Create Ack
        ServerResponse response = new ServerResponse((short)10);
        response.setSecondOP(message.getOp());
        return response;
    }

    public ServerResponse follow(ClientMessage message, Client client) {
        String ourUsername = client.getUsername();
        //Check if is not registered || is not logged in
        if(!isUserExist(ourUsername) || !isLoggedIn(ourUsername)) {
            return createError(message.getOp());
        }

        //Follow==0
        if (message.getFollow()==0) {

            String usernameToFollow = message.getUsername();

            //Check if user already in the following list || user doesn't exist || user blocked us || we blocked this user
            if (client.getFollowing().contains(usernameToFollow) ||
                !isUserExist(usernameToFollow) ||
                client.getUserBlockedMe().contains(usernameToFollow) ||
                client.getUsersIBlocked().contains(usernameToFollow)) {
                  return createError(message.getOp());
            }

            //Add the new username to our following list, and our username to his followers list
            client.addUserToFollowing(usernameToFollow);
            users.get(usernameToFollow).addUserToFollowers(ourUsername);

            //Create Ack
            ServerResponse response = new ServerResponse((short)10);
            response.setSecondOP(message.getOp());
            response.setUsername(usernameToFollow);
            return response;
        }

        //Unfollow
        else {
            String usernameToUnfollow = message.getUsername();

            //Check if user is not in the following list || user doesn't exist
            if (!client.getFollowing().contains(usernameToUnfollow) ||
                    !isUserExist(usernameToUnfollow)) {
                return createError(message.getOp());
            }

            //Remove the username from ou following list, and our name from his followers list
            client.removeUserFromFollowing(usernameToUnfollow);
            users.get(usernameToUnfollow).removeUserFromFollowers(ourUsername);

            //Create Ack
            ServerResponse response = new ServerResponse((short)10);
            response.setSecondOP(message.getOp());
            response.setUsername(usernameToUnfollow);
            return response;
        }
    }

    public ServerResponse post(ClientMessage message, Client client) {
        String ourUsername = client.getUsername();
        //Check if is not registered || is not logged in
        if(!isUserExist(ourUsername) || !isLoggedIn(ourUsername)) {
            return createError(message.getOp());
        }

        //Add post to DB
        posts.add(message.getContent());

        //Raise the number of public posts in the client
        client.increaseNumPosts();

        HashSet<String> recipients= new HashSet<>(client.getFollowers());
        recipients.addAll(findTaggedUsers(message.getContent(),client));

        //Send each recipient the notification
        for(String recipient : recipients) {
            ServerResponse notification = createNotification(message.getContent(), client.getUsername(), '1');

            //If recipient is not logged in, save the notification in his queue
            if(!connections.send(recipient, notification)){
                users.get(recipient).addUnreadMessageToQueue(notification);
            }
        }

        //Create Ack
        ServerResponse response = new ServerResponse((short)10);
        response.setSecondOP(message.getOp());
        return response;
    }

    public HashSet<String> findTaggedUsers (String content, Client client) {
        HashSet <String> taggedUsers = new HashSet<>();
        int index = 0;
        while(content.indexOf('@',index)!=-1) {
            int start = content.indexOf('@',index);
            int firstSpace = content.indexOf(' ', start+1);
            if(firstSpace==-1) {
                firstSpace=content.length();
            }
            String username = content.substring(start+1, firstSpace);
            //Check if blocked
            if (!client.getUserBlockedMe().contains(username) && !client.getUsersIBlocked().contains(username))
                taggedUsers.add(username);
            //FirstSpace -1 to handle the case that firsSpace == content.length
            index = firstSpace-1;
        }
        return taggedUsers;
    }

    public ServerResponse createNotification(String content, String postingUser, char notificationType) {
        ServerResponse response = new ServerResponse((short)9);
        response.setContent(content);
        response.setPostingUser(postingUser);
        response.setNotificationType(notificationType);
        return response;
    }

    public ServerResponse PM (ClientMessage message, Client client) {
        String ourUsername = client.getUsername();
        //Check if is not registered || is not logged in
        if(!isUserExist(ourUsername) || !isLoggedIn(ourUsername)) {
            return createError(message.getOp());
        }

        String recipient = message.getUsername();

        //check if recipient is not exist || our user doesn't follow him
        if (!isUserExist(recipient) || !client.getFollowing().contains(recipient)){
            return createError(message.getOp());
        }

        String filteredMessage = filterMessage(message.getContent());

        //Add post to DB
        posts.add(filteredMessage);

        //Create notification
        ServerResponse notification = createNotification(filteredMessage, client.getUsername(), '0');

        //If recipient is not logged in
        if(!connections.send(recipient,notification)){
            users.get(recipient).addUnreadMessageToQueue(notification);
        }

        //Create Ack
        ServerResponse response = new ServerResponse((short)10);
        response.setSecondOP(message.getOp());
        return response;
    }

    public String filterMessage(String message) {

        String [] words = message.split(" ");

        for(int i=0; i<filterWords.size(); i++) {
            String word = filterWords.get(i);
            for(int j=0; j<words.length; j++) {
                if(words[j].equals(word)) {
                    words[j] = "<filtered>";
                }
            }
        }
        StringBuilder output = new StringBuilder();
        for (int i=0; i<words.length-1; i++) {
            output.append(words[i]+" ");
        }
        output.append(words[words.length-1]);
        return output.toString();
    }
}
