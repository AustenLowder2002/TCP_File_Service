package file_service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;

public class TCPFileServiceServer {
    public static void main(String[] args) throws Exception {
        int serverPort = 3000;
        ServerSocketChannel welcomeChannel =
                ServerSocketChannel.open();
        welcomeChannel.socket().bind(new InetSocketAddress(serverPort));
        while (true) {
            SocketChannel serveChannel = welcomeChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = 0;
            do {
                numBytes = serveChannel.read(request);
            } while (numBytes >= 0);

            while (serveChannel.read(request) >= 0) {

                request.flip();
                char command = (char) request.get();
                System.out.println("received command: " + command);
                switch (command) {
                    case 'D':
                        byte[] a = new byte[request.remaining()];
                        request.get(a);
                        String fileName = new String(a);
                        System.out.println("file to delete: " + fileName);
                        File file = new File("ServerFiles/" + fileName);
                        boolean success = false;
                        if (file.exists()) {
                            success = file.delete();
                        }
                        if (success) {
                            ByteBuffer deleteCode =
                                    ByteBuffer.wrap("S".getBytes());
                            serveChannel.write(deleteCode);
                        } else {
                            ByteBuffer deleteCode =
                                    ByteBuffer.wrap("F".getBytes());
                            serveChannel.write(deleteCode);
                        }
                        serveChannel.close();
                        break;
                    case 'U':
                        byte[] uploadFilenameBytes = new byte[request.remaining()];
                        request.get(uploadFilenameBytes);
                        String uploadFilename = new String(uploadFilenameBytes);
                        System.out.println("Uploading file: " + uploadFilename);

                        ByteBuffer fileContents = ByteBuffer.allocate(1024);

                        while (serveChannel.read(fileContents) >= 0) {
                        }

                        File uploadFile = new File("ServerFiles/" + uploadFilename);
                        FileOutputStream fileOutputStream = new FileOutputStream(uploadFile);
                        fileOutputStream.write(fileContents.array());


                        ByteBuffer uploadCode = ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(uploadCode);
                        serveChannel.close();
                        break;

                    case 'R':

                        byte[] oldFilenameBytes = new byte[request.remaining()];
                        request.get(oldFilenameBytes);
                        String oldFilename = new String(oldFilenameBytes);

                        byte[] newFilenameBytes = new byte[request.remaining()];
                        request.get(newFilenameBytes);
                        String newFilename = new String(newFilenameBytes);

                        System.out.println("Renaming file: " + oldFilename + " to " + newFilename);

                        File oldFile = new File("ServerFiles/" + oldFilename);
                        File newFile = new File("ServerFiles/" + newFilename);

                        boolean renameSuccess = oldFile.renameTo(newFile);

                        if (renameSuccess) {
                            ByteBuffer renameCode = ByteBuffer.wrap("S".getBytes());
                            serveChannel.write(renameCode);
                        } else {
                            ByteBuffer renameCode = ByteBuffer.wrap("F".getBytes());
                            serveChannel.write(renameCode);
                        }

                        serveChannel.close();
                        break;

                    case 'G':
                        byte[] downloadFileBytes = new byte[request.remaining()];
                        request.get(downloadFileBytes);
                        String downloadFilename = new String(downloadFileBytes);
                        System.out.println("Downloading file: " + downloadFilename);

                        File downloadFile = new File("ServerFiles/" + downloadFilename);

                        if (downloadFile.exists()) {
                            byte[] fileContent = Files.readAllBytes(downloadFile.toPath());
                            serveChannel.write(ByteBuffer.wrap(fileContent));
                        } else {
                            ByteBuffer downloadCode = ByteBuffer.wrap("F".getBytes());
                            serveChannel.write(downloadCode);
                        }

                        serveChannel.close();
                        break;

                    case 'L':
                        File serverDirectory = new File("ServerFiles");
                        File[] fileList = serverDirectory.listFiles();

                        if (fileList != null && fileList.length > 0) {
                            StringBuilder fileListStr = new StringBuilder();
                            for (File fileItem : fileList) {
                                fileListStr.append(fileItem.getName()).append("\n");
                            }
                            ByteBuffer listResponse = ByteBuffer.wrap(fileListStr.toString().getBytes());
                            serveChannel.write(listResponse);
                        } else {
                            ByteBuffer emptyListResponse = ByteBuffer.wrap("No files available.".getBytes());
                            serveChannel.write(emptyListResponse);
                        }
                        serveChannel.close();
                        break;
                }

            }
        }
    }

}
