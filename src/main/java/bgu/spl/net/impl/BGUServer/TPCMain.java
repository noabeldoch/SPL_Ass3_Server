package bgu.spl.net.impl.BGUServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.MessagingProtocolImpl;
import bgu.spl.net.srv.BaseServerImpl;

public class TPCMain {

    public static void main(String[] args) {
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
