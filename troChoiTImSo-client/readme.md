# troChoiTimSo
Lập trình ứng dụng mạng - Project trò chơi tìm số 

<!-- mvn javafx:run -->

# troChoiTimSo
# how to run
mvn clean install : chỉ chạy khi file pom.xml được update
mvn javafx:run : để chạy code java
mvn dependency:tree : test nhanh những dependencies đã cài thành công chưa (không thấy chữ error là được)

Phải đứng ở thư mục gốc của Project (nơi chứa file pom.xml) để chạy lệnh này.
Lệnh mvn (Maven) luôn đi tìm file pom.xml để đọc "hướng dẫn sử dụng".
Giả sử dự án của bạn nằm ở ổ E:\laptrinhungdungmang\troChoiTImSo.
ĐÚNG: Mở Terminal tại E:\laptrinhungdungmang\troChoiTImSo> và gõ mvn javafx:run.
Làm sao Maven biết file App.java ở đâu để chạy?
Vì đã khai báo đường dẫn trong file pom.xml

# configuration
<!-- configuration 
<mainClass>com.timso.client.MainApp</mainClass>
</configuration> -->

Vì sao không phải là src.main.java.com.timso.client.MainApp? Vì src/main/java là gốc tọa độ, khi bạn khai báo <mainClass> trong file pom.xml, Maven sẽ mặc định bắt đầu tìm kiếm từ bên trong thư mục src/main/java.

# Start?
MainApp > start UI Client

Game Multiplayer => Run server before && run client after.

# DB > MySQL > Xampp > Shell
+ mysql -u root -p 
+ <enter>
+ SHOW DATABASES;
+ USE <ten_DB>
+ SHOW TABLES;
+ DESC <tên_table>;
+ SELECT * FROM <ten_bang>
+ ALTER TABLE <TEN_BANG> ADD COLUMN <TEN_COT> <KIEU_DU_LIEU>(GIA_TRI);
+ 
