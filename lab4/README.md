# BÁO CÁO VÀ HƯỚNG DẪN THỰC HÀNH LAB 04
## LẬP TRÌNH SOCKET TCP VÀ UDP TRONG JAVA (IT.FIT.IUH_v2026)

---

## 1. TỔNG QUAN VÀ NGUYÊN TẮC THỰC HÀNH

Tài liệu thực hành giúp sinh viên nắm vững mô hình mạng **Client - Server**, hiểu rõ sự khác biệt giữa hai giao thức tầng giao vận **TCP** và **UDP**, cũng như áp dụng các chuẩn lập trình mạng hiện đại trong Java:

* **Cổng dịch vụ (Port $\ge 5000$)**: Tránh dùng cổng hệ thống đặc quyền (dưới 1024), sử dụng cổng từ 5000 trở lên.
* **Mã hóa chuỗi (Charset UTF-8)**: Bắt buộc chỉ định `StandardCharsets.UTF_8` để truyền nhận dữ liệu tiếng Việt chính xác và đồng nhất trên mọi hệ điều hành.
* **Tự động đóng tài nguyên (Try-with-resources)**: Tránh rò rỉ socket và file descriptor.
* **Quản lý đa luồng (ExecutorService)**: Sử dụng Fixed Thread Pool để kiểm soát số lượng luồng, tránh cạn kiệt bộ nhớ máy chủ khi có nhiều client kết nối.
* **Xử lý Timeout trong UDP**: Cấu hình `socket.setSoTimeout(3000)` để client không bị treo vô hạn khi mạng mất gói tin hoặc server chưa bật.

---

## 2. CẤU TRÚC THƯ MỤC DỰ ÁN (CHUẨN ECLIPSE IDE)

```
lab4/
├── .project                            <-- Cấu hình Eclipse Java Project
├── .classpath                          <-- Cấu hình Java Build Path cho Eclipse
├── README.md                           <-- File tài liệu & hướng dẫn
├── src/                                <-- Thư mục Source Folder chính
│   ├── muc4_1/
│   │   └── HostInspector.java          <-- [Mục 4.1] Khảo sát IP, Loopback, Site-local, IPv4/IPv6
│   ├── muc4_2/
│   │   ├── TcpCommandServer.java       <-- [Mục 4.2] TCP Server đơn luồng theo giao thức dòng
│   │   └── TcpCommandClient.java       <-- [Mục 4.2] TCP Client gửi lệnh
│   ├── muc4_3/
│   │   └── MultiClientTcpServer.java   <-- [Mục 4.3] TCP Server đa luồng dùng Thread Pool
│   ├── muc4_4/
│   │   ├── UdpEchoServer.java          <-- [Mục 4.4] UDP Echo Server (Port 5001)
│   │   └── UdpEchoClient.java          <-- [Mục 4.4] UDP Echo Client có timeout 3000ms
│   ├── baitap1/
│   │   └── HostUriInspector.java       <-- [Bài 1] Phân giải Host & phân tích URI bằng java.net.URI
│   ├── baitap2/
│   │   ├── NumberToWordServer.java     <-- [Bài 2] Đổi số 0-9 thành chữ tiếng Việt (Port 5002)
│   │   └── NumberToWordClient.java
│   ├── baitap3/
│   │   ├── DateTimeTcpServer.java      <-- [Bài 3] Tra cứu DATE, TIME qua TCP (Port 5003)
│   │   ├── DateTimeTcpClient.java
│   │   ├── DateTimeUdpServer.java      <-- [Bài 3] Tra cứu DATE, TIME qua UDP (Port 5004)
│   │   └── DateTimeUdpClient.java
│   ├── baitap4/
│   │   ├── CalcServer.java             <-- [Bài 4] Máy tính từ xa CALC <op> <n1> <n2> (Port 5005)
│   │   └── CalcClient.java
│   ├── baitap5/
│   │   ├── ChatServer.java             <-- [Bài 5] Chat Room đa client, Nickname duy nhất (Port 5006)
│   │   └── ChatClient.java
│   └── test/
│       └── Lab4TestRunner.java         <-- Bộ Test tự động chạy và kiểm tra toàn bộ bài lab
├── bin/ (hoặc out/)                    <-- Thư mục chứa bytecode .class sau khi biên dịch
└── evidence/
    └── test-results.txt                <-- File log kết quả kiểm thử thực tế
```

