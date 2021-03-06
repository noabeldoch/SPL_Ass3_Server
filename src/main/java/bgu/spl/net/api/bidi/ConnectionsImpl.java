package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Database;
import bgu.spl.net.api.Message;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionsImpl implements Connections{

    private ConcurrentHashMap<String, Integer> usernameToId;
    private ConcurrentHashMap<Integer,String> idToUsername;
    private ConcurrentHashMap<Integer, ConnectionHandler> idToHandler;
    private Database db;

    private static class ConnectionsHolder{
        private static ConnectionsImpl instance = new ConnectionsImpl();
    }

    private ConnectionsImpl() {
        this.usernameToId = new ConcurrentHashMap<>();
        this.idToUsername = new ConcurrentHashMap<>();
        this.idToHandler = new ConcurrentHashMap<>();
        this.db = null;
    }

    public static ConnectionsImpl getInstance() {
        return ConnectionsHolder.instance;
    }

    public void setDataBase() {
        this.db = Database.getInstance();
    }

    //Use the function send() of connectionHandler in order to send message to the client -
    // both in send and broadcast function here (written in the assignment)
    @Override
    public boolean send(int connectionId, Object msg) {
        ConnectionHandler handler = idToHandler.get(connectionId);
        handler.send(msg);
        return true;
    }

    public boolean send(String username, Message msg) {
        if(!db.getUser(username).isLoggedIn())
            return false;
        int connId = usernameToId.get(username);
        return send(connId, msg);
    }

    //NOT IN USE
    @Override
    public void broadcast(Object msg) {

    }

    @Override
    public void disconnect(int connectionId) {
        String username = idToUsername.remove(connectionId);
        usernameToId.remove(username);
        idToHandler.remove(connectionId);
    }

    public void addToIdToHandler(int connId, ConnectionHandler handler) {
        idToHandler.put(connId, handler);
    }

    public void addIdAndUsername(int connId, String username) {
        idToUsername.put(connId, username);
        usernameToId.put(username,connId);
    }
}
