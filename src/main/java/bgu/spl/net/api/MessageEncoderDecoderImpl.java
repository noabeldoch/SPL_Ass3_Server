package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message>{


    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public Message decodeNextByte(byte nextByte) {
        if (nextByte=='\n') {
            return createClientMessage();
        }
        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(Message message) {
        // we will get message like "8 2" or "8 0dani\0blabla\0" or "8 2 18 2 8 3" (=STAT)
        // the numbers should be short.
        //in the protocol we will add a space after the opcode and after each short,
        // and here we will use ShortToBytes when necessary
        return (message + "\n").getBytes();
    }

    public short bytesToShort(byte[] byteArr){
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private short getOpFromBytes(){
        short op = (short) ((bytes[0] & 0xff) << 8);
        op += (short) (bytes[1] & 0xff);
        return op;
    }

    private ClientMessage createClientMessage() {
        short op = getOpFromBytes();
        ClientMessage clientMessage = new ClientMessage(op);

        //Logout, Logstat
        if(op==3 || op==7) {
            return clientMessage;
        }

        //Post, Stat, Block
        else if(op==5 || op==8 || op==12) {
            String str = new String(bytes, 2, len - 3, StandardCharsets.UTF_8);  //without the \0 byte in the end
            if(op==5)
                clientMessage.setContent(str);
            else if (op==8)
                clientMessage.setUsersNamesList(str);
            else
                clientMessage.setUsername(str);
            return clientMessage;
        }

        //Follow
        else if(op==4) {

            //Follow/unfollow byte
            String str = new String(bytes,2,1, StandardCharsets.UTF_8);
            char[] followArr = str.toCharArray();
            char follow = followArr[0];
            clientMessage.setFollow(follow);

            //Username
            String name = new String(bytes,3,len-4, StandardCharsets.UTF_8);
            clientMessage.setUsername(name);

            return clientMessage;
        }

        //Login
        else if (op==2) {
            String str = new String(bytes,2,len-4, StandardCharsets.UTF_8);
            String [] login = str.split("\0");
            clientMessage.setUsername(login[0]);
            clientMessage.setPassword(login[1]);

            //captcha byte
            String cap = new String(bytes,len-1,1, StandardCharsets.UTF_8);
            char[] capArr = cap.toCharArray();
            char captcha = capArr[0];
            clientMessage.setCaptcha(captcha);

            return clientMessage;
        }

        //Register, PM
        else if (op==1 || op==6) {
            String str = new String(bytes,2,len-3, StandardCharsets.UTF_8);
            String [] strArr = str.split("\0");
            if (op==1) {
                clientMessage.setUsername(strArr[0]);
                clientMessage.setPassword(strArr[1]);
                clientMessage.setBirthday(strArr[2]);
            }
            else {
                clientMessage.setUsername(strArr[0]);
                clientMessage.setContent(strArr[1]);
                clientMessage.setSendDate(strArr[2]);
            }
            return clientMessage;
        }
        return null;
    }
}
