<?php



//$client = new MongoClient();

$client = new MongoClient("mongodb://194.225.238.241:27017");  
$db = $client->kaa; 
$log = $db->logs_91832476309660881037;
$cursor= $log->count();
echo var_dump($cursor);


echo "OK!!!!";
?>
