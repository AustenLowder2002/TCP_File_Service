package file_service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPFileServiceSever {
    public static void main(String[] args) throws Exception{
        int port = 3000;
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            // Create input stream to read file
            InputStream inputStream = clientSocket.getInputStream();

            // Specify where to save the uploaded file
            FileOutputStream fileOutputStream = new FileOutputStream("received_file.txt");

            // Create a buffer for reading from the socket and writing to the file
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read from the socket and write to the file
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File received successfully.");

            // Close streams and socket
            fileOutputStream.close();
            inputStream.close();
            clientSocket.close();
        }
    }
}
