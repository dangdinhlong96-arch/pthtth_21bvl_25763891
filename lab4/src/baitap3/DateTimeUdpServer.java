package baitap3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

/**
 * BÀI TẬP 3: Dịch vụ ngày giờ qua UDP
 * 
 * Sử dụng DatagramSocket không hướng kết nối trên cổng 5004.
 * Phục vụ các yêu cầu độc lập: DATE, TIME, DATETIME.
 */
public class DateTimeUdpServer {

    public static final int PORT = 5004;

    public static void main(String[] args) {
        byte[] buffer = new byte[4096];

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("DateTimeUdpServer đang lắng nghe tại cổng UDP " + PORT);

            while (true) {
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                String cmd = new String(
                        requestPacket.getData(),
                        requestPacket.getOffset(),
                        requestPacket.getLength(),
                        StandardCharsets.UTF_8).trim().toUpperCase();

                System.out.println("Nhận lệnh UDP từ [" + requestPacket.getAddress() + ":" + requestPacket.getPort() + "]: " + cmd);

                // Tái sử dụng logic xử lý từ DateTimeTcpServer
                String responseText = DateTimeTcpServer.processCommand(cmd);
                byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        requestPacket.getAddress(),
                        requestPacket.getPort());

                socket.send(responsePacket);
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server UDP: " + e.getMessage());
        }
    }
}
