package bgu.spl.net.impl.BGUServer;

import bgu.spl.net.api.Database;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.api.bidi.MessagingProtocolImpl;
import bgu.spl.net.srv.BaseServerImpl;

import java.util.Arrays;
import java.util.Vector;

public class TPCMain {

    public static void main(String[] args) {

        Database db = Database.getInstance();
        ConnectionsImpl connections = ConnectionsImpl.getInstance();
        db.setConnections();
        connections.setDataBase();

        int port;
        if(args.length==0)
            port=7777;
        else
            port = Integer.parseInt(args[0]);
        BaseServerImpl server = new BaseServerImpl(port
                , () -> {return new MessagingProtocolImpl<>();}
                , () -> {return new MessageEncoderDecoderImpl<>();});
        server.serve();
    }
}
