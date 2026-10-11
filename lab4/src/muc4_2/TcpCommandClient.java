package muc4_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Mục 4.2: TCP Client gửi lệnh tới TcpCommandServer
 * 
 * Cho phép người dùng nhập các lệnh từ bàn phím (console) và gửi tới Server,
 * sau đó nhận và hiển thị phản hồi từ Server.
 */
public class TcpCommandClient {

    public static void main(String[] args) {
        // Lấy host và port từ tham số dòng lệnh nếu có, ngược lại dùng mặc định
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        // Quản lý Socket và các luồng I/O tự động đóng bằng try-with-resources
        try (Socket socket = new Socket(host, port);
             // Luồng đọc dữ liệu người dùng gõ từ bàn phím (System.in)
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8));
             // Luồng đọc phản hồi từ Server gửi về qua Socket
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             // Luồng gửi lệnh lên Server qua Socket (autoFlush = true)
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Đã kết nối tới Server (" + host + ":" + port + ")");
            System.out.println("Các lệnh hợp lệ: PING, TIME, UPPER <văn bản>, QUIT");
            System.out.print("> ");

            String request;
            // Đọc từng dòng người dùng nhập từ bàn phím
            while ((request = console.readLine()) != null) {
                // Gửi dòng lệnh này sang server
                out.println(request);

                // Chờ và đọc 1 dòng phản hồi từ server
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }

                System.out.println("Server phản hồi: " + response);

                // Nếu người dùng vừa gõ lệnh QUIT thì thoát chương trình client
                if (request.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
                System.out.print("> ");
            }

        } catch (NumberFormatException e) {
            System.err.println("Cổng (port) phải là số nguyên hợp lệ!");
        } catch (IOException e) {
            System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
        }
    }
}
