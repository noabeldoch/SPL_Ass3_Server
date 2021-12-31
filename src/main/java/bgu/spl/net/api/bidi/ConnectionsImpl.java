package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Database;
import bgu.spl.net.api.Message;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentMap;

public class ConnectionsImpl implements Connections{

    private ConcurrentMap<String, Integer> usernameToId;
    private ConcurrentMap<Integer, ConnectionHandler> idToHandler;

    private static class ConnectionsHolder{
        private static ConnectionsImpl instance = new ConnectionsImpl();
    }

    public static ConnectionsImpl getInstance() {
        return ConnectionsImpl.ConnectionsHolder.instance;
    }

    //Use the function send() of connectionHandler in order to send message to the client -
    // both in send and broadcast function here (written in the assignment)
    @Override
    public boolean send(int connectionId, Object msg) {
        return false;
    }

    public boolean send(String username, Message msg) {
        int connId = usernameToId.get(username);
        return send(connId, msg);
    }

    @Override
    public void broadcast(Object msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }


}
