package file_service;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.*;

public class TCPFileServiceServer {
    private static final int PORT = 12345;
    private static String baseDirectory = "server_files/";
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                // Handle client requests in a separate thread
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] command = inputLine.split(" ");
                    String response = "";
                    if (command[0].equals("UPLOAD")) {
                        response = handleUpload(command[1]);
                    } else if (command[0].equals("DOWNLOAD")) {
                        response = handleDownload(command[1]);
                    } else if (command[0].equals("LIST")) {
                        response = handleList();
                    } else if (command[0].equals("RENAME")) {
                        response = handleRename(command[1], command[2]);
                    } else if (command[0].equals("DELETE")) {
                        response = handleDelete(command[1]);
                    }
                    out.println(response);
                }
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private String handleUpload(String fileName) {
            // Implement file upload logic here
            return "File upload handled for " + fileName;
        }
        private String handleDownload(String fileName) {
            // Implement file download logic here
            return "File download handled for " + fileName;
        }
        private String handleList() {
            File directory = new File(baseDirectory);
            File[] files = directory.listFiles();
            if (files != null) {
                StringBuilder fileList = new StringBuilder();
                for (File file : files) {
                    fileList.append(file.getName()).append("\n");
                }
                return fileList.toString();
            } else {
                return "No files available.";
            }
        }
        private String handleRename(String oldName, String newName) {
            // Implement file rename logic here
            return "File renamed from " + oldName + " to " + newName;
        }
        private String handleDelete(String fileName) {
            // Implement file delete logic here
            return "File deleted: " + fileName;
        }
    }
}