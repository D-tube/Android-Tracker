<?php
     
	class DBFunction {
    private $conn;
 
    // constructor
    function __construct() {
        require_once 'dbConnect.php';
        // connecting to database
        $db = new DBConnect();
        $this->conn = $db->connect();
    }
	
	/**
     * Storing user location
     * returns true for sucessfull update
	 * false for unsucessfull update
     */
	public function addLocation($uniqueId, $latitude, $longitude, $dateCreated) {
    $stmt = $this->conn->prepare("INSERT INTO realtime(uniqueId, latitude, longitude, dateCreated) VALUES(?, ?, ?, ?)");
    $stmt->bind_param("ssss", $uniqueId, $latitude, $longitude, $dateCreated);
    $result = $stmt->execute();
    $stmt->close();

    // check for successful store
    if ($result === true) {
        return true;
    } else {
        return false;
    }
}

}
	 
?>
