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
('Kościół Świętego Antoniego z Padwy', 'Zobacz neogotycki Kościół Świętego Antoniego z Padwy w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.47237445627327', '14.546551811777833', '10', '07:00', '13:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Sanktuarium Św. Andrzeja Boboli', 'Zobacz modernistyczne Sanktuarium Św. Andrzeja Boboli w Szczecinie. Odwiedź stronę kościoła aby poznać godziny mszy.', '53.42970561868568', '14.533076298284458', '20', '06:00', '19:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Świętych Apostołów Piotra i Pawła', 'Zobacz neogotycki Kościół Świętych Apostołów Piotra i Pawła w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.36920870437769', '14.598861030824052', '20', '06:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Chrystusa Króla', 'Zobacz gotycki Kościół Chrystusa Króla w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.52107850025285', '14.597017413506745', '10', '11:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Ducha', 'Zobacz neogotycki Kościół św. Ducha w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.38052381619585', '14.633950274874342', '10', '11:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Jana Ewangelisty', 'Zobacz gotycki Kościół św. Jana Ewangelisty w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.4225972008795', '14.55756372712034', '50', '06:00', '21:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Józefa Oblubieńca Najświętszej Maryi Panny', 'Zobacz neogotycki Kościół św. Józefa Oblubieńca Najświętszej Maryi Panny w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.40802610519803', '14.534883854102942', '30', '06:00', '20:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Kazimierza', 'Zobacz neogotycki Kościół św. Kazimierza w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.45359023250932', '14.531759155957724', '30', '11:00', '21:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Świętego Krzyża', 'Zobacz modernistyczny Kościół Świętego Krzyża w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.44065194940266', '14.509079237661517', '30', '06:00', '21:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Maksymiliana Marii Kolbego', 'Zobacz gotycki Kościół św. Maksymiliana Marii Kolbego w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.39663684073591', '14.52819482162269', '10', '06:30', '14:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Matki Bożej Bolesnej', 'Zobacz neogotycki Kościół Matki Bożej Bolesnej w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.3968388214182', '14.52821932144058', '10', '11:00', '14:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Matki Bożej Jasnogórskiej', 'Zobacz wierzę ciśnień przekształconą w wyjątkowy Kościół Matki Bożej Jasnogórskiej w Szczecinie. Otwarty całą dobę.', '53.40565101558825', '14.52346894702581', '50', NULL, NULL, '02:00', '1', 'Europa;Polska;Architektura'),
('Kościół Matki Bożej Nieustającej Pomocy', 'Zobacz zabytkowy Kościół Matki Bożej Nieustającej Pomocy w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.468462093376054', '14.590625969596703', '10', '09:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Matki Bożej Ostrobramskiej', 'Zobacz byłą luterańską kaplicę, a aktualnie Kościół Matki Bożej Ostrobramskiej w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.45862857311525', '14.567785767336149', '20', '06:00', '21:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Matki Bożej Różańcowej', 'Zobacz łączący wiele stylów Kościół Matki Bożej Różańcowej w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.410325856724384', '14.495322113628193', '20', '08:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Najświętszego Serca Pana Jezusa', 'Zobacz modernistyczny Kościół Najświętszego Serca Pana Jezusa w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.42629321569597', '14.547280484792745', '20', '08:00', '21:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Najświętszego Zbawiciela', 'Zobacz modernistyczny Kościół Najświętszego Zbawiciela w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.445295890040526', '14.543389442465791', '20', '06:00', '19:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Najświętszej Rodziny', 'Zobacz drewniano-gliniany Kościół Najświętszej Rodziny w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.3513953007855', '14.73264685294512', '10', '09:00', '16:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Niepokalanego Poczęcia Najświętszej Maryi Panny', 'Zobacz jeden z najstarszych kościołów Szczecina - Kościół Niepokalanego Poczęcia Najświętszej Maryi Panny w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.397069163134844', '14.66566404646372', '10', '06:00', '22:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół Niepokalanego Serca Najświętszej Marii Panny', 'Zobacz gotycki Kościół Niepokalanego Serca Najświętszej Marii Panny w Szczecinie. Otwarty całą dobę.', '53.38264477063034', '14.659430615480034', '50', NULL, NULL, '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Piotra i św. Pawła', 'Zobacz gotycki Kościół św. Piotra i św. Pawła w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.428045605863645', '14.55919485780958', '20', '06:00', '22:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Rodziny', 'Zobacz modernistyczny Kościół św. Rodziny w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.43956140593133', '14.536279167595806', '20', '09:00', '17:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Stanisława Kostki', 'Zobacz neogotycki Kościół św. Stanisława Kostki w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.43811186396396', '14.567195955957095', '10', '09:00', '17:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Bazylika archikatedralna św. Jakuba', 'Zobacz główny kościół rzymskokatolicki Szczecina - Bazylikę archikatedralna św. Jakuba w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.424819841528496', '14.556067082939737', '50', '07:00', '19:00', '02:00', '1', 'Europa;Polska;Architektura'),
('Bazylika św. Jana Chrzciciela', 'Zobacz neogotycką Bazylikę św. Jana Chrzciciela w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.42711048547056', '14.549246155956626', '40', '09:00', '17:00', '01:00', '1', 'Europa;Polska;Architektura'),
('Parafia Ewangelicko-Augsburska pw. Świętej Trójcy', 'Zobacz neogotycki kościół Parafii Ewangelicko-Augsburską pw. Świętej Trójcy w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.41933559629821', '14.564742500000001', '10', '10:00', '18:00', '02:00', '1', 'Europa;Polska;Architektura'),
('Kościół św. Wojciecha', 'Zobacz neogotycki Kościół św. Wojciecha w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.42566631649229', '14.546571698284321', '10', '07:00', '20:30', '01:00', '1', 'Europa;Polska;Architektura');

--
-- ('Kościół Chrystusa Króla', 'Zobacz gotycki Kościół Chrystusa Króla w Szczecinie. Przyjdź w podanych godzinach aby zobaczyć wnętrze.', '53.52107850025285', '14.597017413506745', '10', '18:00', '19:30', '01:00', '1', 'Europa;Polska;Architektura'),
--

-- ('Wały Chrobrego', 'Odwiedź Wały Chrobrego w Szczecin, Zachodniopomorskie, Polska', '53.429902750176595', '14.5650708160754', '10', '00:00', '24:00', '00:00', '00:30', 'Europa;Polska;Architektura'),
-- ('Brama Portowa', 'Odwiedź Brama Portowa w Szczecin, Zachodniopomorskie, Polska', '53.4250537242813', '14.55023035410361', '10', '00:00', '24:00', '00:00', '00:30', 'Europa;Polska;Architektura');

