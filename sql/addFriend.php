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
$friendnick = $_GET['friendnick'];
$result = mysqli_query($con,"SELECT Personid FROM Users WHERE Nickname='$friendnick'");
$row = mysqli_fetch_array($result);
$friendid = $row[0];

if(empty($friendid) or $friendid == $personid) {
  echo -4;
}
else {
  $result = mysqli_query($con,"SELECT * FROM Friends WHERE Personid=$personid AND Friendid=$friendid");
  $row = mysqli_fetch_array($result);

  if(empty($row[0])) {
    $result = mysqli_query($con,"INSERT INTO Friends (`Personid`, `Friendid`) VALUES
        ('$personid', '$friendid'), ('$friendid', '$personid')");
    echo 0;
  }
  else {
    echo -1;
  }
}

//SELECT * FROM `Users` WHERE `Personid` IN (SELECT `Personid` FROM `Ranking` WHERE `RankAll`<3)
//https://justsomephp.000webhostapp.com/login.php?email=user1@email.com&password=password1
?>