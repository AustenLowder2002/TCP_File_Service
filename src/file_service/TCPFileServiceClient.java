package file_service;

import java.io.*;
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
                    System.out.println("Please enter the file name to upload:");
                    String uploadFileName = input.nextLine();
                    File uploadFile = new File(uploadFileName);
                    if (uploadFile.exists()) {
                        // Send the command
                        ByteBuffer uploadCommand = ByteBuffer.wrap(command.getBytes());
                        channel.write(uploadCommand);

                        // Send the file name to the server
                        ByteBuffer fileNameBuffer = ByteBuffer.wrap(uploadFileName.getBytes());
                        channel.write(fileNameBuffer);

                        // Send the file data to the server
                        FileInputStream fileInputStream = new FileInputStream(uploadFile);
                        byte[] uploadBuffer = new byte[1024];

                        while ((bytesRead = fileInputStream.read(uploadBuffer)) > 0) {
                            ByteBuffer fileDataBuffer = ByteBuffer.wrap(uploadBuffer, 0, bytesRead);
                            channel.write(fileDataBuffer);
                        }

                        fileInputStream.close();
                        channel.shutdownOutput();

                        // Receive and display the server's response
                        ByteBuffer uploadResponseBuffer = ByteBuffer.allocate(1);
                        channel.read(uploadResponseBuffer);
                        uploadResponseBuffer.flip();
                        byte uploadResponse = uploadResponseBuffer.get();
                        if (uploadResponse == 'S') {
                            System.out.println("Upload successful.");
                        } else {
                            System.out.println("Upload failed.");
                        }
                    } else {
                        System.out.println("File not found.");
                    }
                    break;
                case "G":
                    System.out.println("Please enter the file name to download:");
                    String downloadFileName = input.nextLine();

                    // Send the command
                    ByteBuffer downloadCommand = ByteBuffer.wrap(command.getBytes());
                    channel.write(downloadCommand);

                    // Send the file name to the server
                    ByteBuffer downloadFileNameBuffer = ByteBuffer.wrap(downloadFileName.getBytes());
                    channel.write(downloadFileNameBuffer);

                    // Receive and display the server's response
                    ByteBuffer downloadResponseBuffer = ByteBuffer.allocate(1);
                    channel.read(downloadResponseBuffer);
                    downloadResponseBuffer.flip();
                    byte downloadResponse = downloadResponseBuffer.get();

                    if (downloadResponse == 'S') {
                        // Receive and save the file data
                        FileOutputStream fileOutputStream = new FileOutputStream(downloadFileName);
                        ByteBuffer fileData = ByteBuffer.allocate(1024);
                        int bytesReceived;

                        while ((bytesReceived = channel.read(fileData)) > 0) {
                            fileData.flip();
                            byte[] dataBytes = new byte[bytesReceived];
                            fileData.get(dataBytes);
                            fileOutputStream.write(dataBytes);
                            fileData.clear();
                        }

                        fileOutputStream.close();
                        System.out.println("Download successful.");
                    } else {
                        System.out.println("File not found on the server.");
                    }
                    break;
                case "R":
                    System.out.println("Please enter the old file name:");
                    String oldFileName = input.nextLine();
                    System.out.println("Please enter the new file name:");
                    String newFileName = input.nextLine();

                    // Send the command
                    ByteBuffer renameCommand = ByteBuffer.wrap(command.getBytes());
                    channel.write(renameCommand);

                    // Send the old and new file names to the server
                    ByteBuffer oldFileNameBuffer = ByteBuffer.wrap(oldFileName.getBytes());
                    channel.write(oldFileNameBuffer);
                    ByteBuffer newFileNameBuffer = ByteBuffer.wrap(newFileName.getBytes());
                    channel.write(newFileNameBuffer);

                    // Receive and display the server's response
                    ByteBuffer renameResponseBuffer = ByteBuffer.allocate(1);
                    channel.read(renameResponseBuffer);
                    renameResponseBuffer.flip();
                    byte renameResponse = renameResponseBuffer.get();

                    if (renameResponse == 'S') {
                        System.out.println("Rename successful.");
                    } else {
                        System.out.println("Rename failed.");
                    }
                    break;
                case "L":
                    // Send the command
                    ByteBuffer listCommand = ByteBuffer.wrap(command.getBytes());
                    channel.write(listCommand);

                    // Receive and display the server's response
                    ByteBuffer listResponseBuffer = ByteBuffer.allocate(1024); // Adjust the buffer size as needed

                    while ((bytesRead = channel.read(listResponseBuffer)) > 0) {
                        listResponseBuffer.flip();
                        byte[] dataBytes = new byte[bytesRead];
                        listResponseBuffer.get(dataBytes);
                        System.out.println(new String(dataBytes));
                        listResponseBuffer.clear();
                    }

                    if (bytesRead == -1) {
                        System.out.println("List request completed.");
                    } else {
                        System.out.println("Error reading list response.");
                    }
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