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
$password = $_GET['password'];
$result = mysqli_query($con,"SELECT * FROM Users where Email='$email' 
  and Password='$password'");
$row = mysqli_fetch_assoc($result);

if(empty($row["Personid"]))
  echo -1;
else {
  foreach( $row as $data )
    echo $data.PHP_EOL;
}

//https://justsomephp.000webhostapp.com/login.php?email=user1@email.com&password=password1
?>