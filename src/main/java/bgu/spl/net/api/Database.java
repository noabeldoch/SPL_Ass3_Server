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
        connectionIdCounter = new AtomicInteger(0);
        posts = new LinkedList<>();
        filterWords = new Vector<>(Arrays.asList("war","Trump"));

    }

    public int getConnId() {
        return connectionIdCounter.addAndGet(1);
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

    public ServerResponse logstat(ClientMessage message, Client client) {
        String ourUsername = client.getUsername();
        //Check if is not registered || is not logged in
        if(!isUserExist(ourUsername) || !isLoggedIn(ourUsername)) {
           return createError(message.getOp());
        }

        for(Map.Entry<String, Client> user : users.entrySet()) {
           String currUsername = user.getKey();
           if(!client.getUserBlockedMe().contains(currUsername) && !currUsername.equals(ourUsername)) {
               Client currUser = user.getValue();
               ServerResponse logstat = new ServerResponse((short)10);
               logstat.setSecondOP((short)7);
               logstat.setAge(currUser.getAge());
               logstat.setNumPosts(currUser.getNumPosts());
               logstat.setNumFollowing((short)currUser.getFollowing().size());
               logstat.setNumFollowers((short)currUser.getFollowers().size());

               connections.send(ourUsername, logstat);
           }
        }
        return null;
    }

    public ServerResponse stat(ClientMessage message, Client client) {
        String ourUsername = client.getUsername();
        //Check if is not registered || is not logged in
        if(!isUserExist(ourUsername) || !isLoggedIn(ourUsername)) {
            return createError(message.getOp());
        }

        String[] arr = message.getContent().split("|");
        Vector<String> usernames = new Vector<>(Arrays.asList(arr));
        for(String name : usernames) {

            //User doesnt exist - return Error
            if(!isUserExist(name)) {
                return createError(message.getOp());
            }

            //User blocked me - delete the user from the vector
            else if (client.getUserBlockedMe().contains(name)) {
                usernames.remove(name);
            }
        }

        //If the vector is empty - return Error
        if (usernames.isEmpty()) {
            return createError(message.getOp());
        }

        for(String name : usernames) {
            Client currUser = users.get(name);
            ServerResponse stat = new ServerResponse((short)10);
            stat.setSecondOP((short)8);
            stat.setAge(currUser.getAge());
            stat.setNumPosts(currUser.getNumPosts());
            stat.setNumFollowing((short)currUser.getFollowing().size());
            stat.setNumFollowers((short)currUser.getFollowers().size());

            connections.send(ourUsername, stat);
        }
        return null;
    }

    public ServerResponse block(ClientMessage message, Client client) {
        String ourUsername = client.getUsername();
        //Check if is not registered || is not logged in || user to block is not exist
        if(!isUserExist(ourUsername) ||
          !isLoggedIn(ourUsername) ||
          !isUserExist(message.getUsername())) {
            return createError(message.getOp());
        }

        String userToBlock = message.getUsername();
        if(client.getFollowers().contains(userToBlock))
            client.removeUserFromFollowers(userToBlock);
        if(client.getFollowing().contains(userToBlock))
            client.removeUserFromFollowing(userToBlock);

        Client blockedUser = users.get(userToBlock);
        if(blockedUser.getFollowers().contains(ourUsername))
            blockedUser.removeUserFromFollowers(ourUsername);
        if(blockedUser.getFollowing().contains(ourUsername))
            blockedUser.removeUserFromFollowing(ourUsername);

        client.addToUserIBLocked(userToBlock);
        blockedUser.addToUserBLockedME(ourUsername);

        ServerResponse response = new ServerResponse((short)10);
        response.setSecondOP((short)12);
        return response;
    }
}
