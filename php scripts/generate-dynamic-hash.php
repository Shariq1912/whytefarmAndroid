<?php


//error_reporting(E_ALL);
//ini_set("display_errors", "on");

require('./config.php');
$api_secret = $_POST["api_secret"];

$app_env = "_prod_";
$key ="";
$salt = "";
if(isset($_POST["app_env"])) {
    
    $app_env = $_POST["app_env"];
}

$stmt = $conn->query("SELECT * FROM settings LIMIT 1");
$obj = $stmt->fetch();


if(($obj["api_secret"] != '' && $obj["api_secret"] != null) && 
    ($api_secret != '' && $api_secret != null) && 
    $obj["api_secret"] === $api_secret) {
    
    
    $key = $obj["payu".$app_env."key"];
    $salt = $obj["payu".$app_env."salt"];
    
    $hashData = $_POST["hashData"].$salt;
    
    $output["hashString"] = strtolower(hash('sha512', $hashData));
    $output["code"] ="200";
        
} else {
        
    $output["code"] ="403";
    $output["error"] = "api_key_error";
    
}

echo json_encode($output);

?>
