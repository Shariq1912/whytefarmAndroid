<?php

$servername = "localhost";
$dbname   = "androidwhytefarm";
$username = "whytefarm_dev";
$password = "whytefarms@123";

try {
     $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
     $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (\PDOException $e) {
     die("Connection failed: " . $e->getMessage());
}

?>
