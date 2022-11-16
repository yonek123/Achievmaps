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



$email = $_GET['email'];
$nickname = $_GET['nickname'];
$password = $_GET['password'];
$result = mysqli_query($con,"SELECT Personid FROM Users where Email='$email'");
$row = mysqli_fetch_array($result);
$data = $row[0];
if(!empty($data))
  echo 1;
else{
  $result = mysqli_query($con,"SELECT Personid FROM Users where Nickname='$nickname'");
  $row = mysqli_fetch_array($result);
  $data = $row[0];  
  if(!empty($data))
    echo 2;
  else{
    $result = mysqli_query($con, "INSERT INTO `Users` (`Email`, `Password`, `Nickname`, `PointsAll`, `PointsNature`, `PointsArchitecture`) VALUES
      ('$email', '$password', '$nickname', 0, 0, 0)");
    
    $result = mysqli_query($con,"SELECT Personid FROM Users where Email='$email'");
    $row = mysqli_fetch_array($result);
    $personid = $row[0];
    $table = $nickname . "Achievements";

    $result = mysqli_query($con,"CREATE TABLE IF NOT EXISTS $table (
      `Achievementid` int(11) NOT NULL UNIQUE,
      `Progress` int(11) NOT NULL,
      `Requirements` int(11) NOT NULL,
      `DateAdded` datetime DEFAULT current_timestamp(),
      FOREIGN KEY (`Achievementid`) REFERENCES `AchievementList`(`Achievementid`)
    ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");

    $result = mysqli_query($con,"SELECT Achievementid, Requirements FROM AchievementList");
    while ($row = mysqli_fetch_assoc($result)) {
      $result2 = mysqli_query($con, "INSERT INTO $table (`Achievementid`, `Progress`, `Requirements`) VALUES
        ('$row[Achievementid]', 0, '$row[Requirements]')");
    }

    echo 0;
  }
}
?>