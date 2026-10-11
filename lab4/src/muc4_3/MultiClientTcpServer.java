package muc4_3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import muc4_2.TcpCommandServer;

/**
 * Mục 4.3: TCP Server phục vụ đồng thời nhiều Client bằng Thread Pool
 * 
 * Thay vì xử lý tuần tự (từng client một), lớp này dùng ExecutorService
 * với Fixed Thread Pool giới hạn 20 luồng. Mỗi khi có client mới kết nối,
 * một luồng trong pool sẽ chịu trách nhiệm phục vụ client đó.
 */
public class MultiClientTcpServer {

    private static final int PORT = 5000;
    // Giới hạn số lượng client phục vụ đồng thời tối đa là 20 để tránh quá tải RAM/CPU
    private static final int MAX_CLIENTS = 20;

    public static void main(String[] args) {
        // Tạo một Thread Pool cố định gồm 20 workers
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Multi-client TCP server đang hoạt động trên port " + PORT);
            System.out.println("Sẵn sàng phục vụ tối đa " + MAX_CLIENTS + " clients cùng lúc...");

            while (true) {
                // Chờ đợi kết nối mới từ client
                Socket socket = server.accept();

                // Giao việc xử lý client này cho 1 thread trong pool
                pool.submit(() -> {
                    String clientAddress = String.valueOf(socket.getRemoteSocketAddress());
                    System.out.println("[+] Client kết nối: " + clientAddress);

                    // Đảm bảo đóng socket khi client ngắt kết nối
                    try (socket) {
                        // Tái sử dụng lại phương thức xử lý giao thức từ TcpCommandServer
                        TcpCommandServer.serve(socket);
                    } catch (IOException e) {
                        System.err.println("[-] Client " + clientAddress + " gặp sự cố: " + e.getMessage());
                    } finally {
                        System.out.println("[-] Client đã ngắt kết nối: " + clientAddress);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        } finally {
            // Khi dừng server thì giải phóng Thread Pool
            pool.shutdown();
        }
    }
}
