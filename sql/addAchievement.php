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
$achievement = $_GET['achievement'];
$table = $nickname . "Achievements";
$str = str_replace("+", " ", $achievement);
$result = mysqli_query($con,"SELECT PointsAll, PointsNature, PointsArchitecture FROM Users WHERE Nickname='$nickname'");
$row = mysqli_fetch_assoc($result);
$pointsAll = $row["PointsAll"];
$pointsNature = $row["PointsNature"];
$pointsArchitecture = $row["PointsArchitecture"];

$result = mysqli_query($con,"SELECT Achievementid, Tags, Points FROM AchievementList WHERE AchievementName='$achievement'");
$row = mysqli_fetch_assoc($result);
$achievementid = $row["Achievementid"];
$tags = $row["Tags"];
$points = $row["Points"];

$pointsAll = $pointsAll + $points;
if(strpos($tags,"Natura")){
  $pointsNature = $pointsNature + $points;
}
if(strpos($tags,"Architektura")){
  $pointsArchitecture = $pointsArchitecture + $points;
}
if(strpos($tags,"Woliński Park Narodowy")){
  $result = mysqli_query($con,"SELECT Progress, Requirements, Achievementid
    FROM $table WHERE Achievementid IN (SELECT Achievementid FROM AchievementList WHERE AchievementName='Woliński Park Narodowy')");
  $row = mysqli_fetch_assoc($result);
  $progress = $row["Progress"];
  $req = $row["Requirements"];
  $id = $row["Achievementid"];
  if ($progress < $req)
  {
    $data = $progress;
    $data = $data + 1;
    $result = mysqli_query($con,"UPDATE $table SET Progress=$data, DateAdded=NOW() WHERE Achievementid=$id;");
    if ($data==$req)
    {
      $result = mysqli_query($con,"SELECT Points FROM AchievementList WHERE Achievementid=$id");
      $row = mysqli_fetch_array($result);
      $data = $row[0];
      $pointsAll = $pointsAll + $data;
      $pointsNature = $pointsNature + $data;
    }
  }
}

$result = mysqli_query($con,"UPDATE $table
SET Progress = 1, DateAdded=NOW()
WHERE Achievementid = $achievementid;");

$result = mysqli_query($con,"UPDATE Users
  SET PointsAll = $pointsAll, PointsNature = $pointsNature, PointsArchitecture = $pointsArchitecture
  WHERE Nickname = '$nickname';");

echo 0;

?>