---

## 3. HƯỚNG DẪN BIÊN DỊCH VÀ CHẠY NHANH

### Cách 1: Biên dịch và chạy bộ test tự động (Khuyên dùng)

Mở **PowerShell** hoặc **Terminal** tại thư mục `lab4`:

```powershell
# 1. Biên dịch toàn bộ mã nguồn vào thư mục bin/
javac -d bin -encoding UTF-8 (Get-ChildItem -Recurse -Filter *.java src).FullName

# 2. Chạy bộ kiểm thử tự động toàn bộ 100% các phần và bài tập
java "-Dfile.encoding=UTF-8" -cp bin test.Lab4TestRunner
```

Kết quả kiểm thử chi tiết sẽ được tự động xuất ra màn hình và lưu vào file `evidence/test-results.txt`.

---

### Cách 2: Chạy tương tác từng chương trình độc lập

#### 1. Khảo sát địa chỉ mạng (Mục 4.1)
```powershell
# Kiểm tra localhost
java -cp bin muc4_1.HostInspector localhost

# Kiểm tra tên miền ngoài
java -cp bin muc4_1.HostInspector example.com

# Kiểm tra tên miền không tồn tại (xử lý ngoại lệ)
java -cp bin muc4_1.HostInspector host-khong-ton-tai.invalid
```

#### 2. TCP Client - Server theo giao thức dòng (Mục 4.2 & 4.3)
Mở **2 cửa sổ Terminal**:
* **Terminal 1 (Server đa luồng)**:
  ```powershell
  java -cp bin muc4_3.MultiClientTcpServer
  ```
* **Terminal 2 (Client)**:
  ```powershell
  java -cp bin muc4_2.TcpCommandClient localhost 5000
  ```
* **Các lệnh hỗ trợ gõ trên Client**:
  - `PING` $\to$ Server trả về: `OK PONG`
  - `UPPER xin chào` $\to$ Server trả về: `OK XIN CHÀO`
  - `TIME` $\to$ Server trả về: `OK <ngày giờ>`
  - `QUIT` $\to$ Server trả về: `OK BYE` và đóng kết nối

#### 3. UDP Echo Server & Client có Timeout (Mục 4.4)
Mở **2 cửa sổ Terminal**:
* **Terminal 1 (Server UDP)**:
  ```powershell
  java -cp bin muc4_4.UdpEchoServer
  ```
* **Terminal 2 (Client UDP)**:
  ```powershell
  java -cp bin muc4_4.UdpEchoClient localhost 5001 "xin chào UDP"
  ```
* *Kiểm tra Timeout*: Khi tắt server hoặc gửi tới cổng không tồn tại, Client tự động báo lỗi sau 3 giây: `Hết 3 giây nhưng chưa nhận được phản hồi từ Server`.

---

#### 4. Bài tập 1: Phân tích Host & URI Inspector
```powershell
# Cú pháp: java -cp bin baitap1.HostUriInspector <hostname> <uri>
java -cp bin baitap1.HostUriInspector localhost "https://localhost:8080/api/users?id=123#profile"
```

#### 5. Bài tập 2: TCP đổi chữ số thành chữ tiếng Việt (Port 5002)
* **Terminal 1**: `java -cp bin baitap2.NumberToWordServer`
* **Terminal 2**: `java -cp bin baitap2.NumberToWordClient localhost 5002`
* **Các ca kiểm thử**:
  - Gõ `0` $\to$ `OK không`
  - Gõ `9` $\to$ `OK chín`
  - Gõ `10`, `a`, hoặc chuỗi có khoảng trắng $\to$ `ERR INVALID_DIGIT`
  - Gõ `QUIT` $\to$ `OK BYE`

#### 6. Bài tập 3: Dịch vụ ngày giờ qua TCP & UDP
* **TCP Server (Port 5003)**: `java -cp bin baitap3.DateTimeTcpServer`
* **TCP Client**: `java -cp bin baitap3.DateTimeTcpClient localhost 5003`
  - Hỗ trợ: `DATE`, `TIME`, `DATETIME`, `QUIT`.
