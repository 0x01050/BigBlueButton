<!--

BigBlueButton - http://www.bigbluebutton.org

Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.

BigBlueButton is free software; you can redistribute it and/or modify it under the 
terms of the GNU Lesser General Public License as published by the Free Software 
Foundation; either version 3 of the License, or (at your option) any later 
version. 

BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along 
with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.

Author: Fred Dixon <ffdixon@bigbluebutton.org>
  
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<title>Create Your Own Meeting</title>

<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>
<script type="text/javascript" src="heartbeat.js"></script>


</head>
<body>


<%@ include file="bbb_api.jsp"%>
<%@ page import="java.util.regex.*"%>

<br>

<%
	if (request.getParameterMap().isEmpty()) {
		//
		// Assume we want to create a meeting
		//
%>
<a href="demo1.jsp">Join a Course</a>
|
<a href="demo2.jsp">Join a Selected Course</a>
|
<a href="demo3.jsp">Create Your Own Meeting</a>
|
<a href="/">Home</a>

<h2>Demo #3: Create Your Own Meeting</h2>

<p />
<FORM NAME="form1" METHOD="GET">

<table width=600 cellspacing="20" cellpadding="20"
	style="border-collapse: collapse; border-right-color: rgb(136, 136, 136);"
	border=3>
	<tbody>
		<tr>
			<td width="50%">Create your own meeting.
			<p />
			</td>
			<td width="50%">Step 1. Enter your name: <input type="text"
				name="username1" /> <br />
			<INPUT TYPE=hidden NAME=action VALUE="create"> <br />
			<input id="submit-button" type="submit" value="Create meeting" /></td>
		</tr>
	</tbody>
</table>

</FORM>

<script>
//
// We could have asked the user for both their name and a meeting title, but we'll just use their name to create a title
// We'll use JQuery to dynamically update the button
//
$(document).ready(function(){
    $("input[name='username1']").keyup(function() {
        if ($("input[name='username1']").val() == "") {
        	$("#submit-button").attr('value',"Create meeting" );
        } else {
       $("#submit-button").attr('value',"Create " +$("input[name='username1']").val()+ "'s meeting" );
        }
    });
});
</script>

<%
	} else if (request.getParameter("action").equals("create")) {
		//
		// User has requested to create a meeting
		//

		String username = request.getParameter("username1");
		String meetingID = username + "'s meeting";

		String meetingToken = "";

		//
		// This is the URL for to join the meeting as moderator
		//
		String joinURL = getJoinURL(username, meetingID, "<br>Welcome to %%CONFNAME%%.<br>");

		//
		// We're going to extract the meetingToken to enable others to join as viewers
		//
		String p = "meetingToken=[^&]*";
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(joinURL);

		if (matcher.find()) {
			meetingToken = joinURL.substring(matcher.start(), matcher
					.end());
		} else {
			out.print("Error: Did not find meeting token.");
		}
		//out.print ("Match : " + meetingToken );
		String inviteURL = BigBlueButtonURL
				+ "demo/demo3.jsp?action=invite&meetingID=" + URLEncoder.encode(meetingID, "UTF-8")
				+ "&" + meetingToken;
%>

<hr />
<h2>Meeting Created</h2>
<hr />


<table width="800" cellspacing="20" cellpadding="20"
	style="border-collapse: collapse; border-right-color: rgb(136, 136, 136);"
	border=3>
	<tbody>
		<tr>
			<td width="50%">
			<center><strong> <%=username%>'s meeting</strong> has been
			created.</center>
			</td>

			<td width="50%">
			<p>&nbsp;</p>

			Step 2. Invite others using the following <a href="<%=inviteURL%>">link</a> (shown below):
			<form name="form2" method="POST"><textarea cols="62" rows="5"
				name="myname" style="overflow: hidden">
<%=inviteURL%>
</textarea></form>
			<p>&nbsp;
			<p />Step 3. Click the following link to start your meeting:
			<p>&nbsp;</p>
			<center><a href="<%=joinURL%>">Start Meeting</a></center>
			<p>&nbsp;</p>

			</td>
		</tr>
	</tbody>
</table>







<%
	} else if (request.getParameter("action").equals("enter")) {
		//
		// The user is now attempting to joing the meeting
		//
		String meetingID = request.getParameter("meetingID");
		String username = request.getParameter("username");
		String meetingToken = request.getParameter("meetingToken");

		String enterURL = BigBlueButtonURL
				+ "demo/demo3.jsp?action=join&username="
				+ URLEncoder.encode(username, "UTF-8") + "&meetingID="
				+ URLEncoder.encode(meetingID, "UTF-8");

		if (isMeetingRunning(meetingToken, meetingID).equals("true")) {
			//
			// The meeting has started -- bring the user into the meeting.
			//
%>
<script type="text/javascript">
	window.location = "<%=enterURL%>";
</script>
<%
	} else {
			//
			// The meeting has not yet started, so check until we get back the status that the meeting is running
			//
			String checkMeetingStatus = getURLisMeetingRunning(
					meetingToken, meetingID);
%>

<script type="text/javascript">
$(document).ready(function(){
		$.jheartbeat.set({
		   url: "<%=checkMeetingStatus%>",
		   delay: 5000
		}, function () {
			mycallback();
		});
		});


function mycallback() {
	// Not elegant, but works around a bug in IE8
	var isMeetingRunning = ($("#HeartBeatDIV").text().search("true") > 0 );

	if ( isMeetingRunning) {
	window.location = "<%=enterURL%>"; 
	}
}
</script>

<hr />
<h2><strong><%=meetingID%></strong> has not yet started.</h2>
<hr />


<table width=600 cellspacing="20" cellpadding="20"
	style="border-collapse: collapse; border-right-color: rgb(136, 136, 136);"
	border=3>
	<tbody>
		<tr>
			<td width="50%">

			<p>Hi <%=username%>,</p>
			<p>Now waiting for the moderator to start <strong><%=meetingID%></strong>.</p>
			<br />
			<p>(Your browser will automatically refresh and join the meeting
			when it starts.)</p>
			</td>
			<td width="50%"><img src="polling.gif"></img></td>
		</tr>
	</tbody>
</table>


<%
	}
	} else if (request.getParameter("action").equals("invite")) {
		//
		// We have an invite to an active meeting.  Ask the person for their name 
		// so they can join.
		//
		String meetingID = request.getParameter("meetingID");
		String meetingToken = request.getParameter("meetingToken");
%>

<hr />
<h2>Invite</h2>
<hr />

<FORM NAME="form3" METHOD="GET">

<table width=600 cellspacing="20" cellpadding="20"
	style="border-collapse: collapse; border-right-color: rgb(136, 136, 136);"
	border=3>
	<tbody>
		<tr>
			<td width="50%">

			<p />You have been invited to join<br />
			<strong><%=meetingID%></strong>.
			</td>

			<td width="50%">Enter your name: <input type="text"
				name="username" /> <br />
			<INPUT TYPE=hidden NAME=meetingID VALUE="<%=meetingID%>"> <INPUT
				TYPE=hidden NAME=meetingToken VALUE="<%=meetingToken%>"> <INPUT
				TYPE=hidden NAME=action VALUE="enter"> <br />
			<input type="submit" value="Join" /></td>
		</tr>
	</tbody>
</table>

</FORM>




<%
	} else if (request.getParameter("action").equals("join")) {
		//
		// We have an invite request to join an existing meeting and the meeting is running
		//
		// We don't need to pass a meeting descritpion as it's already been set by the first time 
		// the meeting was created.
		String joinURL = getJoinURL(request.getParameter("username"), request.getParameter("meetingID"), null);

		if (joinURL.startsWith("http://")) {
%>

<script language="javascript" type="text/javascript">
  window.location.href="<%=joinURL%>";
</script>

<%
	} else { 
%>

Error: getJoinURL() failed
<p /><%=joinURL%> 

<%
 	}
 }
 %> 

<%@ include file="demo_footer.jsp"%>

</body>
</html>