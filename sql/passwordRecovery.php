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
$result = mysqli_query($con,"SELECT Password FROM Users where Email='$email'");
$row = mysqli_fetch_array($result);

if(empty($row[0]))
  echo -1;
else {
    echo $row[0];
}

//https://justsomephp.000webhostapp.com/login.php?email=user1@email.com&password=password1
?>