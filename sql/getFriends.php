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


$personid = $_GET['personid'];
$result = mysqli_query($con,"SELECT Users.Nickname, Users.PointsAll, Users.PointsNature, Users.PointsArchitecture 
  FROM Friends INNER JOIN Users ON Friends.Friendid=Users.Personid 
  WHERE Friends.Personid=$personid");

while ($row = mysqli_fetch_assoc($result)) {
  foreach( $row as $data )
    echo $data.PHP_EOL;
}

//SELECT * FROM `Users` WHERE `Personid` IN (SELECT `Personid` FROM `Ranking` WHERE `RankAll`<3)
//https://justsomephp.000webhostapp.com/login.php?email=user1@email.com&password=password1
?>