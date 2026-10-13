import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================================");
        System.out.println("         CHUONG TRINH CHAT NHOM TCP         ");
        System.out.println("============================================");

        System.out.print("Nhap ten cua ban: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            System.out.print("Ten khong duoc de trong. Vui long nhap lai: ");
            name = scanner.nextLine().trim();
        }

        System.out.println("Dang ket noi toi Server " + SERVER_HOST + ":" + SERVER_PORT + "...");

        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println(">> Ket noi toi Server thanh cong!\n");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            out.println(name);

            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (isRunning && (serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        System.out.println("\n[THONG BAO] Mat ket noi toi Server.");
                    }
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            while (isRunning) {
                String userInput = scanner.nextLine();
                out.println(userInput);

                if (userInput.trim().equalsIgnoreCase("/exit")) {
                    isRunning = false;
                    System.out.println(">> Dang ngat ket noi va dong chuong trinh...");
                    break;
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }

            try {
                in.close();
            } catch (IOException ignored) {
            }

            if (out != null) {
                out.close();
            }

            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }

            System.out.println(">> Da ngat ket noi hoan toan. Hen gap lai!");

        } catch (IOException e) {
            System.err.println("[LOI KET NOI] Khong the ket noi toi Server: " + e.getMessage());
            System.err.println("Goi y: Hay khoi dong ChatServer truoc khi mo ChatClient.");
        } finally {
            scanner.close();
        }
    }
}
