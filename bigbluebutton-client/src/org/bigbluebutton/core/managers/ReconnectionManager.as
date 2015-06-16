/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2015 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
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
package org.bigbluebutton.core.managers
{
  import com.asfusion.mate.events.Dispatcher;

  import flash.display.DisplayObject;
  import flash.events.TimerEvent;
  import flash.utils.Dictionary;
  import flash.utils.Timer;

  import mx.controls.Alert;
  import mx.core.FlexGlobals;
  import mx.core.IFlexDisplayObject;
  import mx.events.CloseEvent;
  import mx.managers.PopUpManager;
  import mx.utils.ObjectUtil;

  import org.bigbluebutton.main.events.BBBEvent;
  import org.bigbluebutton.main.events.ClientStatusEvent;
  import org.bigbluebutton.main.model.users.AutoReconnect;
  import org.bigbluebutton.main.views.ReconnectionPopup;

  public class ReconnectionManager
  {
    public static const LOG:String = "ReconnectionManager - ";

    public static const BIGBLUEBUTTON_CONNECTION:String = "BIGBLUEBUTTON_CONNECTION";
    public static const SIP_CONNECTION:String = "SIP_CONNECTION";
    public static const VIDEO_CONNECTION:String = "VIDEO_CONNECTION";
    public static const DESKSHARE_CONNECTION:String = "DESKSHARE_CONNECTION";

    private var _connections:Dictionary = new Dictionary();
    private var _reconnectTimer:Timer = new Timer(10000, 1);
    private var _dispatcher:Dispatcher = new Dispatcher();
    private var _popup:IFlexDisplayObject = null;

    public function ReconnectionManager() {
      _reconnectTimer.addEventListener(TimerEvent.TIMER_COMPLETE, reconnect);
    }
    
    private function reconnect(e:TimerEvent = null):void {
      if (_connections.hasOwnProperty(BIGBLUEBUTTON_CONNECTION)) {
        reconnectHelper(BIGBLUEBUTTON_CONNECTION);
      } else {
        for (var type:String in _connections) {
          reconnectHelper(type);
        }
      }
    }

    private function reconnectHelper(type:String):void {
      var obj:Object = _connections[type];
      obj.reconnect = new AutoReconnect();
      obj.reconnect.onDisconnect(obj.callback, obj.callbackParameters);
    }

    public function onDisconnected(type:String, callback:Function, parameters:Array):void {
      trace(LOG + "onDisconnected, type=" + type + ", parameters=" + parameters.toString());

      var obj:Object = new Object();
      obj.callback = callback;
      obj.callbackParameters = parameters;
      _connections[type] = obj;

      if (!_reconnectTimer.running) {
        _popup = PopUpManager.createPopUp(FlexGlobals.topLevelApplication as DisplayObject, ReconnectionPopup, true);
        PopUpManager.centerPopUp(_popup);

        _reconnectTimer.reset();
        _reconnectTimer.start();
      }
    }

    public function onConnectionAttemptFailed(type:String):void {
      trace(LOG + "onConnectionAttemptFailed, type=" + type);
      if (_connections.hasOwnProperty(type)) {
        _connections[type].reconnect.onConnectionAttemptFailed();
      }
    }

    private function get connectionDictEmpty():Boolean {
      for (var key:Object in _connections) {
        return false;
      }
      return true;
    }
    
    private function dispatchReconnectionSucceededEvent(type:String):void {
      var map:Object = {
        BIGBLUEBUTTON_CONNECTION: BBBEvent.RECONNECT_BIGBLUEBUTTON_SUCCEEDED_EVENT,
        SIP_CONNECTION: BBBEvent.RECONNECT_SIP_SUCCEEDED_EVENT,
        VIDEO_CONNECTION: BBBEvent.RECONNECT_VIDEO_SUCCEEDED_EVENT,
        DESKSHARE_CONNECTION: BBBEvent.RECONNECT_DESKSHARE_SUCCEEDED_EVENT
      };
      
      if (map.hasOwnProperty(type)) {
        trace(LOG + "dispatchReconnectionSucceededEvent, type=" + type);
        _dispatcher.dispatchEvent(new BBBEvent(map[type]));
      } else {
        trace(LOG + "dispatchReconnectionSucceededEvent, couldn't find a map value for type " + type);
      }
    }

    public function onConnectionAttemptSucceeded(type:String):void {
      trace(LOG + "onConnectionAttemptSucceeded, type=" + type);
      dispatchReconnectionSucceededEvent(type);

      delete _connections[type];
      if (type == BIGBLUEBUTTON_CONNECTION) {
        reconnect();
      }

      if (connectionDictEmpty) {
        _dispatcher.dispatchEvent(new ClientStatusEvent(ClientStatusEvent.SUCCESS_MESSAGE_EVENT, 
          "Connection reestablished", 
          "Connection has been reestablished successfully"));

        if (_popup != null) {
          PopUpManager.removePopUp(_popup);
          _popup = null;
        }
      }
    }

    public function onCancelReconnection():void {
      // Not sure if we need this to be clean
      for (var type:Object in _connections) delete _connections[type];
      if (_popup != null) {
        PopUpManager.removePopUp(_popup);
        _popup = null;
      }
    }
  }
}
