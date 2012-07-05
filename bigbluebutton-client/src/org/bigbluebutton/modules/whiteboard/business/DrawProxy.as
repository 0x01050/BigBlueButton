/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
* 
*/
package org.bigbluebutton.modules.whiteboard.business
{
	import com.asfusion.mate.events.Dispatcher;
	
	import flash.events.NetStatusEvent;
	import flash.events.SyncEvent;
	import flash.events.TimerEvent;
	import flash.net.NetConnection;
	import flash.net.Responder;
	import flash.net.SharedObject;
	
	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.modules.present.events.PresentationEvent;
	import org.bigbluebutton.modules.whiteboard.business.shapes.DrawObject;
	import org.bigbluebutton.modules.whiteboard.business.shapes.DrawObjectFactory;
	import org.bigbluebutton.modules.whiteboard.business.shapes.GraphicObject;
	import org.bigbluebutton.modules.whiteboard.business.shapes.TextFactory;
	import org.bigbluebutton.modules.whiteboard.business.shapes.TextObject;
	import org.bigbluebutton.modules.whiteboard.events.PageEvent;
	import org.bigbluebutton.modules.whiteboard.events.StartWhiteboardModuleEvent;
	import org.bigbluebutton.modules.whiteboard.events.WhiteboardDrawEvent;
	import org.bigbluebutton.modules.whiteboard.events.WhiteboardPresenterEvent;
	import org.bigbluebutton.modules.whiteboard.events.WhiteboardUpdate;
	
	/**
	 * The DrawProxy class is a Delegate class for the Red5 Server. It communicates directly with the Red5
	 * server and abstracts that communication so that other classes don't have to worry about it 
	 * @author dzgonjan
	 * 
	 */	
	public class DrawProxy
	{	
		private var url:String;
		private var host:String;
		private var conference:String;
		private var room:String;
		private var userid:Number;
		private var connection:NetConnection;
		
		private var drawSO:SharedObject;
		private var manualDisconnect:Boolean = false;
		private var dispatcher:Dispatcher;
		private var drawFactory:DrawObjectFactory;
		private var textFactory:TextFactory;
		
		private var initialLoading:Boolean = true;
		private var initialPageEvent:PageEvent;
		
		/**
		 * The default constructor. Initializes the Connection and the red5 NetConnection class, which
		 * interacts with the red5 server.
		 * @param drawVO The drawVO Value Object which holds the objects being drawn by the user on the Whiteboard
		 * 
		 */		
		public function DrawProxy()
		{
			drawFactory = new DrawObjectFactory();
			textFactory = new TextFactory();
			dispatcher = new Dispatcher();
		}
		
		public function connect(e:StartWhiteboardModuleEvent):void{
			extractAttributes(e.attributes);
			
			drawSO = SharedObject.getRemote("drawSO", url, false);
            drawSO.addEventListener(SyncEvent.SYNC, sharedObjectSyncHandler);
            drawSO.addEventListener(NetStatusEvent.NET_STATUS, netStatusEventHandler);
            drawSO.client = this;
            drawSO.connect(connection);
		}
		
		private function netStatusEventHandler(e:NetStatusEvent):void{
			LogUtil.debug("Whiteboard Shared Object Net Status: " + e.info.code);
			LogUtil.debug("whiteboard connection uri: " + connection.uri);
			LogUtil.debug("whiteboard shared object uri: " + url + "/" + room);
		}
		
		private function extractAttributes(a:Object):void{
			host = a.host as String;
			conference = a.conference as String;
			room = a.room as String;
			userid = a.userid as Number;
			connection = a.connection;
			url = connection.uri;
		}
		
		/**
		 * Once a shared object is created, it is synced accross all clients, and this method is invoked 
		 * @param e The sync event passed to the method
		 * 
		 */		
		public function sharedObjectSyncHandler(e:SyncEvent):void{
			
		}
		
		public function setActivePresentation(e:PresentationEvent):void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.setActivePresentation",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						LogUtil.debug("Whiteboard::setActivePresentation() : " + e.presentationName); 
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				),//new Responder
				e.presentationName, e.numberOfSlides
			); //_netConnection.call
		}
		
		public function checkIsWhiteboardOn():void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.isWhiteboardEnabled",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						LogUtil.debug("Whiteboard::checkIsWhiteboardOn() : " + result as String); 
						if (result as Boolean) modifyEnabledCallback(true);
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				)//new Responder
				
			); //_netConnection.call
		}
		
		public function getPageHistory(e:PageEvent):void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.setActivePage",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						if ((result as int) != e.graphicObjs.length) {
							LogUtil.debug("Whiteboard: Need to retrieve shapes. Have " + e.graphicObjs.length + " on client, "
										  + (result as int) + " on server");
							LogUtil.debug("Whiteboard: Retrieving shapes on page" + e.pageNum);
							getHistory(); 
						} else{
							LogUtil.debug("Whiteboard: Shapes up to date, no need to update");
						}
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred: Whiteboard::DrawProxy::getPageHistory()"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				),//new Responder
				e.pageNum
			); //_netConnection.call
		}
		
		/**
		 * Sends a shape to the Shared Object on the red5 server, and then triggers an update across all clients
		 * @param shape The shape sent to the SharedObject
		 * 
		 */		
		public function sendShape(e:WhiteboardDrawEvent):void{
			var shape:DrawObject = e.message as DrawObject;
			LogUtil.debug("*** Sending shape");
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.sendShape",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						//LogUtil.debug("Whiteboard::sendShape() "); 
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				),//new Responder
				shape.getShapeArray(), shape.getType(), shape.getColor(), shape.getThickness(), 
				shape.getFill(), shape.getTransparency(), shape.getGraphicID(), shape.status
			); //_netConnection.call
		}
		
		/**
		 * Sends a TextObject to the Shared Object on the red5 server, and then triggers an update across all clients
		 * @param shape The shape sent to the SharedObject
		 * 
		 */		
		public function sendText(e:WhiteboardDrawEvent):void{
			var tobj:TextObject = e.message as TextObject;
			//LogUtil.error("Step 2: " + tobj.x + "," + tobj.y);
			//LogUtil.debug("*** Sending text");
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.sendText",// Remote function name
				new Responder(
					// On successful result
					function(result:Object):void { 

					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				),//new Responder
				tobj.text, tobj.textColor, tobj.bgColor, tobj.bgColorVisible,
				tobj.x, tobj.y, tobj.getGraphicID(), tobj.status
			); //_netConnection.call
		}
		/**
		 * Adds a shape to the ValueObject, then triggers an update event
		 * @param array The array representation of a shape
		 * 
		 */		
		public function addSegment(graphicType:String, array:Array, type:String, color:uint, thickness:uint, 
								   fill:Boolean, transparent:Boolean, id:String, status:String, recvdShapes:Boolean):void{
			LogUtil.debug("Rx add segment **** with ID of " + id + " " + type
			+ " and " + color + " " + thickness + " " + fill + " " + transparent);
			var d:DrawObject = drawFactory.makeDrawObject(type, array, color, thickness, fill, transparent);
			
			d.setGraphicID(id);
			d.status = status;
			
			var e:WhiteboardUpdate = new WhiteboardUpdate(WhiteboardUpdate.BOARD_UPDATED);
			e.data = d;
			e.recvdShapes = recvdShapes;
			dispatcher.dispatchEvent(e);
		}
		
		//convenience method
		public function addSegmentNormally(graphicType:String, array:Array, type:String, color:uint, thickness:uint, 
								   fill:Boolean, transparent:Boolean, id:String, status:String):void {
			
			addSegment(graphicType, array, type, color, thickness, 
				fill, transparent, id, status, false);
		}
		
		/**
		 * Adds a new TextObject to the Whiteboard overlay
		 * @param Params represent the data used to recreate the TextObject
		 * 
		 */		
		public function addText(graphicType:String, text:String, textColor:uint, bgColor:uint, bgColorVisible:Boolean,
								x:Number, y:Number, id:String, status:String, recvdShapes:Boolean):void {
			//LogUtil.error("Step 3(received): " + x + "," + y);
			LogUtil.debug("Rx add text **** with ID of " + id + " " + x + "," + y);
			var t:TextObject = textFactory.cloneTextObject(text, textColor, bgColor, bgColorVisible, x, y);
			t.setGraphicID(id);
			t.status = status;
			
			var e:WhiteboardUpdate = new WhiteboardUpdate(WhiteboardUpdate.BOARD_UPDATED);
			e.data = t;
			e.recvdShapes = recvdShapes;
			dispatcher.dispatchEvent(e);
		}
		
		//convenience method
		public function addTextNormally(graphicType:String, text:String, textColor:uint, bgColor:uint, bgColorVisible:Boolean,
										x:Number, y:Number, id:String, status:String):void {
			
			addText(graphicType, text, textColor, bgColor, bgColorVisible, 
						x, y, id, status, false);
		}
		
		/**
		 * Sends a call out to the red5 server to notify the clients that the board needs to be cleared 
		 * 
		 */		
		public function clearBoard():void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.clear",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						LogUtil.debug("Whiteboard::clearBoard()"); 
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				)//new Responder
				
			); //_netConnection.call
			
			//drawSO.send("clear");
		}
		
		/**
		 * Trigers the clear notification on a client 
		 * 
		 */		
		public function clear():void{
			dispatcher.dispatchEvent(new WhiteboardUpdate(WhiteboardUpdate.BOARD_CLEARED));
		}
		
		/**
		 * Sends a call out to the red5 server to notify the clients to undo a GraphicObject
		 * 
		 */		
		public function undoGraphic():void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.undo",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						LogUtil.debug("Whiteboard::undoGraphic()"); 
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				)//new Responder
				
			); //_netConnection.call
			
			//drawSO.send("undo");
		}
		
		/**
		 * Triggers the undo shape event on all clients 
		 * 
		 */		
		public function undo():void{
			dispatcher.dispatchEvent(new WhiteboardUpdate(WhiteboardUpdate.GRAPHIC_UNDONE));
		}
		
		public function modifyEnabled(e:WhiteboardPresenterEvent):void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.enableWhiteboard",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						LogUtil.debug("Whiteboard::modifyEnabled() : " + e.enabled); 
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				),//new Responder
				e.enabled
			); //_netConnection.call
		}
		
		public function modifyEnabledCallback(enabled:Boolean):void{
			var e:WhiteboardUpdate = new WhiteboardUpdate(WhiteboardUpdate.BOARD_ENABLED);
			e.boardEnabled = enabled;
			dispatcher.dispatchEvent(e);
		}
		
		private function getHistory():void{
			var nc:NetConnection = connection;
			nc.call(
				"whiteboard.getGraphicObjects",// Remote function name
				new Responder(
	        		// On successful result
					function(result:Object):void { 
						LogUtil.debug("Whiteboard::getHistory() : retrieving whiteboard history"); 
						receivedGraphicsHistory(result);
					},	
					// status - On error occurred
					function(status:Object):void { 
						LogUtil.error("Error occurred:"); 
						for (var x:Object in status) { 
							LogUtil.error(x + " : " + status[x]); 
						} 
					}
				)//new Responder
				
			); //_netConnection.call
		}
		
		private function receivedGraphicsHistory(result:Object):void{
			if (result == null) return;
			
			var graphicObjs:Array = result as Array;
			LogUtil.debug("Whiteboard::recievedShapesHistory() : recieved " + graphicObjs.length);
			
			for (var i:int=0; i < graphicObjs.length; i++) {
				var graphic:Array = graphicObjs[i] as Array;
				var graphicType:String = graphic[0] as String;
				if(graphicType == GraphicObject.TYPE_SHAPE) {
					var shapeArray:Array = graphic[1] as Array;
					var type:String = graphic[2] as String;
					var color:uint = graphic[3] as uint;
					var thickness:uint = graphic[4] as uint;
					var fill:Boolean = graphic[5] as Boolean;
					var transparent:Boolean = graphic[6] as Boolean;
					var id:String = graphic[7] as String;
					var status:String = graphic[8] as String;
					addSegment(graphicType, shapeArray, type, color, thickness, fill, transparent, id, status, true);
				} else if(graphicType == GraphicObject.TYPE_TEXT) {
					var text:String = graphic[1] as String;
					var textColor:uint = graphic[2] as uint;
					var bgColor:uint = graphic[3] as uint;
					var bgColorVisible:Boolean = graphic[4] as Boolean;
					var x:Number = graphic[5] as Number;
					var y:Number = graphic[6] as Number;
					var id_other:String = graphic[7] as String;
					var status_other:String = graphic[8] as String;
					addText(graphicType, text, textColor, bgColor, bgColorVisible, x, y, id_other, status_other, true);
				}
			}
		}	
	}
}