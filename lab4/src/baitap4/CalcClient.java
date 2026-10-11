package baitap4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 4: TCP Client cho máy tính từ xa CalcServer
 */
public class CalcClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : CalcServer.PORT;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Kết nối thành công tới CalcServer (" + host + ":" + port + ")");
            System.out.println("Cú pháp: CALC <toán_tử> <số_1> <số_2> (Ví dụ: CALC + 100 200)");
            System.out.println("Nhập QUIT để kết thúc.");
            System.out.print("> ");

            String line;
            while ((line = console.readLine()) != null) {
                out.println(line);
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }
                System.out.println("Server: " + response);

                if (line.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
                System.out.print("> ");
            }

        } catch (IOException e) {
            System.err.println("Lỗi kết nối tới CalcServer: " + e.getMessage());
        }
    }
}
