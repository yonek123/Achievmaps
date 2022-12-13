<?php
/*$host = "remotemysql.com";
$username = "Y87k6ClIBk";
$password = "xiYUETYqHA";
$db = "Y87k6ClIBk";*/

$host = "localhost";
$username = "id17948844_user123";
$password = "q>yx}KBTKGBQj6}h";
$db = "id17948844_achievemaps";
// Create connection
$con = @mysqli_connect($host, $username, $password, $db);

if (!$con) {
  die("-2");
}


$achievement = $_GET['achievement'];
$result = mysqli_query($con,"SELECT OpenTime, CloseTime, ViewTime
  FROM AchievementList
  WHERE AchievementName='$achievement'");

while ($row = mysqli_fetch_assoc($result)) {
  foreach( $row as $data )
    echo $data.PHP_EOL;
}
?>