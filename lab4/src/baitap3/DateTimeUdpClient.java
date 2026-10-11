package baitap3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 3: UDP Client tra cứu ngày giờ
 * 
 * SO SÁNH HÀNH VI KHI SERVER DỪNG GIỮA CHỪNG:
 * - Đối với TCP: Kết nối là liên tục (stateful). Khi server dừng đột ngột, TCP gửi gói RST hoặc đóng kênh,
 *   phía Client đọc được ngay giá trị `null` từ readLine() hoặc bắn ra ngoại lệ SocketException/IOException.
 * - Đối với UDP: Không có kết nối (stateless). Phía Client hoàn toàn không biết Server đã tắt hay còn sống,
 *   gói tin gửi đi biến mất trong không gian mạng và Client sẽ bị treo nếu không có timeout.
 *   Nhờ có socket.setSoTimeout(3000), Client sẽ thoát sau 3 giây với SocketTimeoutException.
 */
public class DateTimeUdpClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DateTimeUdpServer.PORT;

        try (DatagramSocket socket = new DatagramSocket();
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            // Cài đặt timeout 3 giây để tránh bị treo khi server dừng
            socket.setSoTimeout(3000);
            InetAddress serverAddr = InetAddress.getByName(host);

            System.out.println("Sẵn sàng gửi truy vấn tới DateTimeUdpServer (" + host + ":" + port + ")");
            System.out.println("Các lệnh: DATE, TIME, DATETIME (Gõ EXIT để dừng client):");
            System.out.print("> ");

            String line;
            byte[] receiveBuffer = new byte[4096];

            while ((line = console.readLine()) != null) {
                if (line.equalsIgnoreCase("EXIT")) {
                    break;
                }

                byte[] sendBytes = line.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, serverAddr, port);
                socket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                    String response = new String(
                            receivePacket.getData(),
                            receivePacket.getOffset(),
                            receivePacket.getLength(),
                            StandardCharsets.UTF_8);
                    System.out.println("UDP Server: " + response);
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: Sau 3 giây không có phản hồi (Server có thể đã tắt)!");
                }

                System.out.print("> ");
            }

        } catch (Exception e) {
            System.err.println("Lỗi UDP Client: " + e.getMessage());
        }
    }
}
