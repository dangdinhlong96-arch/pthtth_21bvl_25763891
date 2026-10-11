package muc4_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Mục 4.2: TCP Server theo giao thức dòng (Line-based protocol)
 * 
 * Server lắng nghe cổng 5000, xử lý từng kết nối tuần tự từ Client.
 * Mỗi lệnh và phản hồi là một dòng văn bản UTF-8 kết thúc bằng dấu xuống dòng (\n).
 */
public class TcpCommandServer {

    // Khai báo cổng tĩnh theo chuẩn đề bài (cổng 5000 cho TCP)
    public static final int PORT = 5000;

    public static void main(String[] args) {
        // Dùng try-with-resources để ServerSocket tự động đóng khi chương trình dừng
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("TCP server đang lắng nghe tại cổng " + PORT);

            // Vòng lặp vô tận để liên tục chờ đợi client mới kết nối
            while (true) {
                // server.accept() sẽ tạm dừng chờ cho đến khi có một client kết nối tới
                try (Socket socket = server.accept()) {
                    System.out.println("Client đã kết nối: " + socket.getRemoteSocketAddress());
                    // Gọi hàm xử lý yêu cầu của client
                    serve(socket);
                } catch (IOException e) {
                    System.err.println("Lỗi phiên client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Không mở được server: " + e.getMessage());
        }
    }

    /**
     * Hàm phục vụ một client cụ thể thông qua Socket.
     * Hàm này được để dạng public static để lớp MultiClientTcpServer ở mục 4.3 có thể tái sử dụng.
     */
    public static void serve(Socket socket) throws IOException {
        // Đọc dữ liệu từ client gửi lên (dùng UTF-8 để không bị lỗi tiếng Việt)
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
             // Gửi dữ liệu phản hồi về client (true = tự động đẩy dữ liệu đi - auto flush)
             PrintWriter out = new PrintWriter(new OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String request;
            // Đọc từng dòng text mà client gửi tới
            while ((request = in.readLine()) != null) {
                // Xử lý nội dung lệnh và tạo chuỗi phản hồi
                String response = process(request);
                
                // Gửi phản hồi về cho client
                out.println(response);

                // Nếu client gửi lệnh QUIT thì ngắt vòng lặp để đóng kết nối phiên này
                if (request.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        }
    }

    /**
     * Hàm phân tích cú pháp lệnh của giao thức:
     * - PING -> OK PONG
     * - TIME -> OK <ngày giờ hiện tại>
     * - QUIT -> OK BYE
     * - UPPER <chuỗi> -> OK <CHUỖI_IN_HOA>
     * - Lệnh khác -> ERR UNKNOWN_COMMAND
     */
    public static String process(String request) {
        String trimmed = request.trim();

        // 1. Kiểm tra lệnh PING
        if (trimmed.equalsIgnoreCase("PING")) {
            return "OK PONG";
        }

        // 2. Kiểm tra lệnh TIME
        if (trimmed.equalsIgnoreCase("TIME")) {
            return "OK " + LocalDateTime.now();
        }

        // 3. Kiểm tra lệnh QUIT
        if (trimmed.equalsIgnoreCase("QUIT")) {
            return "OK BYE";
        }

        // 4. Kiểm tra lệnh UPPER (bắt đầu bằng 'UPPER ')
        if (trimmed.regionMatches(true, 0, "UPPER ", 0, 6)) {
            // Cắt lấy phần chuỗi từ sau chữ 'UPPER ' và đổi sang chữ hoa
            String content = trimmed.substring(6);
            return "OK " + content.toUpperCase(Locale.ROOT);
        }

        // 5. Nếu không khớp bất kỳ lệnh nào ở trên
        return "ERR UNKNOWN_COMMAND";
    }
}
