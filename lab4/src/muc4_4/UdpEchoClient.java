package muc4_4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Mục 4.4: UDP Echo Client
 * 
 * Gửi một thông điệp datagram tới UDP Echo Server và chờ nhận phản hồi.
 * Cài đặt cơ chế setSoTimeout(3000) để không bị treo máy nếu server chưa chạy hoặc gói tin bị thất lạc trên mạng.
 */
public class UdpEchoClient {

    public static void main(String[] args) throws Exception {
        // Đọc các tham số dòng lệnh hoặc dùng giá trị mặc định
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5001;
        String message = args.length > 2 ? args[2] : "xin chào UDP";

        // Chuyển chuỗi thông điệp thành mảng byte UTF-8
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        InetAddress serverAddress = InetAddress.getByName(host);

        // Tạo DatagramSocket ở cổng ngẫu nhiên do hệ điều hành cấp phát
        try (DatagramSocket socket = new DatagramSocket()) {
            // Thiết lập thời gian chờ tối đa 3 giây (3000 ms)
            socket.setSoTimeout(3000);

            // Đóng gói và gửi dữ liệu tới Server
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, port);
            socket.send(sendPacket);
            System.out.println("Đã gửi tới " + host + ":" + port + " -> \"" + message + "\"");

            // Tạo bộ đệm 4KB để nhận phản hồi từ server
            byte[] buffer = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

            try {
                // Nhận phản hồi
                socket.receive(responsePacket);

                // Trích xuất nội dung từ phản hồi
                String responseText = new String(
                        responsePacket.getData(),
                        responsePacket.getOffset(),
                        responsePacket.getLength(),
                        StandardCharsets.UTF_8);

                System.out.println("Phản hồi từ Server: " + responseText);

            } catch (SocketTimeoutException e) {
                // Xử lý khi quá 3 giây không nhận được phản hồi
                System.err.println("Hết 3 giây nhưng chưa nhận được phản hồi từ Server (Gói tin có thể bị mất hoặc Server chưa bật)");
            }
        }
    }
}
