﻿/*** BigBlueButton open source conferencing system - http://www.bigbluebutton.org/* * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).** This program is free software; you can redistribute it and/or modify it under the* terms of the GNU Lesser General Public License as published by the Free Software* Foundation; either version 3.0 of the License, or (at your option) any later* version.* * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.** You should have received a copy of the GNU Lesser General Public License along* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.**/package org.bigbluebutton.main.model{	 	import flash.events.NetStatusEvent;	import flash.events.TimerEvent;	import flash.net.NetConnection;	import flash.net.ObjectEncoding;	import flash.utils.Timer;		import org.as3commons.logging.api.ILogger;	import org.as3commons.logging.api.getClassLogger;		[Bindable]	/**	 * Test RTMP port.	 * 	 * @author Thijs Triemstra ( info@collab.nl )	 */		public class PortTest 	{		private static const LOGGER:ILogger = getClassLogger(PortTest);      		/**		* Protocol name.		*/				private var protocol 		: String;				/**		* Protocol name (uppercase).		*/				public var protocolName 	: String;				/**		* RTMP hostname.		*/				private var hostname 		: String;				/**		* RTMP port.		*/				public var port 			: String;				/**		* RTMP port.		*/				public var portName 		: String = "Default";				/**		* RTMP application.		*/				private var application 	: String;		/**		* Base RTMP URI.		*/				private var baseURI 		: String;				/**		* RTMP connection.		*/				public var nc 				: NetConnection;				/**		* Connection status.		*/				public var status 			: String;				private var _connectionListener:Function;				/**		* Timer to control timeout of connection test		*/		private var testTimeout:Number = 0;				/**		* Timer to control timeout of connection test		*/		private var connectionTimer:Timer;				/**		* Set default encoding to AMF0 so FMS also understands.		*/				NetConnection.defaultObjectEncoding = ObjectEncoding.AMF0;				/**		 * Create new port test and connect to the RTMP server.		 * 		 * @param protocol		 * @param hostname		 * @param port		 * @param application		 * @testTimeout timeout of test in milliseconds		 */					public function PortTest( protocol : String = "",								  hostname : String = "",								  port : String = "",								  application : String = "",								  testTimeout : Number = 10000								  ) 		{			this.protocol = protocol;			this.protocolName = protocol.toUpperCase();			this.hostname = hostname;			this.application = application;			this.testTimeout = testTimeout;						if ( port.length > 0 )			{				this.portName = port;				this.port = ":" + port;			}			else 			{				this.port = port;			}			// Construct URI.			this.baseURI = this.protocol + "://" + this.hostname + this.port + "/" + this.application;			//		}				/**		 * Start connection.		 */				public function connect() : void		{			this.nc = new NetConnection();			this.nc.client = this;
      this.nc.proxyType = "best";
			this.nc.addEventListener( NetStatusEvent.NET_STATUS, netStatus );			// connect to server			try 			{				// Create connection with the server.				this.nc.connect( this.baseURI, "portTestMeetingId", "portTestDummyUserId" );				status = "Connecting...";								connectionTimer = new Timer(testTimeout, 1);				connectionTimer.addEventListener(TimerEvent.TIMER, connectionTimeout);				connectionTimer.start();			}			catch( e : ArgumentError ) 			{				// Invalid parameters.				status = "ERROR: " + e.message;			}			}				/**		* Method called when connection timed out		*/		public function connectionTimeout (e:TimerEvent) : void 		{			status = "FAILED";			_connectionListener(status, protocol, hostname, port, application);			close();		}				/**		 * Close connection.		 */				public function close() : void		{				//Stop timeout timer when connected/rejected			connectionTimer.stop();						// Remove listener.			this.nc.removeEventListener( NetStatusEvent.NET_STATUS, netStatus );			// Close the NetConnection.			this.nc.close();		}					/**		 * Catch NetStatusEvents.		 * 		 * @param event		 */				protected function netStatus( event : NetStatusEvent ) : void 		{			var info : Object = event.info;			var statusCode : String = info.code;			//			if ( statusCode == "NetConnection.Connect.Success" )			{				status = "SUCCESS";				_connectionListener(status, protocol, hostname, port, application);			}			else if ( statusCode == "NetConnection.Connect.Rejected" ||				 	  statusCode == "NetConnection.Connect.Failed" || 				 	  statusCode == "NetConnection.Connect.Closed" ) 			{				status = "FAILED";				_connectionListener(status, protocol, hostname, port, application);			}			// Close NetConnection.			close();		}				public function onBWCheck(... rest):Number { 			return 0; 		} 		public function onBWDone(... rest):void { 			var p_bw:Number; 			if (rest.length > 0) p_bw = rest[0]; 			// your application should do something here 			// when the bandwidth check is complete 			LOGGER.debug("bandwidth = {0} Kbps.", [p_bw]); 		}				public function addConnectionSuccessListener(listener:Function):void {			_connectionListener = listener;		}	}}
