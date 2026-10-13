import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    public static final int PORT = 5000;
    public static final String[] ROOMS = {"Study", "Game", "TraiNghiem"};
    private static final List<ClientHandler> clientList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("     SERVER CHAT NHOM TCP DANG KHOI DONG    ");
        System.out.println("============================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(">> Server dang lang nghe tai cong: " + PORT);
            System.out.println(">> San sang tiep nhan cac ket noi...\n");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clientList.add(handler);
                handler.start();
            }

        } catch (IOException e) {
            System.err.println("Loi Server: " + e.getMessage());
        }
    }

    public static void broadcastRoom(String room, String message, ClientHandler sender) {
        for (ClientHandler client : clientList) {
            if (client != sender && client.currentRoom != null && client.currentRoom.equalsIgnoreCase(room)) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clientList.remove(client);
    }

    public static String findRoom(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String trimmed = input.trim();

        try {
            int index = Integer.parseInt(trimmed);
            if (index >= 1 && index <= ROOMS.length) {
                return ROOMS[index - 1];
            }
        } catch (NumberFormatException ignored) {
        }

        for (String room : ROOMS) {
            if (room.equalsIgnoreCase(trimmed)) {
                return room;
            }
        }

        return null;
    }
}
