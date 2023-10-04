package file_service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TCPFileServiceServer {
    public static void main(String[] args) throws Exception{
    if(args.length != 2){
        System.out.println("Syntax: TCPFileServiceClient <ServerIP> <ServerPort>");
        }
        int serverPort = Integer.parseInt(args[1]);
        InetAddress serverIP = InetAddress.getByName(args[0]);
        String command = "";

        do{
            Scanner input = new Scanner(System.in);
            System.out.println("Please type a command: ");
            command = input.nextLine().toUpperCase();
            //TODO U for Upload, G for download, R for rename, L for list
            switch(command){
                case "D":
                    System.out.println("Please enter the file name:");
                    String filename = input.nextLine();
                    ByteBuffer request =
                            ByteBuffer.wrap((command+filename).getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(
                            new InetSocketAddress(serverIP, serverPort));
                    channel.write(request);
                    channel.shutdownOutput();
                    //S for Success, F for Failure
                    int bytesRead = 1;
                    ByteBuffer statusCode = ByteBuffer.allocate(bytesRead);
                    while((bytesRead -= channel.read(statusCode))> 0);
                    statusCode.flip();
                    byte[] a = new byte[bytesRead];
                    statusCode.get(a);
                    System.out.println(new String(a));
                    break;
                case "U":
                    break;
                case "G":
                    break;
                case "R":
                    break;
                case "L":
                    break;
                default:
                    if(!command.equals("Q")){
                        System.out.println("Unknown command");
                    }
            }
        }while(!command.equals("Q"));
    }
}
/*TODO server side command = read 1st byte then switch(command): case,
output files in different folders for server/client file f = new file("directoryName/FileName)
*/