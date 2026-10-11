package muc4_4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Mục 4.4: UDP Echo Server
 * 
 * Sử dụng DatagramSocket để nhận và gửi các gói tin DatagramPacket không hướng kết nối qua cổng 5001.
 * Khi nhận được một gói tin chuỗi UTF-8, Server chuyển chuỗi thành chữ hoa, gắn tiền tố "ACK "
 * và gửi trả về đúng địa chỉ IP và Port của bên gửi.
 */
public class UdpEchoServer {

    // Cổng dịch vụ UDP theo quy định của tài liệu thực hành
    public static final int PORT = 5001;

    public static void main(String[] args) {
        // Tạo mảng byte đệm để chứa dữ liệu nhận được (4 KB)
        byte[] buffer = new byte[4096];

        // Mở DatagramSocket lắng nghe tại cổng 5001
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("UDP Echo Server đang lắng nghe tại cổng " + PORT);

            while (true) {
                // Tạo gói tin DatagramPacket rỗng để chuẩn bị hứng dữ liệu tới
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                // Chờ và nhận gói tin từ bất kỳ client nào gửi tới (hàm blocking)
                socket.receive(request);

                // Giải mã dữ liệu nhận được thành chuỗi UTF-8.
                // LƯU Ý QUAN TRỌNG: Phải dùng getOffset() và getLength() để chỉ lấy đúng số byte thực nhận!
                String message = new String(
                        request.getData(),
                        request.getOffset(),
                        request.getLength(),
                        StandardCharsets.UTF_8);

                System.out.println("Nhận từ [" + request.getAddress() + ":" + request.getPort() + "]: " + message);

                // Chuẩn bị dữ liệu phản hồi: Chuyển sang chữ hoa và thêm tiền tố "ACK "
                String text = "ACK " + message.toUpperCase(Locale.ROOT);
                byte[] responseData = text.getBytes(StandardCharsets.UTF_8);

                // Đóng gói dữ liệu vào DatagramPacket kèm IP và Port của Client vừa gửi tới
                DatagramPacket response = new DatagramPacket(
                        responseData,
                        responseData.length,
                        request.getAddress(),
                        request.getPort());

                // Gửi gói tin phản hồi đi
                socket.send(response);
            }
        } catch (IOException e) {
            System.err.println("Lỗi UDP Server: " + e.getMessage());
        }
    }
}
