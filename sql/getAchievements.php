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


$nickname = $_GET['nickname'];
$table = $nickname . "Achievements";
$result = mysqli_query($con,"SELECT AchievementList.AchievementName, AchievementList.Description, AchievementList.Points 
  FROM $table INNER JOIN AchievementList ON $table.Achievementid=AchievementList.Achievementid 
  WHERE $table.Progress=$table.Requirements ORDER BY $table.DateAdded DESC");

while ($row = mysqli_fetch_assoc($result)) {
  foreach( $row as $data )
    echo $data.PHP_EOL;
}

//SELECT * FROM `Users` WHERE `Personid` IN (SELECT `Personid` FROM `Ranking` WHERE `RankAll`<3)
//https://justsomephp.000webhostapp.com/login.php?email=user1@email.com&password=password1
?>