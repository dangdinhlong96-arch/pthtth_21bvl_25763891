package baitap3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BÀI TẬP 3: Dịch vụ ngày giờ qua TCP
 * 
 * Hỗ trợ các lệnh:
 * - DATE     -> Trả về ngày theo định dạng dd/MM/yyyy
 * - TIME     -> Trả về giờ theo định dạng HH:mm:ss
 * - DATETIME -> Trả về ngày và giờ kết hợp dd/MM/yyyy HH:mm:ss
 * - QUIT     -> Thoát phiên kết nối
 */
public class DateTimeTcpServer {

    public static final int PORT = 5003;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("DateTimeTcpServer đang lắng nghe tại cổng " + PORT);

            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    System.out.println("Client TCP kết nối: " + socket.getRemoteSocketAddress());
                    String line;
                    while ((line = in.readLine()) != null) {
                        String cmd = line.trim().toUpperCase();
                        if (cmd.equals("QUIT")) {
                            out.println("OK BYE");
                            break;
                        }

                        String response = processCommand(cmd);
                        out.println(response);
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi phiên client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server TCP: " + e.getMessage());
        }
    }

    public static String processCommand(String cmd) {
        LocalDateTime now = LocalDateTime.now();
        switch (cmd) {
            case "DATE":
                return "OK " + now.format(DATE_FMT);
            case "TIME":
                return "OK " + now.format(TIME_FMT);
            case "DATETIME":
                return "OK " + now.format(DATETIME_FMT);
            default:
                return "ERR UNKNOWN_COMMAND";
        }
    }
}
