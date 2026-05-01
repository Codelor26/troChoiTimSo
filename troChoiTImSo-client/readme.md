# DB > MySQL > Xampp > Shell
- mysql -u root -p
- <enter>
- SHOW DATABASES;
- USE <ten_DB>
- SHOW TABLES;
- DESC <tên_table>;
- SELECT \* FROM <ten_bang>
- ALTER TABLE <TEN_BANG> ADD COLUMN <TEN_COT> <KIEU_DU_LIEU>(GIA_TRI);
-

# Export sql//CMD
mysqldump -u root -p game_tim_so > game_tim_so.sql

# Reload
mvn clean install

# Run sever:
cd g:\timso\troChoiTimSo
mvn -pl troChoiTImSo-server exec:java

# Run client:
mvn -pl troChoiTImSo-client javafx:run
