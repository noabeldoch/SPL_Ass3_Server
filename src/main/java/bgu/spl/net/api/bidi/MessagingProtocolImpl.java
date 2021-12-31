package bgu.spl.net.api.bidi;

import bgu.spl.net.api.*;

import javax.xml.crypto.Data;

public class MessagingProtocolImpl implements BidiMessagingProtocol<Message>{

    private boolean shouldTerminate = false;
    private int connId;
    private Connections connections;
    private Database db = Database.getInstance();
    private Client client = null;

    /**
     * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
     **/
    @Override
    public void start(int connectionId, Connections connections) {
        this.connId = connectionId;
        this.connections = connections;
    }

    @Override
    public Message process(Message message) {
        ClientMessage clientMessage = (ClientMessage)message;
        int op = clientMessage.getOp();
        switch (op){
            case 1:
                return register(clientMessage);
            case 2:
                return db.login(clientMessage);
            case 3:
                return logout(clientMessage);
            case 4:
                return db.follow(clientMessage, client);
            case 5:
                return db.post(clientMessage, client);
            case 6:
                return db.PM(clientMessage, client);
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public ServerResponse register (ClientMessage message) {
        ServerResponse response = db.register(message, connId);
        client = db.getUser(message.getUsername());
        return response;
    }

    public ServerResponse logout (ClientMessage message) {
        ServerResponse response= db.logout(message, client.getUsername());
        if(response.getFirstOP()==10) {
            this.shouldTerminate=true;
            //TODO remove the connection Id from Connections map
        }
        return response;
    }
}
