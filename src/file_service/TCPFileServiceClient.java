package file_service;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TCPFileServiceClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Syntax: TCPFileServiceClient <ServerIP> <ServerPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        InetAddress serverIP = InetAddress.getByName(args[0]);
        String command = "";

        do {
            Scanner input = new Scanner(System.in);
            System.out.println("Please type a command: ");
            command = input.nextLine().toUpperCase();
            SocketChannel channel = null;

            switch (command) {
                case "D":
                    System.out.println("Please enter the file name:");
                    String filename = input.nextLine();
                    ByteBuffer request =
                            ByteBuffer.wrap((command + filename).getBytes());
                    channel = SocketChannel.open();
                    channel.connect(
                            new InetSocketAddress(serverIP, serverPort));
                    channel.write(request);
                    channel.shutdownOutput();

                    int bytesRead = 1;
                    ByteBuffer statusCode = ByteBuffer.allocate(bytesRead);
                    while ((bytesRead -= channel.read(statusCode)) > 0) ;
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
                        channel = SocketChannel.open();
                        channel.connect(
                                new InetSocketAddress(serverIP, serverPort));

                        ByteBuffer uploadCommand = ByteBuffer.wrap(command.getBytes());
                        channel.write(uploadCommand);

                        ByteBuffer fileNameBuffer = ByteBuffer.wrap(uploadFileName.getBytes());
                        channel.write(fileNameBuffer);

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
                    channel = SocketChannel.open();
                    channel.connect(
                            new InetSocketAddress(serverIP, serverPort));

                    ByteBuffer downloadCommand = ByteBuffer.wrap(command.getBytes());
                    channel.write(downloadCommand);

                    ByteBuffer downloadFileNameBuffer = ByteBuffer.wrap(downloadFileName.getBytes());
                    channel.write(downloadFileNameBuffer);

                    ByteBuffer downloadResponseBuffer = ByteBuffer.allocate(1);
                    channel.read(downloadResponseBuffer);
                    downloadResponseBuffer.flip();
                    byte downloadResponse = downloadResponseBuffer.get();

                    if (downloadResponse == 'S') {
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
                    channel = SocketChannel.open();
                    channel.connect(
                            new InetSocketAddress(serverIP, serverPort));

                    ByteBuffer renameCommand = ByteBuffer.wrap(command.getBytes());
                    channel.write(renameCommand);

                    ByteBuffer oldFileNameBuffer = ByteBuffer.wrap(oldFileName.getBytes());
                    channel.write(oldFileNameBuffer);
                    ByteBuffer newFileNameBuffer = ByteBuffer.wrap(newFileName.getBytes());
                    channel.write(newFileNameBuffer);

                    ByteBuffer renameResponseBuffer = ByteBuffer.allocate(1);
                    channel.read(renameResponseBuffer);
                    renameResponseBuffer.flip();
                    byte renameCode = renameResponseBuffer.get();

                    if (renameCode == 'S') {
                        System.out.println("Rename successful.");
                    } else {
                        System.out.println("Rename failed.");
                    }
                    break;
                case "L":
                    channel = SocketChannel.open();
                    channel.connect(
                            new InetSocketAddress(serverIP, serverPort));

                    ByteBuffer listCommand = ByteBuffer.wrap(command.getBytes());
                    channel.write(listCommand);

                    ByteBuffer listOutput = ByteBuffer.allocate(1024);

                    while ((bytesRead = channel.read(listOutput)) > 0) {
                        listOutput.flip();
                        byte[] dataBytes = new byte[bytesRead];
                        listOutput.get(dataBytes);
                        System.out.println(new String(dataBytes));
                        listOutput.clear();
                    }

                    if (bytesRead == -1) {
                        System.out.println("List request completed.");
                    } else {
                        System.out.println("Error reading list response.");
                    }
                    break;
                default:
                    if (!command.equals("Q")) {
                        System.out.println("Unknown command");
                    }
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } while (!command.equals("Q"));
    }
}
