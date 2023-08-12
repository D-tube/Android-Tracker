<?php
    require_once 'include/dbFunctions.php';
	$db = new DBFunction();
	 
	// json response array
	$response = array();
if (isset($_POST['uniqueId']) && isset($_POST['latitude']) && isset($_POST['longitude']) && isset($_POST['dateCreated'])) {
	
	// receiving the post params
	$uniqueId = $_POST['uniqueId'];
	$latitude = $_POST['latitude'];
	$longitude = $_POST['longitude'];
	$dateCreated = $_POST['dateCreated'];
	
	// add new position to db
	$location = $db->addLocation($uniqueId, $latitude, $longitude, $dateCreated);
	
	if ($location) {
		$response["error"] = false; // Successfully added
		echo json_encode($response);
	} else {
		$response["error"] = true; // Error occurred
		echo json_encode($response);
	}
}

	 
?>
