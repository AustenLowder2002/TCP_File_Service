package file_service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TCPFileServiceClient {
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
                    String filePath = "file_to_send.txt"; // Path to the file to upload

                        File file = new File(filePath);
                        Socket socket = new Socket(serverIP, serverPort);
                        OutputStream outputStream = socket.getOutputStream();

                        // Send the file name to the server
                        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                        dataOutputStream.writeUTF(file.getName());

                        // Create an input stream to read the file
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];

                        // Read from the file and write to the socket
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        System.out.println("File sent successfully.");

                        // Close streams and socket
                        fileInputStream.close();
                        socket.close();
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