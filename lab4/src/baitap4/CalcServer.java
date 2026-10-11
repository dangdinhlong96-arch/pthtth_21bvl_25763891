package baitap4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 4: Máy tính từ xa qua TCP
 * 
 * Định dạng yêu cầu:
 *   CALC <toán_tử> <toán_hạng_1> <toán_hạng_2>
 * Ví dụ:
 *   CALC + 100 200 -> OK 300
 *   CALC / 10 0    -> ERR DIVIDE_BY_ZERO
 *   CALC % 10 3    -> ERR UNSUPPORTED_OPERATOR
 *   CALC + a 2     -> ERR INVALID_NUMBER
 *   Thiếu tham số  -> ERR INVALID_FORMAT
 */
public class CalcServer {

    public static final int PORT = 5005;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("CalcServer đang lắng nghe trên cổng " + PORT);

            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    System.out.println("Client kết nối: " + socket.getRemoteSocketAddress());

                    String line;
                    while ((line = in.readLine()) != null) {
                        String trimmed = line.trim();
                        if (trimmed.equalsIgnoreCase("QUIT")) {
                            out.println("OK BYE");
                            break;
                        }

                        String response = calculate(trimmed);
                        out.println(response);
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi phiên client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        }
    }

    /**
     * Hàm phân tích chuỗi tính toán và xử lý lỗi chặt chẽ theo đặc tả đề bài
     */
    public static String calculate(String expression) {
        if (expression == null || expression.isEmpty()) {
            return "ERR INVALID_FORMAT";
        }

        // Tách chuỗi theo một hoặc nhiều dấu khoảng trắng
        String[] parts = expression.trim().split("\\s+");

        // Cú pháp bắt buộc: CALC <op> <n1> <n2> (đúng 4 phần tử)
        if (parts.length != 4 || !parts[0].equalsIgnoreCase("CALC")) {
            return "ERR INVALID_FORMAT";
        }

        String operator = parts[1];
        double n1, n2;

        // Kiểm tra xem toán hạng có phải là số hợp lệ không
        try {
            n1 = Double.parseDouble(parts[2]);
            n2 = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }

        // Kiểm tra phép toán
        double result;
        switch (operator) {
            case "+":
                result = n1 + n2;
                break;
            case "-":
                result = n1 - n2;
                break;
            case "*":
                result = n1 * n2;
                break;
            case "/":
                if (n2 == 0) {
                    return "ERR DIVIDE_BY_ZERO";
                }
                result = n1 / n2;
                break;
            default:
                return "ERR UNSUPPORTED_OPERATOR";
        }

        // Trả kết quả: nếu là số nguyên thì in số nguyên (ví dụ 300 thay vì 300.0)
        if (result == (long) result) {
            return "OK " + (long) result;
        } else {
            return "OK " + result;
        }
    }
}
