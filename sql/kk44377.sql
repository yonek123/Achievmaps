-- phpMyAdmin SQL Dump
-- version 3.3.10
-- http://www.phpmyadmin.net
--
-- Host: db.zut.edu.pl
-- Czas wygenerowania: 29 Paź 2021, 18:47
-- Wersja serwera: 1.0.328
-- Wersja PHP: 5.4.16

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Baza danych: `kk44377`
--

-- --------------------------------------------------------

--
-- Struktura tabeli dla  `AchievementList`
--

CREATE TABLE IF NOT EXISTS `AchievementList` (
  `Achievementid` int(11) NOT NULL UNIQUE AUTO_INCREMENT,
  `AchievementName` varchar(255) NOT NULL UNIQUE,
  `Description` text NOT NULL,
  `Latitude` text,
  `Longitude` text,
  `Points` int(11) NOT NULL,
  `OpenTime` varchar(255),
  `CloseTime` varchar(255),
  `ViewTime` varchar(255),
  `Requirements` int(11) NOT NULL,
  `Tags` text NOT NULL,
  PRIMARY KEY (`Achievementid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Zrzut danych tabeli `AchievementList`
--

ALTER TABLE `AchievementList` CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE `AchievementList` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla  `News`
--

CREATE TABLE IF NOT EXISTS `News` (
  `Newsid` int(11) NOT NULL UNIQUE AUTO_INCREMENT,
  `Title` varchar(255) NOT NULL,
  `Article` text NOT NULL,
  `ArticleImage` mediumblob NOT NULL,
  `Tags` text NOT NULL,
  `DateAdded` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`Newsid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Zrzut danych tabeli `News`
--

ALTER TABLE `News` CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE `News` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla  `Users`
--

CREATE TABLE IF NOT EXISTS `Users` (
  `Personid` int(11) NOT NULL UNIQUE AUTO_INCREMENT,
  `Email` varchar(255) NOT NULL UNIQUE,
  `Password` varchar(255) NOT NULL,
  `Nickname` varchar(255) NOT NULL UNIQUE,
  `PointsAll` int(11) NOT NULL,
  `PointsNature` int(11) NOT NULL,
  `PointsArchitecture` int(11) NOT NULL,
  PRIMARY KEY (`Personid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Zrzut danych tabeli `Users`
--

ALTER TABLE `Users` CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE `Users` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla  `Friends`
--

CREATE TABLE IF NOT EXISTS `Friends` (
  `Personid` int(11) NOT NULL,
  `Friendid` int(11) NOT NULL,
  FOREIGN KEY (`Personid`) REFERENCES `Users`(`Personid`),
  FOREIGN KEY (`Friendid`) REFERENCES `Users`(`Personid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Zrzut danych tabeli `Friends`
--


INSERT INTO `AchievementList` (`AchievementName`, `Description`, `Latitude`, `Longitude`, `Points`, `OpenTime`, `CloseTime`, `ViewTime`, `Requirements`, `Tags`) VALUES
('Baszta Panieńska', 'Odwiedź Basztę Panieńską, zwaną też Basztą Siedmiu Płaszczy, symbol miasta Szczecin.', '53.42631352116791', '14.562885502712085', '10', '10:00', '17:00', '00:30', '1', 'Europa;Polska;Architektura'),
('Brama Królewska', 'Odwiedź barokową Bramę Królewską w Szczecinie i napij się przepysznej czekolady Wedel.', '53.42832774717961', '14.556718555956685', '50', '00:00', '24:00', '00:30', '1', 'Europa;Polska;Architektura'),
('Brama Portowa', 'Odwiedź barokową Bramę Portową w Szczecinie. Wewnątrz zobaczyć można przedstawienia Teatru Kameralnego. Odwiedź ich stronę internetową po szczegóły.', '53.424932256502316', '14.550359100137214', '50', '00:00', '24:00', '00:10', '1', 'Europa;Polska;Architektura'),
('Kamienica Loitzów', 'Zobacz przepiękną, gotycką Kamienicę Loitzów w Szczecinie.', '53.425233964007774', '14.559322938759125', '10', '00:00', '24:00', '00:10', '1', 'Europa;Polska;Architektura'),
('Kamienica Augusta Mintza', 'Zobacz elektyczną Kamienicę Augusta Mintza w Szczecinie i wejdź do umieszczonej w nim zabytkowej apteki.', '53.423827139607404', '14.559419472358165', '10', NULL, NULL, '00:30', '1', 'Europa;Polska;Architektura'),
('Kościół Świętego Antoniego z Padwy', 'Zobacz neogotycki Kościół Świętego Antoniego z Padwy w Szczecinie. Przyjdź w trakcie mszy aby zobaczyć wnętrze.', '53.47237445627327', '14.546551811777833', '10', '07:00', '13:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Sanktuarium Św. Andrzeja Boboli', 'Zobacz modernistyczne Sanktuarium Św. Andrzeja Boboli w Szczecinie. Odwiedź stronę kościoła aby poznać godziny mszy.', '53.42970561868568', '14.533076298284458', '20', '06:00', '19:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Świętych Apostołów Piotra i Pawła', 'Zobacz neogotycki Kościół Świętych Apostołów Piotra i Pawła w Szczecinie. Przyjdź w trakcie mszy aby zobaczyć wnętrze.', '53.36920870437769', '14.598861030824052', '20', '18:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Chrystusa Króla', 'Zobacz gotycki Kościół Chrystusa Króla w Szczecinie. Przyjdź w trakcie mszy aby zobaczyć wnętrze.', '53.52107850025285', '14.597017413506745', '10', '18:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura');

--
-- ('Kościół Chrystusa Króla', 'Zobacz gotycki Kościół Chrystusa Króla w Szczecinie. Przyjdź w trakcie mszy aby zobaczyć wnętrze.', '53.52107850025285', '14.597017413506745', '10', '18:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
--

-- ('Wały Chrobrego', 'Odwiedź Wały Chrobrego w Szczecin, Zachodniopomorskie, Polska', '53.429902750176595', '14.5650708160754', '10', '00:00', '24:00', '00:00', '00:30', 'Europa;Polska;Architektura'),
-- ('Brama Portowa', 'Odwiedź Brama Portowa w Szczecin, Zachodniopomorskie, Polska', '53.4250537242813', '14.55023035410361', '10', '00:00', '24:00', '00:00', '00:30', 'Europa;Polska;Architektura');

