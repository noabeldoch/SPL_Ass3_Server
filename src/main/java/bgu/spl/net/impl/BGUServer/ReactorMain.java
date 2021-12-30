package bgu.spl.net.impl.BGUServer;

import bgu.spl.net.api.ClientMessage;
import bgu.spl.net.api.Message;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.ServerResponse;

import java.nio.charset.StandardCharsets;

public class ReactorMain {

    public static byte[] encode(Message message) {
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

    public static byte[] charArrToBytes(char[] charArr) {
        byte[] bytesArr = new byte[charArr.length];
        for(int i=0;i<charArr.length;i++){
            bytesArr[i]= (byte) charArr[i];
        }
        return bytesArr;
    }

    public static short bytesToShort(byte[] byteArr){
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
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

    public static void main (String [] args) {

        ServerResponse sr = new ServerResponse((short)10);
        sr.setSecondOP((short)7);
        sr.setAge((short)4);
        sr.setNumPosts((short)5);
        sr.setNumFollowers((short)6);
        sr.setNumFollowing((short)7);


        byte[] response = encode(sr);
        for(int i=0; i<response.length; i++) {
            System.out.print(response[i]+", ");
        }

//        String vIn = "A";
//        char [] vOut = vIn.toCharArray();
//        byte[] b = {(byte)vOut[0]};
//
//        char a = '1';
//        byte by = (byte) a;
//        byte[] byteArr = {(byte)a};
//        byteArr[0] = by;
//        System.out.println(by);

//        System.out.println(b[0]);

        //TESTING USER MESSAGES - DecodeNextByte
//        short op = 6;
//        byte[] b1 = shortToBytes(op);
//        String name="noa";
//        byte[] b2 = name.getBytes(StandardCharsets.UTF_8);
//        byte[] b3 = {'\0'};
//        String pass="dabi";
//        byte[] b4 = pass.getBytes(StandardCharsets.UTF_8);
//        byte[] b5 = {'\0'};
//        String birthday ="date";
//        byte[] b6 = birthday.getBytes(StandardCharsets.UTF_8);
//        byte[] b7 = {'\0'};
//        byte[] b8 = {'\n'};
//        byte[] b9 = mergeBytes(b1,b2);
//        byte[] b10 = mergeBytes(b9,b3);
//        byte[] b11 = mergeBytes(b10,b4);
//        byte[] b12 = mergeBytes(b11,b5);
//        byte[] b13 = mergeBytes(b12,b6);
//        byte[] b14 = mergeBytes(b13,b7);
//        byte[] b15 = mergeBytes(b14,b8);
//
//        ClientMessage c=null;
//        for (int i=0; i<b15.length; i++) {
//            c = (ClientMessage) MessageEncoderDecoderImpl.decodeNextByte(b15[i]);
//        }
//        System.out.println(c.getOp());
//        System.out.println(c.getUsername());
//        System.out.println(c.getContent());
//        System.out.println(c.getSendDate());
    }
}
