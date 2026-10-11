package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import muc4_1.HostInspector;
import muc4_2.TcpCommandServer;
import muc4_3.MultiClientTcpServer;
import muc4_4.UdpEchoServer;
import baitap1.HostUriInspector;
import baitap2.NumberToWordServer;
import baitap3.DateTimeTcpServer;
import baitap3.DateTimeUdpServer;
import baitap4.CalcServer;
import baitap5.ChatServer;

/**
 * Lớp kiểm thử tự động toàn diện tất cả các hoạt động và bài tập trong Lab 4.
 * Kết quả kiểm thử sẽ được in ra console và ghi vào file evidence/test-results.txt.
 */
public class Lab4TestRunner {

    private static final StringBuilder logBuilder = new StringBuilder();

    private static void log(String msg) {
        System.out.println(msg);
        logBuilder.append(msg).append(System.lineSeparator());
    }

    public static void main(String[] args) throws Exception {
        log("================================================================================");
        log("               BÁO CÁO KIỂM THỬ TỰ ĐỘNG LAB 04 - SOCKET TCP & UDP               ");
        log("================================================================================");
        log("Thời gian thực hiện: " + java.time.LocalDateTime.now());
        log("Môi trường: Java " + System.getProperty("java.version") + " (" + System.getProperty("os.name") + ")");
        log("--------------------------------------------------------------------------------\n");

        // 1. Kiểm thử Hoạt động 4.1: HostInspector
        testSection4_1();

        // 2. Kiểm thử Hoạt động 4.2 & 4.3: TCP Server & MultiClient
        testSection4_2_and_4_3();

        // 3. Kiểm thử Hoạt động 4.4: UDP Echo
        testSection4_4();

        // 4. Kiểm thử Bài tập 1: Host và URI Inspector
        testBai1();

        // 5. Kiểm thử Bài tập 2: TCP đổi chữ số thành chữ
        testBai2();

        // 6. Kiểm thử Bài tập 3: Dịch vụ ngày giờ TCP và UDP
        testBai3();

        // 7. Kiểm thử Bài tập 4: Máy tính từ xa
        testBai4();

        // 8. Kiểm thử Bài tập 5: Chat nhiều client
        testBai5();

        log("\n================================================================================");
        log("                         TẤT CẢ KIỂM THỬ ĐÃ HOÀN TẤT!                           ");
        log("================================================================================");

        // Ghi file kết quả ra evidence/test-results.txt
        File evidenceDir = new File("evidence");
        if (!evidenceDir.exists()) evidenceDir.mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter("evidence/test-results.txt", StandardCharsets.UTF_8))) {
            writer.print(logBuilder.toString());
        }
        System.out.println("\n[XONG] Kết quả kiểm thử chi tiết đã được lưu tại: evidence/test-results.txt");
        System.exit(0);
    }

    // --- TEST 4.1: HostInspector ---
    private static void testSection4_1() {
        log("[MỤC 4.1] KIỂM THỬ KHẢO SÁT ĐỊA CHỈ MẠNG (HostInspector)");
        String[] hosts = {"localhost", "example.com", "host-khong-ton-tai.invalid"};
        for (String h : hosts) {
            log("-> Test host: " + h);
            try {
                InetAddress[] addrs = InetAddress.getAllByName(h);
                for (InetAddress a : addrs) {
                    log("   IP: " + a.getHostAddress() + " | Canonical: " + a.getCanonicalHostName()
                            + " | Loopback: " + a.isLoopbackAddress() + " | SiteLocal: " + a.isSiteLocalAddress()
                            + " | Type: " + (a instanceof java.net.Inet4Address ? "IPv4" : "IPv6"));
                }
            } catch (Exception e) {
                log("   [Bắt ngoại lệ dự kiến]: " + e.getMessage());
            }
        }
        log("[OK] Hoàn tất Mục 4.1\n");
    }

    // --- TEST 4.2 & 4.3: TCP Command Server & MultiClient ---
    private static void testSection4_2_and_4_3() throws Exception {
        log("[MỤC 4.2 & 4.3] KIỂM THỬ TCP COMMAND SERVER & MULTI-CLIENT (Port 5000)");

        // Khởi động server trong 1 luồng nền
        Thread serverThread = new Thread(() -> {
            MultiClientTcpServer.main(new String[0]);
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500); // Đợi server sẵn sàng

        // Kiểm tra kịch bản tuần tự các lệnh giao thức
        log("-> Thử nghiệm kịch bản lệnh chuẩn:");
        try (Socket s = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String[] testCommands = {"PING", "UPPER xin chào", "TIME", "ABC", "QUIT"};
            for (String cmd : testCommands) {
                out.println(cmd);
                String resp = in.readLine();
                log("   Gửi: \"" + cmd + "\" -> Server: \"" + resp + "\"");
            }
        }

        // Kiểm tra đa luồng (3 client kết nối đồng thời)
        log("-> Thử nghiệm phục vụ đồng thời 3 client:");
        ExecutorService clientPool = Executors.newFixedThreadPool(3);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            futures.add(clientPool.submit(() -> {
                try (Socket s = new Socket("localhost", 5000);
                     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {
                    out.println("UPPER Client " + id + " message");
                    String resp = in.readLine();
                    log("   [Client " + id + "] Server: " + resp);
                    out.println("QUIT");
                    in.readLine();
                } catch (Exception e) {
                    log("   [Client " + id + "] Lỗi: " + e.getMessage());
                }
            }));
        }
        for (Future<?> f : futures) f.get();
        clientPool.shutdown();
        log("[OK] Hoàn tất Mục 4.2 & 4.3\n");
    }

    // --- TEST 4.4: UDP Echo Server ---
    private static void testSection4_4() throws Exception {
        log("[MỤC 4.4] KIỂM THỬ UDP ECHO SERVER & TIMEOUT (Port 5001)");

        Thread udpServerThread = new Thread(() -> {
            UdpEchoServer.main(new String[0]);
        });
        udpServerThread.setDaemon(true);
        udpServerThread.start();
        Thread.sleep(400);

        // 1. Ca gửi tiếng Việt có dấu
        try (DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(3000);
            byte[] msg = "xin chào UDP".getBytes(StandardCharsets.UTF_8);
            DatagramPacket req = new DatagramPacket(msg, msg.length, InetAddress.getByName("localhost"), 5001);
            client.send(req);

            byte[] buf = new byte[4096];
            DatagramPacket resp = new DatagramPacket(buf, buf.length);
            client.receive(resp);
            String text = new String(resp.getData(), resp.getOffset(), resp.getLength(), StandardCharsets.UTF_8);
            log("   Gửi: \"xin chào UDP\" -> Server UDP phản hồi: \"" + text + "\"");
        }

        // 2. Ca timeout (gửi tới port không có server lắng nghe)
        log("-> Thử nghiệm timeout khi gửi sai port (5999):");
        try (DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(1000); // 1 giây timeout để test nhanh
            byte[] msg = "hello timeout".getBytes(StandardCharsets.UTF_8);
            DatagramPacket req = new DatagramPacket(msg, msg.length, InetAddress.getByName("localhost"), 5999);
            client.send(req);

            byte[] buf = new byte[4096];
            DatagramPacket resp = new DatagramPacket(buf, buf.length);
            client.receive(resp);
        } catch (SocketTimeoutException e) {
            log("   [Bắt ngoại lệ Timeout thành công]: Quá thời gian chờ phản hồi từ cổng 5999!");
        }

        log("[OK] Hoàn tất Mục 4.4\n");
    }

    // --- TEST BÀI 1: HostUriInspector ---
    private static void testBai1() {
        log("[BÀI TẬP 1] KIỂM THỬ HOST VÀ URI INSPECTOR");
        log("-> 3 Ca hợp lệ:");
        String[][] validCases = {
            {"localhost", "https://localhost:8080/api/users?id=123#profile"},
            {"example.com", "http://example.com/test"},
            {"google.com", "ftp://files.google.com:21/files/document.pdf"}
        };
        for (String[] tc : validCases) {
            log("   [Hợp lệ] Host: " + tc[0] + " | URI: " + tc[1]);
            baitap1.HostUriInspector.main(tc);
        }

        log("-> 3 Ca lỗi kiểm thử:");
        log("   [Ca lỗi 1: Thiếu args]");
        baitap1.HostUriInspector.main(new String[0]);

        log("   [Ca lỗi 2: Host không tồn tại]");
        baitap1.HostUriInspector.main(new String[]{"host-khong-ton-tai-abc.invalid", "http://example.com"});

        log("   [Ca lỗi 3: URI sai cú pháp]");
        baitap1.HostUriInspector.main(new String[]{"localhost", "http:// bad uri : invalid"});

        log("[OK] Hoàn tất Bài 1\n");
    }

    // --- TEST BÀI 2: NumberToWord ---
    private static void testBai2() throws Exception {
        log("[BÀI TẬP 2] KIỂM THỬ TCP ĐỔI CHỮ SỐ THÀNH CHỮ (Port 5002)");

        Thread sThread = new Thread(() -> {
            NumberToWordServer.main(new String[0]);
        });
        sThread.setDaemon(true);
        sThread.start();
        Thread.sleep(300);

        try (Socket s = new Socket("localhost", NumberToWordServer.PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Các ca biên bắt buộc theo đề bài: 0, 9, chuỗi rỗng, 10, a, khoảng trắng
            String[] testInputs = {"0", "9", "5", "", "10", "a", " 5 ", "QUIT"};
            for (String input : testInputs) {
                out.println(input);
                String resp = in.readLine();
                log("   Nhập: \"" + input + "\" -> Phản hồi: \"" + resp + "\"");
            }
        }
        log("[OK] Hoàn tất Bài 2\n");
    }

    // --- TEST BÀI 3: DateTime ---
    private static void testBai3() throws Exception {
        log("[BÀI TẬP 3] KIỂM THỬ DỊCH VỤ NGÀY GIỜ TCP (Port 5003) & UDP (Port 5004)");

        Thread tcpThread = new Thread(() -> DateTimeTcpServer.main(new String[0]));
        tcpThread.setDaemon(true);
        tcpThread.start();

        Thread udpThread = new Thread(() -> DateTimeUdpServer.main(new String[0]));
        udpThread.setDaemon(true);
        udpThread.start();
        Thread.sleep(300);

        // Test TCP
        try (Socket s = new Socket("localhost", DateTimeTcpServer.PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String[] cmds = {"DATE", "TIME", "DATETIME", "UNKNOWN", "QUIT"};
            for (String c : cmds) {
                out.println(c);
                String resp = in.readLine();
                log("   [TCP] Gửi: " + c + " -> Server: " + resp);
            }
        }

        // Test UDP
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(2000);
            InetAddress srv = InetAddress.getByName("localhost");
            String[] cmds = {"DATE", "TIME", "DATETIME"};
            byte[] buf = new byte[4096];
            for (String c : cmds) {
                byte[] b = c.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(b, b.length, srv, DateTimeUdpServer.PORT));
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.receive(resp);
                String txt = new String(resp.getData(), resp.getOffset(), resp.getLength(), StandardCharsets.UTF_8);
                log("   [UDP] Gửi: " + c + " -> Server: " + txt);
            }
        }
        log("[OK] Hoàn tất Bài 3\n");
    }

    // --- TEST BÀI 4: CalcServer ---
    private static void testBai4() throws Exception {
        log("[BÀI TẬP 4] KIỂM THỬ MÁY TÍNH TỪ XA (Port 5005)");

        Thread sThread = new Thread(() -> CalcServer.main(new String[0]));
        sThread.setDaemon(true);
        sThread.start();
        Thread.sleep(300);

        try (Socket s = new Socket("localhost", CalcServer.PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Các tình huống quy định trong bảng đề bài:
            String[] testExprs = {
                "CALC + 100 200",
                "CALC / 10 0",
                "CALC % 10 3",
                "CALC + a 2",
                "CALC + 50", // Thiếu thành phần
                "CALC * 12.5 4",
                "QUIT"
            };

            for (String expr : testExprs) {
                out.println(expr);
                String resp = in.readLine();
                log("   Yêu cầu: \"" + expr + "\" -> Server: \"" + resp + "\"");
            }
        }
        log("[OK] Hoàn tất Bài 4\n");
    }

    // --- TEST BÀI 5: ChatServer ---
    private static void testBai5() throws Exception {
        log("[BÀI TẬP 5] KIỂM THỬ PHÒNG CHAT ĐA CLIENT (Port 5006)");

        Thread chatThread = new Thread(() -> ChatServer.main(new String[0]));
        chatThread.setDaemon(true);
        chatThread.start();
        Thread.sleep(300);

        // Kết nối Client 1: Alice
        Socket socketAlice = new Socket("localhost", ChatServer.PORT);
        BufferedReader inAlice = new BufferedReader(new InputStreamReader(socketAlice.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter outAlice = new PrintWriter(new OutputStreamWriter(socketAlice.getOutputStream(), StandardCharsets.UTF_8), true);
        inAlice.readLine(); // Chào mừng
        outAlice.println("Alice"); // Gửi nickname
        String welcomeAlice = inAlice.readLine();
        log("   [Alice] Đăng ký: " + welcomeAlice);

        // Kết nối Client 2: Thử Nickname trùng "Alice"
        Socket socketBob = new Socket("localhost", ChatServer.PORT);
        BufferedReader inBob = new BufferedReader(new InputStreamReader(socketBob.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter outBob = new PrintWriter(new OutputStreamWriter(socketBob.getOutputStream(), StandardCharsets.UTF_8), true);
        inBob.readLine(); // Chào mừng
        outBob.println("Alice"); // Thử trùng tên
        String takenMsg = inBob.readLine();
        log("   [Bob thử tên Alice]: Server phản hồi -> " + takenMsg);

        // Bob chọn lại nickname đúng
        outBob.println("Bob");
        String welcomeBob = inBob.readLine();
        log("   [Bob] Đăng ký lại: " + welcomeBob);

        // Đọc thông báo join của Bob trên Alice
        String aliceNotify = inAlice.readLine();
        log("   [Alice nhận thông báo Broadcast]: " + aliceNotify);

        // Alice gõ USERS
        outAlice.println("USERS");
        String usersResp = inAlice.readLine();
        log("   [Alice lệnh USERS]: " + usersResp);

        // Alice gửi tin nhắn: MSG Chào Bob nhé!
        outAlice.println("MSG Chào Bob nhé!");
        inAlice.readLine(); // OK SENT
        String bobReceived = inBob.readLine();
        log("   [Bob nhận tin từ Alice]: " + bobReceived);

        // Alice QUIT
        outAlice.println("QUIT");
        inAlice.readLine(); // OK BYE
        socketAlice.close();

        // Bob nhận thông báo Alice rời phòng
        String bobLeaveNotify = inBob.readLine();
        log("   [Bob nhận thông báo]: " + bobLeaveNotify);

        // Bob QUIT
        outBob.println("QUIT");
        socketBob.close();

        log("[OK] Hoàn tất Bài 5\n");
    }
}
