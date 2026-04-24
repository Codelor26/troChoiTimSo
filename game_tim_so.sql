-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th4 16, 2026 lúc 01:58 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `game_tim_so`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `gold` int(11) DEFAULT 0,
  `avatar` varchar(255) DEFAULT NULL,
  `player_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `gender`, `dob`, `gold`, `avatar`, `player_name`) VALUES
(1, 'ytran@gmail.com', '$2a$10$Dfo8jFEq59u7ZyM4uHBAWu55lzgtjmbaEmE.Syapj18G95tksGaXO', 'Tran Thi Nhu Y', 'Nu', '2004-06-02', 1000, NULL, NULL),
(2, 'admin123@gmail.com', '$2a$10$OJkaQNBMbf0NF5HZ6l10gucIiEWUfci912JCE4GY5umt15y9Crhve', 'Admin', 'Nu', '2002-04-05', 9999, '/icon/Martin-Berube-Character-Devil.256.png', NULL),
(3, 'nhuy40794@gmail.com', '$2a$10$jzvZs1ywWthwYf78uKQe1OuUXAG6K2QiRoEwq7b6FwQrTAAbHo8UO', 'NhuY', 'Female', '2026-04-09', 0, '/icon/Diversity-Avatars-Avatars-Trinity.512.png', NULL),
(4, 'codelor2604@gmail.com', '$2a$10$uxL4JjG4qzAOI2z9s4chW.s42N/h1wfLl6NmUL8ayn/7Wt0VusJ52', 'Codelor', 'Female', '2026-04-08', 0, '/icon/Hopstarter-Superhero-Avatar-Avengers-Thor.256.png', NULL),
(6, '123@gmail.com', '$2a$10$byXI1HIhAAwPrwWGJ2HD9ugMEslDx.4K9OS/6YXyEbi/S2MBLQI.S', 'tamnham', 'Female', '2026-04-01', 0, NULL, NULL),
(7, 'a1234@gmail.com', '$2a$10$ivMZLlDKhKTesX2JrPSBwOnYhHs8lhC2uDUJ0SY8y7Xsvki2LESG6', 'mori', 'Male', '2026-04-01', 0, '/icon/Diversity-Avatars-Avatars-Nikola-tesla.512.png', NULL),
(8, 'codelor260404@gmail.com', '$2a$10$4iTGUJ1NRpOR1Dammuir4OSBrVG.wO2W4Mx/34UtAGrb6ySWrsd/K', 'Codelordethuong', 'Female', '2004-06-02', 0, '/icon/Diversity-Avatars-Avatars-Native-woman.512.png', NULL);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
