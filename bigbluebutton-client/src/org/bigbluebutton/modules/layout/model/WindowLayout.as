/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 *
 * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
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
 * Author: Felipe Cecagno <felipe@mconf.org>
 */
package org.bigbluebutton.modules.layout.model {

	public class WindowLayout {

		import flexlib.mdi.containers.MDICanvas;
		import flexlib.mdi.containers.MDIWindow;

		import mx.effects.Move;
		import mx.effects.Resize;

		import flash.display.DisplayObject;
		import flash.display.DisplayObjectContainer;
		import flash.utils.Dictionary;
		import flash.utils.getQualifiedClassName;
		import org.bigbluebutton.common.LogUtil;

		[Bindable] public var name:String;
		[Bindable] public var width:Number;
		[Bindable] public var height:Number;
		[Bindable] public var x:Number;
		[Bindable] public var y:Number;
		[Bindable] public var minimized:Boolean = false;
		[Bindable] public var maximized:Boolean = false;

		static private var resizers:Dictionary = new Dictionary();
		static private var movers:Dictionary = new Dictionary();
		
		static private var EVENT_DURATION:int = 750;

		public function load(vxml:XML):void {
			if (vxml != null) {
				if (vxml.@name != undefined) {
					name = vxml.@name.toString();
				}
				if (vxml.@width != undefined) {
					width = Number(vxml.@width);
				}
				if (vxml.@height != undefined) {
					height = Number(vxml.@height);
				}
				if (vxml.@x != undefined) {
					x = Number(vxml.@x);
				}
				if (vxml.@y != undefined) {
					y = Number(vxml.@y);
				}
				if (vxml.@minimized != undefined) {
					minimized = (vxml.@minimized.toString().toUpperCase() == "TRUE") ? true : false;
				}
				if (vxml.@maximized != undefined) {
					maximized = (vxml.@maximized.toString().toUpperCase() == "TRUE") ? true : false;
				}
			}
		}
		
		static public function getLayout(canvas:MDICanvas, window:MDIWindow):WindowLayout {
			var layout:WindowLayout = new WindowLayout();
			layout.name = getType(window);
			layout.width = window.width / canvas.width;
			layout.height = window.height / canvas.height;
			layout.x = window.x / canvas.width;
			layout.y = window.y / canvas.height;
			layout.minimized = window.minimized;
			layout.maximized = window.maximized;
			return layout;
		}
		
		static public function setLayout(canvas:MDICanvas, window:MDIWindow, layout:WindowLayout):void {
			if (layout == null) return;
			layout.applyToWindow(canvas, window);
		}
		
		public function applyToWindow(canvas:MDICanvas, window:MDIWindow):void {
			if (this.minimized || this.maximized) {
				this.minimized? window.minimize(): window.maximize();
				return; 
			} else
				window.restore();
			
			var newWidth:int = this.width * canvas.width;
			var newHeight:int = this.height * canvas.height;
			var newX:int = this.x * canvas.width;
			var newY:int = this.y * canvas.height;
			
			if (window.width != newWidth || window.height != newHeight) {
				var resizer:Resize = getResizer(window);
				resizer.end();
				resizer.widthTo = newWidth;
				resizer.heightTo = newHeight;
				resizer.play();
			}
			
			if (window.x != newX || window.y != newY) {
				var mover:Move = getMover(window);
				mover.end();
				mover.xTo = newX;
				mover.yTo = newY;
				mover.play();
			}
		}
		
		static private function getResizer(p:DisplayObjectContainer):Resize {
			var effect:Resize = resizers[p];
			if (effect == null) {
				effect = new Resize();
				effect.target = p;
				effect.duration = EVENT_DURATION;
				resizers[p] = effect;
			}
			return effect;
		}
		
		static private function getMover(p:DisplayObjectContainer):Move {
			var effect:Move = movers[p];
			if (effect == null) {
				effect = new Move();
				effect.target = p;
				effect.duration = EVENT_DURATION;
				movers[p] = effect;
			}
			return effect;
		}

		static public function getType(obj:Object):String {
			var qualifiedClass:String = String(getQualifiedClassName(obj));
			var pattern:RegExp = /(\w+)::(\w+)/g;
			if (qualifiedClass.match(pattern)) {
				return qualifiedClass.split("::")[1];
			} else { 
				return String(Object).substr(String(Object).lastIndexOf(".") + 1).match(/[a-zA-Z]+/).join();
			}
		}
		
		public function toXml():String {
			return "<window name=\"" + name + "\"" +
				(minimized? " minimized=\"true\"":
				(maximized? " maximized=\"true\"":
				" width=\"" + width + "\"" +
				" height=\"" + height + "\"" +
				" x=\"" + x + "\"" +
				" y=\"" + y + "\"")) +
				" />";
		}  
	}
}