* **UDP Server (Port 5004)**: `java -cp bin baitap3.DateTimeUdpServer`
* **UDP Client**: `java -cp bin baitap3.DateTimeUdpClient localhost 5004`
  - So sánh: Khi tắt Server đột ngột, TCP Client nhận được `null` hoặc báo ngắt kết nối ngay lập tức; UDP Client sẽ chờ đúng 3 giây rồi báo timeout.

#### 7. Bài tập 4: Máy tính từ xa (Port 5005)
* **Terminal 1**: `java -cp bin baitap4.CalcServer`
* **Terminal 2**: `java -cp bin baitap4.CalcClient localhost 5005`
* **Bảng lệnh mẫu**:
  - `CALC + 100 200` $\to$ `OK 300`
  - `CALC / 10 0` $\to$ `ERR DIVIDE_BY_ZERO`
  - `CALC % 10 3` $\to$ `ERR UNSUPPORTED_OPERATOR`
  - `CALC + a 2` $\to$ `ERR INVALID_NUMBER`
  - `CALC + 50` $\to$ `ERR INVALID_FORMAT`

#### 8. Bài tập 5: Phòng Chat TCP đa Client (Port 5006)
* **Terminal 1**: `java -cp bin baitap5.ChatServer`
* **Terminal 2 (User 1)**: `java -cp bin baitap5.ChatClient localhost 5006` $\to$ Đặt nickname `Alice`
* **Terminal 3 (User 2)**: `java -cp bin baitap5.ChatClient localhost 5006` $\to$ Đặt nickname `Bob`
* **Các tính năng**:
  - Nhập trùng nickname $\to$ Báo lỗi `ERR NICK_TAKEN`.
  - Gõ `USERS` $\to$ Xem ai đang online: `OK USERS Alice, Bob`.
  - Gõ `MSG Chào các bạn` $\to$ Gửi tin nhắn broadcast đến toàn bộ người trong phòng chat.
  - Gõ `QUIT` $\to$ Thoát phòng chat; server tự động thông báo cho mọi người và dọn dẹp kết nối an toàn.

---

## 4. HƯỚNG DẪN MỞ DỰ ÁN TRONG ECLIPSE IDE

1. Khởi động **Eclipse IDE**.
2. Chọn Workspace: `C:\Users\David Dang\eclipse-workspace`.
3. Trên thanh menu, chọn: **File** $\to$ **Open Projects from File System...**
4. Tại mục **Import Source**, bấm **Directory...** và chọn thư mục: `C:\Users\David Dang\eclipse-workspace\lab4`.
5. Bấm **Finish**.
6. Để chạy bất kỳ bài nào:
   - Mở file `.java` trong Package Explorer.
   - Nhấp chuột phải vào file $\to$ chọn **Run As** $\to$ **Java Application**.
   - Nếu chương trình cần tham số dòng lệnh (như `HostInspector`), nhấp chuột phải $\to$ **Run As** $\to$ **Run Configurations...** $\to$ Tab **Arguments** $\to$ điền tham số vào ô **Program arguments**.

---

## 5. BẢNG TỔNG KẾT SO SÁNH TCP VÀ UDP

| Tiêu chí | TCP (Transmission Control Protocol) | UDP (User Datagram Protocol) |
| :--- | :--- | :--- |
| **Kiểu truyền** | Hướng kết nối (Connection-oriented), luồng byte liên tục | Không kết nối (Connectionless), từng gói Datagram độc lập |
| **Độ tin cậy** | Rất cao (bảo đảm thứ tự, kiểm tra lỗi, tự truyền lại nếu mất gói) | Thấp hơn (không bảo đảm thứ tự, không tự truyền lại) |
| **Ranh giới thông điệp** | Không tự bảo toàn (cần quy ước dấu xuống dòng `\n` hoặc độ dài) | Mỗi `DatagramPacket` là một thông điệp độc lập |
| **Lớp Java cốt lõi** | `ServerSocket` và `Socket` | `DatagramSocket` và `DatagramPacket` |
| **Tình huống sử dụng** | Chat, truyền file, web, hệ thống yêu cầu độ chính xác cao | Truyền streaming, video call, game thời gian thực, DNS query |
