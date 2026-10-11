package baitap5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BÀI TẬP 5: TCP Chat Server phục vụ nhiều client
 * 
 * Đặc tả:
 * - Dùng Thread Pool (ExecutorService) để phục vụ nhiều client đồng thời.
 * - Dùng ConcurrentHashMap để quản lý danh sách Nickname và PrintWriter an toàn đa luồng (thread-safe).
 * - Client đăng ký Nickname duy nhất khi mới kết nối.
 * - Lệnh USERS: Trả về danh sách nickname online.
 * - Lệnh MSG <nội dung>: Phát tin nhắn tới tất cả client khác (Broadcast).
 * - Lệnh QUIT: Thoát kết nối sạch sẽ.
 * - Xử lý ngắt đột ngột: Bắt ngoại lệ và xóa client khỏi danh sách.
 */
public class ChatServer {

    public static final int PORT = 5006;
    private static final int MAX_CLIENTS = 50;

    // Cấu trúc Thread-safe lưu trữ danh sách người dùng: Key = nickname, Value = luồng gửi PrintWriter
    private static final ConcurrentHashMap<String, PrintWriter> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("ChatServer đang chạy trên cổng " + PORT);

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            System.err.println("Lỗi Chat Server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        String nickname = null;
        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            out.println("Chào mừng bạn đến Chat Server! Vui lòng nhập Nickname của bạn:");

            // 1. Giai đoạn đăng ký Nickname duy nhất
            while (true) {
                String input = in.readLine();
                if (input == null) return;

                String candidate = input.trim();
                if (candidate.isEmpty() || candidate.contains(" ")) {
                    out.println("ERR Nickname không được để trống hoặc chứa khoảng trắng. Vui lòng nhập lại:");
                    continue;
                }

                // Dùng putIfAbsent của ConcurrentHashMap để đảm bảo tính nguyên tử (atomic) chống trùng nickname
                if (activeClients.putIfAbsent(candidate, out) == null) {
                    nickname = candidate;
                    out.println("OK CHAT_READY Chào mừng " + nickname + "! Các lệnh: USERS, MSG <nội dung>, QUIT");
                    broadcast("[HỆ THỐNG] Người dùng @" + nickname + " vừa tham gia phòng chat!", nickname);
                    break;
                } else {
                    out.println("ERR NICK_TAKEN Nickname này đã có người dùng, vui lòng chọn tên khác:");
                }
            }

            // 2. Vòng lặp nhận và xử lý lệnh từ Client
            String line;
            while ((line = in.readLine()) != null) {
                String trimmed = line.trim();

                // Lệnh QUIT
                if (trimmed.equalsIgnoreCase("QUIT")) {
                    out.println("OK BYE");
                    break;
                }

                // Lệnh xem danh sách USERS
                if (trimmed.equalsIgnoreCase("USERS")) {
                    out.println("OK USERS " + String.join(", ", activeClients.keySet()));
                }
                // Lệnh gửi tin nhắn: MSG nội dung
                else if (trimmed.toUpperCase().startsWith("MSG ")) {
                    String messageContent = trimmed.substring(4).trim();
                    if (!messageContent.isEmpty()) {
                        broadcast("[" + nickname + "]: " + messageContent, nickname);
                        out.println("OK SENT");
                    }
                } else {
                    out.println("ERR Cú pháp không hợp lệ. Hãy dùng USERS, MSG <nội dung>, hoặc QUIT");
                }
            }

        } catch (IOException e) {
            System.err.println("Client [" + nickname + "] bị ngắt kết nối đột ngột: " + e.getMessage());
        } finally {
            // 3. Dọn dẹp tài nguyên khi client rời đi hoặc đứt mạng
            if (nickname != null) {
                activeClients.remove(nickname);
                System.out.println("[-] Người dùng @" + nickname + " đã thoát.");
                broadcast("[HỆ THỐNG] @" + nickname + " đã rời khỏi phòng chat.", nickname);
            }
        }
    }

    /**
     * Gửi tin nhắn tới tất cả client (ngoại trừ người gửi)
     */
    private static void broadcast(String message, String senderNickname) {
        for (var entry : activeClients.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(senderNickname)) {
                try {
                    entry.getValue().println(message);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
