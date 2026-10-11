package baitap2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 2: Client gửi ký tự số sang NumberToWordServer
 */
public class NumberToWordClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : NumberToWordServer.PORT;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Kết nối thành công tới NumberToWordServer (" + host + ":" + port + ")");
            System.out.println("Nhập chữ số từ 0 - 9 để đọc thành chữ, hoặc nhập QUIT để thoát:");
            System.out.print("> ");

            String input;
            while ((input = console.readLine()) != null) {
                out.println(input);
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }
                System.out.println("Server: " + response);

                if (input.equalsIgnoreCase("QUIT")) {
                    break;
                }
                System.out.print("> ");
            }

        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
    }
}
