package muc4_1;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Mục 4.1: Khảo sát địa chỉ mạng bằng InetAddress
 * 
 * Chương trình nhận vào một tên miền (hostname) hoặc địa chỉ IP từ tham số dòng lệnh,
 * sau đó phân giải để lấy danh sách tất cả các địa chỉ IP tương ứng, kiểm tra xem
 * đó là IPv4 hay IPv6, có phải Loopback hay Site Local hay không.
 */
public class HostInspector {

    public static void main(String[] args) {
        // Kiểm tra xem người dùng có truyền đúng 1 tham số không
        if (args.length != 1) {
            System.out.println("Cách dùng: java muc4_1.HostInspector <hostname>");
            System.out.println("Ví dụ: java muc4_1.HostInspector localhost");
            System.out.println("       java muc4_1.HostInspector example.com");
            return;
        }

        String hostName = args[0];

        try {
            // Lấy tất cả các địa chỉ IP được gắn với hostname này
            InetAddress[] addresses = InetAddress.getAllByName(hostName);
            System.out.println("Host: " + hostName);

            for (InetAddress address : addresses) {
                System.out.println("----------------------------------------");
                // In ra địa chỉ IP dạng chuỗi (ví dụ 127.0.0.1 hoặc 93.184.215.14)
                System.out.println("- IP: " + address.getHostAddress());

                // Xác định loại địa chỉ là IPv4 hay IPv6 theo yêu cầu đề bài
                if (address instanceof Inet4Address) {
                    System.out.println("  Phân loại: IPv4");
                } else if (address instanceof Inet6Address) {
                    System.out.println("  Phân loại: IPv6");
                } else {
                    System.out.println("  Phân loại: Không xác định");
                }

                // In tên chính quy (FQDN) lấy từ DNS đảo
                System.out.println("  Canonical : " + address.getCanonicalHostName());

                // Loopback: Địa chỉ trỏ về chính máy tính hiện tại (ví dụ 127.0.0.1 hoặc ::1)
                System.out.println("  Loopback  : " + address.isLoopbackAddress());

                // Site local: Địa chỉ mạng nội bộ / Private IP (như dải 192.168.x.x, 10.x.x.x)
                System.out.println("  Site local: " + address.isSiteLocalAddress());
            }
            System.out.println("----------------------------------------");

        } catch (UnknownHostException e) {
            // Khi không tìm thấy hoặc DNS không phân giải được host
            System.err.println("Không phân giải được host: " + hostName);
        }
    }
}
