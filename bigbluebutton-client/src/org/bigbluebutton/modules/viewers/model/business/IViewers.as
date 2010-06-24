/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2008 by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
* 
*/
package org.bigbluebutton.modules.viewers.model.business
{
	import mx.collections.ArrayCollection;
	
	import org.bigbluebutton.modules.viewers.model.vo.BBBUser;
	
	public interface IViewers {
		function get me():BBBUser;
		function get users():ArrayCollection;
		function addUser(newuser:BBBUser):void;
		function hasParticipant(id:Number):Boolean;
		function getParticipant(id:Number):BBBUser;
		function removeParticipant(userid:Number):void;
		function removeAllParticipants():void;
		function newUserStatus(id:Number, status:String, value:Object):void;
		function hasOnlyOneModerator():Boolean;
		function getTheOnlyModerator():BBBUser;
	}
}