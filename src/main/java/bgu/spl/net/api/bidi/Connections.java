package bgu.spl.net.api.bidi;

import java.io.IOException;

public interface Connections<T> {

    //Use the function send() of connectionHandler in order to send message to the client -
    // both in send and broadcast function here (written in the assignment)
    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
