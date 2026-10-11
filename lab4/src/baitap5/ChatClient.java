package baitap5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 5: TCP Chat Client đa luồng
 * 
 * - 1 Luồng phụ (Reader Thread): Chuyên lắng nghe tin nhắn từ Server và in ra màn hình.
 * - Luồng chính (Main Thread): Nhận phím gõ từ bàn phím và gửi lên Server.
 */
public class ChatClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : ChatServer.PORT;

        try {
            Socket socket = new Socket(host, port);
            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("Đã kết nối tới Chat Server (" + host + ":" + port + ")");

            // Khởi động luồng đọc tin nhắn ngầm từ Server để không chặn việc người dùng gõ phím
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                        if (serverMsg.equalsIgnoreCase("OK BYE")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Ngắt kết nối với Chat Server.");
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Luồng chính: đọc dữ liệu từ bàn phím người dùng và gửi đi
            String userInput;
            while ((userInput = console.readLine()) != null) {
                out.println(userInput);
                if (userInput.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

            // Đợi 500ms rồi đóng socket sạch sẽ
            Thread.sleep(500);
            socket.close();

        } catch (Exception e) {
            System.err.println("Lỗi kết nối tới phòng chat: " + e.getMessage());
        }
    }
}
