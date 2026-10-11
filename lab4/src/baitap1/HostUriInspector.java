package baitap1;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * BÀI TẬP 1: Host và URI Inspector
 * 
 * Yêu cầu:
 * - Nhận hostname và URI qua tham số dòng lệnh args (args[0] là hostname, args[1] là URI).
 * - In toàn bộ IP, loại IPv4 hoặc IPv6, cờ loopback và site local.
 * - Sử dụng java.net.URI để bóc tách: scheme, host, port, path, query, fragment.
 * - Bắt và xử lý ngoại lệ: thiếu tham số, URI sai định dạng, hostname không phân giải được.
 */
public class HostUriInspector {

    public static void main(String[] args) {
        // 1. Kiểm tra đủ 2 tham số đầu vào
        if (args.length < 2) {
            System.out.println("Lỗi: Thiếu tham số dòng lệnh!");
            System.out.println("Cách dùng: java baitap1.HostUriInspector <hostname> <uri>");
            System.out.println("Ví dụ: java baitap1.HostUriInspector google.com https://iuh.edu.vn:443/tuyensinh?year=2026#lienhe");
            return;
        }

        String hostInput = args[0];
        String uriInput = args[1];

        System.out.println("==================================================");
        System.out.println("             KẾT QUẢ PHÂN TÍCH HOST VÀ URI        ");
        System.out.println("==================================================");

        // 2. Phân giải Hostname bằng InetAddress
        System.out.println("[1] PHÂN TÍCH HOST: " + hostInput);
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostInput);
            for (int i = 0; i < addresses.length; i++) {
                InetAddress addr = addresses[i];
                System.out.println("  -> Địa chỉ " + (i + 1) + ": " + addr.getHostAddress());
                if (addr instanceof Inet4Address) {
                    System.out.println("     + Kiểu       : IPv4");
                } else if (addr instanceof Inet6Address) {
                    System.out.println("     + Kiểu       : IPv6");
                }
                System.out.println("     + Loopback   : " + addr.isLoopbackAddress());
                System.out.println("     + Site Local : " + addr.isSiteLocalAddress());
            }
        } catch (UnknownHostException e) {
            System.err.println("  -> [LỖI] Không thể phân giải Hostname '" + hostInput + "': " + e.getMessage());
        }

        System.out.println("--------------------------------------------------");

        // 3. Phân tích URI bằng java.net.URI
        System.out.println("[2] PHÂN TÍCH URI: " + uriInput);
        try {
            URI uri = new URI(uriInput);

            // In ra các thành phần của URI
            System.out.println("  + Scheme   (Giao thức) : " + (uri.getScheme() != null ? uri.getScheme() : "[Không có]"));
            System.out.println("  + Host     (Tên máy)   : " + (uri.getHost() != null ? uri.getHost() : "[Không có]"));
            System.out.println("  + Port     (Cổng)      : " + (uri.getPort() != -1 ? uri.getPort() : "[Mặc định]"));
            System.out.println("  + Path     (Đường dẫn) : " + (uri.getPath() != null ? uri.getPath() : "[Không có]"));
            System.out.println("  + Query    (Truy vấn)  : " + (uri.getQuery() != null ? uri.getQuery() : "[Không có]"));
            System.out.println("  + Fragment (Thẻ neo)   : " + (uri.getFragment() != null ? uri.getFragment() : "[Không có]"));

        } catch (URISyntaxException e) {
            System.err.println("  -> [LỖI] URI sai cú pháp: " + e.getReason() + " (tại vị trí index: " + e.getIndex() + ")");
        }

        System.out.println("==================================================");
    }
}
