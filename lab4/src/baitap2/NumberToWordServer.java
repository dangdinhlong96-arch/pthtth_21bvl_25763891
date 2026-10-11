package baitap2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 2: TCP Server đổi chữ số thành chữ tiếng Việt
 * 
 * Yêu cầu:
 * - Client gửi 1 ký tự từ '0' đến '9'.
 * - Server trả về cách đọc tiếng Việt tương ứng ("không", "một", "hai", ...).
 * - Nếu không phải đúng duy nhất 1 ký tự số, trả về "ERR INVALID_DIGIT".
 * - Hỗ trợ nhiều yêu cầu liên tục cho đến khi gửi "QUIT".
 * - Bắt buộc dùng mã hóa UTF-8 và giao thức dòng.
 */
public class NumberToWordServer {

    public static final int PORT = 5002;

    private static final String[] WORDS = {
        "không", "một", "hai", "ba", "bốn",
        "năm", "sáu", "bảy", "tám", "chín"
    };

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("NumberToWordServer đang chạy trên cổng " + PORT);

            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    System.out.println("Client đã kết nối: " + socket.getRemoteSocketAddress());

                    String line;
                    while ((line = in.readLine()) != null) {
                        // Kiểm tra lệnh thoát
                        if (line.equalsIgnoreCase("QUIT")) {
                            out.println("OK BYE");
                            break;
                        }

                        String response = convertDigit(line);
                        out.println(response);
                    }
                    System.out.println("Client đã ngắt kết nối: " + socket.getRemoteSocketAddress());

                } catch (IOException e) {
                    System.err.println("Lỗi phiên client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Không thể khởi động server: " + e.getMessage());
        }
    }

    /**
     * Hàm kiểm tra và chuyển đổi số thành chữ:
     * - Đúng 1 ký tự '0' -> '9': trả về từ tiếng Việt.
     * - Các trường hợp biên (chuỗi rỗng, '10', 'a', có khoảng trắng " 5 "): trả về "ERR INVALID_DIGIT".
     */
    public static String convertDigit(String input) {
        // Kiểm tra đúng độ dài 1 ký tự (không trim trước để phát hiện dữ liệu chứa khoảng trắng)
        if (input == null || input.length() != 1) {
            return "ERR INVALID_DIGIT";
        }

        char c = input.charAt(0);
        if (c >= '0' && c <= '9') {
            int digit = c - '0';
            return "OK " + WORDS[digit];
        }

        return "ERR INVALID_DIGIT";
    }
}
