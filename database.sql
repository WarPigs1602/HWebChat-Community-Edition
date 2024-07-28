-- phpMyAdmin SQL Dump
-- version 5.1.1deb5ubuntu1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Erstellungszeit: 28. Jul 2024 um 17:51
-- Server-Version: 10.6.18-MariaDB-0ubuntu0.22.04.1
-- PHP-Version: 8.1.29

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `database`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_banlist`
--

CREATE TABLE `hwc_banlist` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL DEFAULT '',
  `reason` varchar(255) NOT NULL,
  `banner` varchar(255) NOT NULL,
  `time` varchar(255) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_board`
--

CREATE TABLE `hwc_board` (
  `id` int(11) NOT NULL,
  `topic` varchar(255) NOT NULL,
  `content` longtext NOT NULL,
  `ref` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL,
  `board` bigint(20) NOT NULL,
  `posted` bigint(20) NOT NULL,
  `ip` varchar(255) NOT NULL,
  `cat` bigint(20) NOT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `closed` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `hwc_board`
--

INSERT INTO `hwc_board` (`id`, `topic`, `content`, `ref`, `user`, `board`, `posted`, `ip`, `cat`, `deleted`, `closed`) VALUES
(1, 'Neues!', 'Herzlich Willkommen in der Community :)', 1, 2, 0, 1711274899954, '127.0.0.1', 1, 0, 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_board_boards`
--

CREATE TABLE `hwc_board_boards` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `cat` bigint(20) NOT NULL,
  `topic` varchar(255) NOT NULL,
  `readonly` tinyint(1) NOT NULL DEFAULT 0,
  `description` longtext NOT NULL,
  `guests` tinyint(1) NOT NULL DEFAULT 0,
  `deleted` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `hwc_board_boards`
--

INSERT INTO `hwc_board_boards` (`id`, `cat`, `topic`, `readonly`, `description`, `guests`, `deleted`) VALUES
(1, 1, 'News!', 1, 'Aktuelle Nachrichten', 0, 0),
(2, 3, 'Supportforum', 0, 'Hier bekommst Du Support!', 0, 0),
(3, 1, 'Changelog', 1, 'Alle Änderungen in der Communitysoftware!', 0, 0),
(4, 3, 'Fehler melden!', 0, 'Hier kannst Du Fehler in der Software melden!', 0, 0),
(5, 4, 'Offtopic', 0, 'Allgemeines plaudern!', 0, 0),
(6, 4, 'Vorschläge für Funktionen!', 0, 'Hier kannst Du Funktionen für die Community vorschlagen!', 0, 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_board_cat`
--

CREATE TABLE `hwc_board_cat` (
  `id` bigint(20) NOT NULL,
  `topic` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Daten für Tabelle `hwc_board_cat`
--

INSERT INTO `hwc_board_cat` (`id`, `topic`, `description`, `deleted`) VALUES
(1, 'Allgemeines und Neues!', 'Hier finden Sie allgemeine aktuelle Informationen!', 0),
(3, 'Hilfen und Support', 'Hier findest Du Hilfe!', 0),
(4, 'Allgemeines', 'Hier werden allgemeine Beiträge geposted!', 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_friends`
--

CREATE TABLE `hwc_friends` (
  `id` bigint(20) NOT NULL,
  `nick` varchar(255) NOT NULL,
  `nick2` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_messages`
--

CREATE TABLE `hwc_messages` (
  `id` bigint(20) NOT NULL,
  `sender` varchar(255) DEFAULT NULL,
  `target` varchar(255) DEFAULT NULL,
  `text` longtext DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_napping`
--

CREATE TABLE `hwc_napping` (
  `id` int(11) NOT NULL,
  `nick` varchar(255) NOT NULL,
  `room` varchar(255) NOT NULL,
  `bg_color_1` varchar(6) NOT NULL,
  `bg_color_2` varchar(6) NOT NULL,
  `color_1` varchar(6) NOT NULL,
  `color_2` varchar(6) NOT NULL,
  `link_color_1` varchar(6) NOT NULL,
  `link_color_2` varchar(6) NOT NULL,
  `title` varchar(255) NOT NULL,
  `su` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`su`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_roomcfg`
--

CREATE TABLE `hwc_roomcfg` (
  `id` bigint(20) NOT NULL,
  `room` varchar(255) NOT NULL DEFAULT '',
  `topic` text DEFAULT NULL,
  `tar` int(11) NOT NULL DEFAULT 0,
  `locked` int(11) NOT NULL DEFAULT 0,
  `lock_reason` text DEFAULT NULL,
  `standard` int(11) NOT NULL DEFAULT 0,
  `allow_smilies` int(11) NOT NULL DEFAULT 1,
  `chat_napping` int(11) DEFAULT 0,
  `first_bgcolor` varchar(6) DEFAULT NULL,
  `second_bgcolor` varchar(6) DEFAULT NULL,
  `bordercolor` varchar(6) DEFAULT NULL,
  `textcolor` varchar(6) DEFAULT NULL,
  `linkcolor` varchar(6) DEFAULT NULL,
  `page_title` text NOT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `su` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Daten für Tabelle `hwc_roomcfg`
--

INSERT INTO `hwc_roomcfg` (`id`, `room`, `topic`, `tar`, `locked`, `lock_reason`, `standard`, `allow_smilies`, `chat_napping`, `first_bgcolor`, `second_bgcolor`, `bordercolor`, `textcolor`, `linkcolor`, `page_title`, `mail`, `owner`, `su`) VALUES
(1, 'Development', '', 0, 0, '', 1, 1, 0, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, ''),
(2, 'Exil', 'Hier landet der Müll des Chats ;)', 0, 0, '', 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, ''),
(3, 'Lounge', '❤️❤️❤️ Herzlich Willkommen im Chat ❤️❤️❤️', 0, 0, '', 1, 1, 0, '', '', '', '', '', '', NULL, NULL, ''),
(4, 'Staff-Lounge', 'Nur für Staff-Mitglieder!', 0, 1, 'Nur Staff-Mitglieder können diesen Raum betreten...', 1, 1, 0, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, '');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_session`
--

CREATE TABLE `hwc_session` (
  `id` bigint(20) NOT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `session` varchar(255) NOT NULL DEFAULT '',
  `room` varchar(255) NOT NULL DEFAULT '',
  `color` varchar(7) NOT NULL DEFAULT '',
  `status` int(11) NOT NULL DEFAULT 0,
  `away_status` int(11) NOT NULL DEFAULT 0,
  `away_reason` varchar(255) NOT NULL DEFAULT '',
  `gag` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_stats`
--

CREATE TABLE `hwc_stats` (
  `id` bigint(20) NOT NULL,
  `parameter` varchar(255) DEFAULT NULL,
  `value` bigint(20) DEFAULT NULL,
  `date` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Daten für Tabelle `hwc_stats`
--

INSERT INTO `hwc_stats` (`id`, `parameter`, `value`, `date`) VALUES
(1, 'peak', 0, 0),
(2, 'day_1', 0, 0),
(3, 'day_2', 0, 0),
(4, 'day_3', 0, 0),
(5, 'day_4', 0, 0),
(6, 'day_5', 0, 0),
(7, 'day_6', 0, 0),
(8, 'day_7', 0, 0),
(9, 'day_8', 0, 0),
(10, 'day_9', 0, 0),
(11, 'day_10', 0, 0),
(12, 'day_11', 0, 0),
(13, 'day_12', 0, 0),
(14, 'day_13', 0, 0),
(15, 'day_14', 0, 0),
(16, 'day_15', 0, 0),
(17, 'day_16', 0, 0),
(18, 'day_17', 0, 0),
(19, 'day_18', 0, 0),
(20, 'day_20', 0, 0),
(21, 'day_30', 0, 0),
(22, 'day_21', 0, 0),
(23, 'day_22', 0, 0),
(24, 'day_23', 0, 0),
(25, 'day_24', 0, 0),
(26, 'day_25', 0, 0),
(27, 'day_26', 0, 0),
(28, 'day_27', 0, 0),
(29, 'day_28', 0, 0),
(30, 'day_29', 0, 0),
(31, 'day_31', 0, 0),
(32, 'month_1', 0, 0),
(33, 'month_2', 0, 0),
(34, 'month_3', 0, 0),
(35, 'month_4', 0, 0),
(36, 'month_5', 0, 0),
(37, 'month_6', 0, 0),
(38, 'month_7', 0, 0),
(39, 'month_8', 0, 0),
(40, 'month_9', 0, 0),
(41, 'month_10', 0, 0),
(42, 'month_11', 0, 0),
(43, 'month_12', 0, 0),
(44, 'hour_0', 0, 0),
(45, 'hour_1', 0, 0),
(46, 'hour_2', 0, 0),
(47, 'hour_3', 0, 0),
(48, 'hour_4', 0, 0),
(49, 'hour_5', 0, 0),
(50, 'hour_6', 0, 0),
(51, 'hour_7', 0, 0),
(52, 'hour_8', 0, 0),
(53, 'hour_9', 0, 0),
(54, 'hour_10', 0, 0),
(55, 'hour_11', 0, 0),
(56, 'hour_12', 0, 0),
(57, 'hour_13', 0, 0),
(58, 'hour_14', 0, 0),
(59, 'hour_15', 0, 0),
(60, 'hour_16', 0, 0),
(61, 'hour_17', 0, 0),
(62, 'hour_18', 0, 0),
(63, 'hour_19', 0, 0),
(64, 'hour_20', 0, 0),
(65, 'hour_21', 0, 0),
(66, 'hour_22', 0, 0),
(67, 'hour_23', 0, 0),
(68, 'curr_date', 0, 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `hwc_users`
--

CREATE TABLE `hwc_users` (
  `id` bigint(20) NOT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `nick2` varchar(255) DEFAULT NULL,
  `pwd` varchar(255) DEFAULT NULL,
  `pwd2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `mail` varchar(255) DEFAULT NULL,
  `sex` char(1) NOT NULL DEFAULT '-',
  `reminder` varchar(255) DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `homepage` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `image_upload` longblob DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `hobby` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL DEFAULT 1,
  `points` int(11) NOT NULL DEFAULT 0,
  `timestamp_reg` bigint(20) NOT NULL DEFAULT 0,
  `timestamp_login` bigint(20) NOT NULL DEFAULT 0,
  `bday_day` tinyint(4) NOT NULL DEFAULT 1,
  `bday_month` tinyint(4) NOT NULL DEFAULT 1,
  `bday_year` int(11) NOT NULL DEFAULT 1970,
  `login_room` varchar(255) NOT NULL DEFAULT 'Lounge',
  `description` varchar(255) DEFAULT NULL,
  `slogan` varchar(255) DEFAULT NULL,
  `signature` text DEFAULT NULL,
  `ignore` text DEFAULT NULL,
  `sv` int(11) NOT NULL DEFAULT 0,
  `icq` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `live` varchar(255) DEFAULT NULL,
  `yahoo` varchar(255) DEFAULT NULL,
  `facebook` varchar(255) DEFAULT NULL,
  `twitter` varchar(255) DEFAULT NULL,
  `irc` varchar(255) DEFAULT NULL,
  `youtube` varchar(255) DEFAULT NULL,
  `visitors` text DEFAULT NULL,
  `color` varchar(7) DEFAULT '000000',
  `fam_status` varchar(255) DEFAULT NULL,
  `moderator` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `hwc_banlist`
--
ALTER TABLE `hwc_banlist`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_board`
--
ALTER TABLE `hwc_board`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_board_boards`
--
ALTER TABLE `hwc_board_boards`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id` (`id`);

--
-- Indizes für die Tabelle `hwc_board_cat`
--
ALTER TABLE `hwc_board_cat`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_friends`
--
ALTER TABLE `hwc_friends`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_messages`
--
ALTER TABLE `hwc_messages`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_napping`
--
ALTER TABLE `hwc_napping`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_roomcfg`
--
ALTER TABLE `hwc_roomcfg`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_session`
--
ALTER TABLE `hwc_session`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_stats`
--
ALTER TABLE `hwc_stats`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `hwc_users`
--
ALTER TABLE `hwc_users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `hwc_banlist`
--
ALTER TABLE `hwc_banlist`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT für Tabelle `hwc_board`
--
ALTER TABLE `hwc_board`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT für Tabelle `hwc_board_boards`
--
ALTER TABLE `hwc_board_boards`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT für Tabelle `hwc_board_cat`
--
ALTER TABLE `hwc_board_cat`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT für Tabelle `hwc_friends`
--
ALTER TABLE `hwc_friends`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT für Tabelle `hwc_messages`
--
ALTER TABLE `hwc_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT für Tabelle `hwc_napping`
--
ALTER TABLE `hwc_napping`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `hwc_roomcfg`
--
ALTER TABLE `hwc_roomcfg`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT für Tabelle `hwc_session`
--
ALTER TABLE `hwc_session`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2429;

--
-- AUTO_INCREMENT für Tabelle `hwc_stats`
--
ALTER TABLE `hwc_stats`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;

--
-- AUTO_INCREMENT für Tabelle `hwc_users`
--
ALTER TABLE `hwc_users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
