<?php

if(function_exists("curl_init()"))
{
	function bbb_wrap_simplexml_load_file($url)
	{
		$ch = curl_init() or die ( curl_error() );
		curl_setopt( $ch, CURLOPT_URL, $url );
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
		$data = curl_exec( $ch );
		curl_close( $ch );
		return (new SimpleXMLElement($data));
	} 	
}
else
{
	/*
	 * REQUIREMENT - PHP.INI
	 * allow_url_fopen = On
	*/
	 function bbb_wrap_simplexml_load_file($url)
	 {
	 	return (simplexml_load_file($url));
	 }
}

/*
@param
$userName = userName AND meetingID (string) 
$welcomeString = welcome message (string)
$modPW = moderator password (string)
$vPW = viewer password (string)
$voiceBridge = voice bridge (integer)
$logout = logout url (url)
*/
// create a meeting and return the url to join as moderator


// TODO::
// create some set methods 
class BigBlueButton {
	
	var $userName = array();
	var $meetingID; // the meeting id
	
	var $welcomeString;
	// the next 2 fields are maybe not needed?!?
	var $modPW; // the moderator password 
	var $attPW; // the attendee pw
	
	var $securitySalt; // the security salt; gets encrypted with sha1
	var $URL; // the url the bigbluebutton server is installed
	var $sessionURL; // the url for the administrator to join the sessoin
	var $userURL;
	
	var $conferenceIsRunning = false;
	// this constructor is used to create a BigBlueButton Object
	// use this object to create servers
	// Use is either 0 arguments or all 7 arguments
public function __construct() {


	$numargs = func_num_args();

	if( $numargs == 0 ) {
	#	echo "Constructor created";
	}
	// pass the information to the class variables
	else if( $numargs >= 6 ) {
		$this->userName = func_get_arg(0);
		$this->meetingID = func_get_arg(1);
		$this->welcomeString = func_get_arg(2);
		$this->modPW = func_get_arg(3);
		$this->attPW = func_get_arg(4);
		$this->securitySalt = func_get_arg(5);
		$this->URL = func_get_arg(6);
		

 		$arg_list = func_get_args();
#debug output for the number of arguments
#    	for ($i = 0; $i < $numargs; $i++) {
#        	echo "Argument $i is: " . $arg_list[$i] . "<br />\n";
#    	}
    
//    $this->createMeeting( $this->userName, $this->meetingID, $this->welcomeString, $this->modPW, $this->attPW, $this->securitySalt, $this->URL );
 //   echo "Object created";
//	 echo '<br />';
	}// end else if
}

public function checkCurl($url) {
	/*
 * Is libcurl available ?
 */
if(function_exists("curl_init()"))
{
	//function bbb_wrap_simplexml_load_file($url)
	//{
		$ch = curl_init() or die ( curl_error() );
		curl_setopt( $ch, CURLOPT_URL, $url );
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
		$data = curl_exec( $ch );
		curl_close( $ch );
		return (new SimpleXMLElement($data));
//	} 	
}
else
{
	/*
	 * REQUIREMENT - PHP.INI
	 * allow_url_fopen = On
	*/
	// function bbb_wrap_simplexml_load_file($url)
	// {
	 	return (simplexml_load_file($url));
	// }
}
}

// a whole bunch of set and get functions for the variables
// set the user name

public function setUserName( $userName ) {
	$this->userName = urlencode($userName);
}

public function getUserName() { return $this->userName; }

// set the meetingID
public function setMeetingID( $meetingID ) {
	$this->meetingID = urlencode($meetingID);
}

public function getMeetingID() { return $this->meetingID; }

// unsafe function, try not to use it
public function setModeratorPW( $modPW ) {
	$this->modPW = $modPW;
}

public function getModeratorPW() { return $this->modPW; }

// same comment as for setModeratorPW
public function setAttendeePW( $attPW ) {
	$this->attPW = $attPW;
}

public function getAttendeePW() { $this->attPW; }

public function setSecuritySalt( $salt ) {
	$this->securitySalt = $salt;
}

public function getSecuritySalt() { return $this->securitySalt; }

public function setURL( $URL ) {
	$this->URL = $URL;
}

public function getURL() { return $this->URL; }
	
public function createMeeting( $username, $meetingID, $welcomeString, $mPW, $aPW, $SALT, $URL ) {
	$url_create = $URL."api/create?";
	$url_join = $URL."api/join?";
	$voiceBridge = 70000 + rand(0, 9999);

	$params = 'name='.urlencode($meetingID).'&meetingID='.urlencode($meetingID).'&attendeePW='.$aPW.'&moderatorPW='.$mPW.'&voiceBridge='.$voiceBridge;

	if( trim( $welcomeString ) ) 
		$params .= '&welcome='.urlencode($welcomeString);

	$xml = bbb_wrap_simplexml_load_file($url_create.$params.'&checksum='.sha1("create".$params.$SALT) );
	
	if( $xml && $xml->returncode == 'SUCCESS' ) {
		$params = 'meetingID='.urlencode($meetingID).'&fullName='.urlencode($username).'&password='.$mPW;
		// create the url
		#$this->sessionURL = $url_join.$params.'&checksum='.sha1("join".$params.$SALT);
		$conferenceIsRunning = true;
		return ($url_join.$params.'&checksum='.sha1("join".$params.$SALT) );
	}	
	else if( $xml ) {
		return ( $xml->messageKey.' : '.$xml->message );
	}
	else {
		return ('Unable to fetch URL '.$url_create.$params.'&checksum='.sha1("create".$params.$SALT) );
	}
}

// return the url to join a meeting as viewer
public function joinAsViewer( $meetingID, $userName, $welcomeString = '', $aPW, $SALT, $URL ) {
	$url_join = $URL."api/join?";
	$params = 'meetingID='.urlencode($meetingID).'&fullName='.urlencode($userName).'&password='.$aPW;
	$userURL = $url_join.$params.'&checksum='.sha1("join".$params.$SALT);
	return ($url_join.$params.'&checksum='.sha1("join".$params.$SALT) );
}

// getURLisMeetingRunning() -- return an URL that the client can use to poll for whether the given meeting is running
public function getUrlOfRunningMeeting( $meetingID, $URL, $SALT ) {
	$base_url = $URL."api/isMeetingRunning?";
	$params = 'meetingID='.urlencode($meetingID);
	return ($base_url.$params.'&checksum='.sha1("isMeetingRunning".$params.$SALT) );	
}

// isMeetingRunning() -- check the BigBlueButton server to see if the meeting is running (i.e. there is someone in the meeting)
public function isMeetingRunning( $meetingID, $URL, $SALT ) {
	$xml = bbb_wrap_simplexml_load_file( BigBlueButton::getUrlOfRunningMeeting( $meetingID, $URL, $SALT ) );
	if( $xml && $xml->returncode == 'SUCCESS' ) 
		return ( ( $xml->running == 'true' ) ? true : false);
	else
		return ( false );
}

// getURLMeetingInfo() -- return a URL to get MeetingInfo
public function getUrlFromMeetingInfo( $meetingID, $modPW, $URL, $SALT ) {
	$base_url = $URL."api/getMeetingInfo?";
	$params = 'meetingID='.urlencode($meetingID).'&password='.$modPW;
	return ( $base_url.$params.'&checksum='.sha1("getMeetingInfo".$params.$SALT));	
}

