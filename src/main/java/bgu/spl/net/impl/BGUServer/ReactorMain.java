package bgu.spl.net.impl.BGUServer;

import bgu.spl.net.api.Database;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.api.bidi.MessagingProtocolImpl;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {

    public static void main (String [] args) {

        Database db = Database.getInstance();
        ConnectionsImpl connections = ConnectionsImpl.getInstance();
        db.setConnections();
        connections.setDataBase();

        int port;
        int numOfThreads;
        if (args.length==0){
            port = 7777;
            numOfThreads=5;

        }
        else {
            port = Integer.parseInt(args[0]);
            numOfThreads = Integer.parseInt(args[1]);
        }
        Reactor server = new Reactor(numOfThreads
                ,port
                ,()-> {return new MessagingProtocolImpl<>();}
                ,()-> {return new MessageEncoderDecoderImpl<>();});
        server.serve();
    }
}

