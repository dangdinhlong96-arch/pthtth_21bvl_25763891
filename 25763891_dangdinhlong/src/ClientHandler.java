import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public String clientName;
    public String currentRoom = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String receivedName = in.readLine();
            clientName = (receivedName == null || receivedName.trim().isEmpty())
                    ? "User_" + socket.getPort()
                    : receivedName.trim();

            System.out.println("[+] " + clientName + " da ket noi");

            sendRoomList();

            String line;
            while ((line = in.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (trimmed.equalsIgnoreCase("/exit")) {
                    out.println("[HE THONG] Ban da ngat ket noi.");
                    break;
                }

                if (currentRoom == null) {
                    if (trimmed.equalsIgnoreCase("/quit")) {
                        out.println("[HE THONG] Ban chua o trong phong nao.");
                        sendRoomList();
                        continue;
                    }

                    String matchedRoom = ChatServer.findRoom(trimmed);
                    if (matchedRoom != null) {
                        currentRoom = matchedRoom;
                        System.out.println("[LOG] " + clientName + " vao phong " + currentRoom);
                        out.println("[HE THONG] Ban da tham gia phong: " + currentRoom);
                        out.println("[HE THONG] Nhap tin nhan de chat, /quit de doi phong, /exit de thoat.");
                        ChatServer.broadcastRoom(currentRoom, "[HE THONG] " + clientName + " da vao phong chat!", this);
                    } else {
                        out.println("[LOI] Phong khong ton tai, vui long chon lai:");
                        sendRoomList();
                    }
                } else {
                    if (trimmed.equalsIgnoreCase("/quit")) {
                        String leftRoom = currentRoom;
                        currentRoom = null;
                        System.out.println("[LOG] " + clientName + " roi phong " + leftRoom);
                        ChatServer.broadcastRoom(leftRoom, "[HE THONG] " + clientName + " da roi phong chat.", this);
                        out.println("[HE THONG] Ban da roi phong: " + leftRoom);
                        sendRoomList();
                    } else {
                        String chatMessage = "[" + clientName + "]: " + trimmed;
                        ChatServer.broadcastRoom(currentRoom, chatMessage, this);
                        System.out.println("[" + currentRoom + "] " + chatMessage);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("[-] " + clientName + " bi ngat ket noi");
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendRoomList() {
        out.println("\n--- DANH SACH PHONG CHAT ---");
        for (int i = 0; i < ChatServer.ROOMS.length; i++) {
            out.println("  " + (i + 1) + ". " + ChatServer.ROOMS[i]);
        }
        out.println(">> Nhap so (1-" + ChatServer.ROOMS.length + ") hoac ten phong de tham gia:");
    }

    public void disconnect() {
        ChatServer.removeClient(this);

        if (currentRoom != null) {
            ChatServer.broadcastRoom(currentRoom, "[HE THONG] " + clientName + " da roi phong chat.", this);
            currentRoom = null;
        }

        System.out.println("[-] Da dong ket noi voi: " + (clientName != null ? clientName : "Unknown"));

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
