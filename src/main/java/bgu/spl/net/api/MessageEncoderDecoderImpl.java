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
        ServerResponse response = (ServerResponse) message;
        short firstOP = response.getFirstOP();

        //Error
        if(firstOP==11) {
            byte[] firstOPByte = shortToBytes(firstOP);
            byte[] secondOPByte = shortToBytes(response.getSecondOP());
            byte[] errorResponse = mergeBytes(firstOPByte,secondOPByte);
            return errorResponse;
        }

        //Notification
        else if (firstOP==9) {
            //First op byte array
            byte[] firstOPByte = shortToBytes(firstOP);

            //Notification type byte array
            char notificationType = response.getNotificationType();
            byte[] notificationTypeByte = {(byte) notificationType};

            //Posting user byte array
            String postingUser = response.getPostingUser();
            char[] charArr = postingUser.toCharArray();
            byte [] postingUserByte = charArrToBytes(charArr);

            //Zero byte array
            byte[] zero = {'\0'};

            //Content byte array
            String content = response.getContent();
            char[] charArr1 = content.toCharArray();
            byte [] contentByte = charArrToBytes(charArr1);

            //Merge all byte arrays
            byte[] b1 = mergeBytes(firstOPByte,notificationTypeByte);
            byte[] b2 = mergeBytes(b1,postingUserByte);
            byte[] b3 = mergeBytes(b2,zero);
            byte[] b4 = mergeBytes(b3,contentByte);
            byte[] responseByte = mergeBytes(b4,zero);

            return responseByte;
        }

        //Ack
        else {
            byte[] firstOPByte = shortToBytes(firstOP);

            short secondOP = response.getSecondOP();
            byte[] secondOPByte = shortToBytes(secondOP);

            //Follow
            if(secondOP==4) {
                String username = response.getUsername();
                char[] charArr1 = username.toCharArray();
                byte [] usernameByte = charArrToBytes(charArr1);
                byte[] zero = {'\0'};

                byte[] b1 = mergeBytes(firstOPByte,secondOPByte);
                byte[] b2 = mergeBytes(b1, usernameByte);
                byte[] responseByte = mergeBytes(b2,zero);

                return responseByte;
            }

            //Logstat, Stat
            else if (secondOP==7 || secondOP==8) {
                short age = response.getAge();
                byte[] ageByte = shortToBytes(age);

                short numPosts = response.getNumPosts();
                byte[] numPostsByte = shortToBytes(numPosts);

                short numFollowers = response.getNumFollowers();
                byte[] numFollowersByte = shortToBytes(numFollowers);

                short numFollowing = response.getNumFollowing();
                byte[] numFollowingByte = shortToBytes(numFollowing);

                byte[] b1 = mergeBytes(firstOPByte,secondOPByte);
                byte[] b2 = mergeBytes(b1,ageByte);
                byte[] b3 = mergeBytes(b2,numPostsByte);
                byte[] b4 = mergeBytes(b3,numFollowersByte);
                byte[] responseByte = mergeBytes(b4,numFollowingByte);

                return responseByte;
            }

            //Generic Ack
            else {
                byte[] responseByte = mergeBytes(firstOPByte,secondOPByte);
                return responseByte;
            }
        }
    }


    private byte[] charArrToBytes(char[] charArr) {
        byte[] bytesArr = new byte[charArr.length];
        for(int i=0;i<charArr.length;i++){
            bytesArr[i]= (byte) charArr[i];
        }
        return bytesArr;
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

    public static byte[] shortToBytes(short num){
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public static byte[] mergeBytes(byte[] a, byte[] b) {
        //return merged array, a is first b is second
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