 // getMeetingInfo() -- Calls getMeetingInfo to obtain information on a given meeting.
public function getMeetingInfo( $meetingID, $modPW, $URL, $SALT ) {
	$xml = bbb_wrap_simplexml_load_file( BigBlueButton::getUrlFromMeetingInfo( $meetingID, $modPW, $URL, $SALT ) );
	return ( str_replace('</response>', '', str_replace("<?xml version=\"1.0\"?>\n<response>", '', $xml->asXML())));
}

 // getMeetingXML() --calls isMeetingRunning to obtain the xml values of the response of the returned URL
public function getMeetingXML( $meetingID, $URL, $SALT ) {
	$xml = bbb_wrap_simplexml_load_file( BigBlueButton::getUrlOfRunningMeeting( $meetingID, $URL, $SALT ) );
	return ( str_replace('</response>', '', str_replace("<?xml version=\"1.0\"?>\n<response>", '', $xml->asXML())));
}

// getURLMeetings() -- return a URL for listing all running meetings
public function getUrlMeetings($URL, $SALT) { 
	$base_url = $URL."api/getMeetings?";
	$params = 'random='.(rand() * 1000 );
	return ( $base_url.$params.'&checksum='.sha1("getMeetings".$params.$SALT));
}

public function endMeeting( $meetingID, $mPW, $URL, $SALT ) {
	$base_url = $URL."api/end?";
	$params = 'meetingID='.urlencode($meetingID).'&password='.$mPW;
	return ( $base_url.$params.'&checksum='.sha1("end".$params.$SALT) );
}



// TODO: WRITE AN ITERATOR WHICH GOES OVER WHATEVER IT IS BEING TOLD IN THE API AND LIST INFORMATION
/* we have to define at least 2 variable fields for getInformation to read out information at any position
The first is: An identifier to chose if we look for attendees or the meetings or something else
The second is: An identifier to chose what integrated functions are supposed to be used

@param IDENTIFIER -- needs to be put in for the function to identify the information to print out
		     current values which can be used are 'attendee' and 'meetings'
@param meetingID -- needs to be put in to identify the meeting
@param modPW -- needs to be put in if the users are supposed to be shown or to retrieve information about the meetings
@param URL -- needs to be put in the URL to the bigbluebutton server
@param SALT -- needs to be put in for the security salt calculation

Note: If 'meetings' is used, then only the parameters URL and SALT needs to be used
      If 'attendee' is used, then all the parameters needs to be used
*/
public function getInformation( $IDENTIFIER, $meetingID, $modPW, $URL, $SALT ) {
	// if the identifier is null or '', then return false
	if( $IDENTIFIER == "" || $IDENTIFIER == null ) {
		echo "You need to type in a valid value into the identifier.";
		return false;
	}
	// if the identifier is attendee, call getUsers
	else if( $IDENTIFIER == 'attendee' ) {
		return getUsers( $meetingID, $modPW, $URL, $SALT );
	}
	// if the identifier is meetings, call getMeetings
	else if( $IDENTIFIER == 'meetings' ) {
		return getMeetings( $URL, $SALT );
	}
	// return nothing
	else {
		return true;
	}
	
}


// return the users in the current conference
public function getUsers( $meetingID, $modPW, $URL, $SALT ) {
	$xml = bbb_wrap_simplexml_load_file( BigBlueButton::getUrlFromMeetingInfo( $meetingID, $modPW, $URL, $SALT ) );
	if( $xml && $xml->returncode == 'SUCCESS' ) {
		ob_start();
		if( count( $xml->attendees ) && count( $xml->attendees->attendee ) ) {

			foreach ( $xml->attendees->attendee as $attendee ) {
				echo $attendee->fullName.'<br />';
			}
		}
		return (ob_end_flush());
	}
	else {
		return (false);
	}
}

// following FUNCTION is for TESTING!!!
public function getUsersXML( $meetingID, $modPW, $URL, $SALT ) {
	$xml = bbb_wrap_simplexml_load_file( getUrlMeetingInfo( $meetingID, $modPW, $URL, $SALT ) );
	if( $xml && $xml->returncode == 'SUCCESS' ) {
		ob_start();
		echo '<attendees>';		
		if( count( $xml->attendees ) && count( $xml->attendees->attendee ) ) {
			foreach ( $xml->attendees->attendee as $attendee ) {
				echo '<attendee>';
				echo $attendee->fullName.'<br />';
				echo '</attendee>';
			}
		}
		else {
			echo '<br />'."There are currently no users in the session right now.".'<br />';
		}
		echo '</attendees>';
			return (ob_end_flush());
	}
	else {
		return (false);
	}
}

// getMeetings() -- Calls getMeetings to obtain the list of meetings, then calls getMeetingInfo for each meeting and concatenates the result.
public function getMeetings( $URL, $SALT ) {
	$xml = bbb_wrap_simplexml_load_file( BigBlueButton::getUrlMeetings( $URL, $SALT ) );
	if( $xml && $xml->returncode == 'SUCCESS' ) {
		if( $xml->messageKey )
			return ( $xml->message->asXML() );	
		ob_start();
		echo '<meetings>';
		if( count( $xml->meetings ) && count( $xml->meetings->meeting ) ) {
			foreach ($xml->meetings->meeting as $meeting)
                        {
                                echo '<meeting>';
                                echo BigBlueButton::getMeetingInfo($meeting->meetingID, $meeting->moderatorPW, $URL, $SALT);
                                echo '</meeting>';
                        }
                }
       echo '</meetings>';
                return (ob_get_clean());
        }
        else {
                return (false);
        }
}

function getServerIP() {
         // get the server url
         $sIP = $_SERVER['SERVER_ADDR'];
         return $serverIP = 'http://'.$sIP.'/bigbluebutton/';
}


# function taken from http://www.php.net/manual/en/function.mt-rand.php
# modified by Sebastian Schneider
# credits go to www.mrnaz.com
function rand_string($len, $chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789')
{
    $string = '';
    for ($i = 0; $i < $len; $i++)
    {
        $pos = rand(0, strlen($chars)-1);
        $string .= $chars{$pos};
    }
    return (sha1($string));
}


}
?>
