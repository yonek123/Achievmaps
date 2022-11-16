<?php
$host = "localhost";
$username = "id17948844_user123";
$password = "q>yx}KBTKGBQj6}h";
$db = "id17948844_achievemaps";
// Create connection
$conn = mysqli_connect($host, $username, $password, $db);
// Check connection
if (!$conn) {
  die("Connection failed: " . $conn->connect_error);
}

$myFile = fopen("news1.png","r") or die("Nono bear");
$fileData = fread($myFile, filesize("news1.png"));
fclose($myFile);

$myFile2 = fopen("news2.jpg","r") or die("Nono bear");
$fileData2 = fread($myFile2, filesize("news2.jpg"));
fclose($myFile2);

$sql = "INSERT INTO `News` (`Newsid`,`Title`, `Article`, `ArticleImage`, `Tags`) VALUES
(1,'Co nowego w wersji 0.1', 'Cóż można powiedzieć o nowościach w pierwszej publicznej wersji aplikacji, jeśli sama aplikacja jest nowością? Najprościej w tej sytuacji będzie więc opisać sam program.\n Achievemaps to aplikacja, która dodaje elementy typowe dla gier wideo do turystyki: zdobywanie osiągnięć, punktów i pozycji w rankingu! Nie ma znaczenia czy poszukujesz katalogu zabytków w twojej okolicy, czy chcesz zostać numerem jeden pod względem podbitych szczytów, na pewno znajdziesz tu coś dla siebie. Nie pozostaje nam nic innego jak życzyć powodzenia i dobrej pogody!', '" . addslashes($fileData) ."', 'Patch'),
(2,'Przerwa techniczna', 'Informujemy że dnia 30.05.2022, od godziny 3:00 serwery będą nieaktywne na czas wprowadzania niezbędnych poprawek. Przewidywany czas trwania przerwy: 2h.', '" . addslashes($fileData2) ."', 'Service');";

if ($conn->query($sql) === TRUE) {
  echo "New record created successfully";
} else {
  echo "Error: " . $sql . "<br>" . $conn->error;
}


$sql = "SELECT ArticleImage FROM `News` WHERE Newsid=1";
$result = mysqli_query($conn, $sql) or die("<b>Error:</b> Problem on Retrieving Image BLOB<br/>" . mysqli_error($conn));
$row = mysqli_fetch_array($result);
file_put_contents('test.png',$row['ArticleImage']);

$conn->close();
?>