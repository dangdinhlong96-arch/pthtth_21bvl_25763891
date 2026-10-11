package baitap3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 3: TCP Client tra cứu ngày giờ
 */
public class DateTimeTcpClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DateTimeTcpServer.PORT;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Đã kết nối tới DateTimeTcpServer (" + host + ":" + port + ")");
            System.out.println("Các lệnh: DATE, TIME, DATETIME, QUIT");
            System.out.print("> ");

            String cmd;
            while ((cmd = console.readLine()) != null) {
                out.println(cmd);
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Mất kết nối: Server đã dừng hoặc đóng kết nối!");
                    break;
                }
                System.out.println("Server: " + response);

                if (cmd.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
                System.out.print("> ");
            }

        } catch (IOException e) {
            System.err.println("Lỗi I/O Socket: " + e.getMessage());
        }
    }
}
