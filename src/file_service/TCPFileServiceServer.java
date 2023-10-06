package file_service;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPFileServiceServer {
    public static void main(String[] args) throws Exception{
        int port = 3000;
        ServerSocketChannel welcomeChannel =
                ServerSocketChannel.open();
        welcomeChannel.socket().bind(new InetSocketAddress(port));
        while(true){
            SocketChannel serveChannel = welcomeChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(2500);
            int numBytes = 0;
            do {
                numBytes = serveChannel.read(request);
            }while(numBytes >= 0);

            //while(serveChannel.read(request) >= 0);

            //new
            request.flip();
            char command = (char)request.get();
            System.out.println("received command: "+command);
            switch(command){
                case 'D':
                    byte[] a = new byte[request.remaining()];
                    request.get(a);
                    String fileName = new String(a);
                    System.out.println("file to delete: "+fileName);
                    File file = new File("ServerFiles/"+fileName);
                    boolean success = false;
                    if(file.exists()){
                        success = file.delete();
                    }
                    if(success){
                        ByteBuffer code =
                                ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(code);
                    }else{
                        ByteBuffer code =
                                ByteBuffer.wrap("F".getBytes());
                        serveChannel.write(code);
                    }
                    serveChannel.close();
                    //new
                    break;

                case 'L':

                    break;
                case 'R':

                    break;

                case 'G':

                    break;
                case 'U':

                    break;

            }

        }
    }

}
