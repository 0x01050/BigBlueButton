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
package org.bigbluebutton.modules.layout.model
{
	import flash.events.EventDispatcher;

	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.core.EventBroadcaster;
	import org.bigbluebutton.core.model.Config;
	import org.bigbluebutton.modules.layout.events.LayoutsLoadedEvent;
	import org.bigbluebutton.modules.layout.model.LayoutDefinition;
	
	public class LayoutDefinitionFile extends EventDispatcher {
		private var _layouts:Array = new Array();
		
		public function get list():Array {
			return _layouts;
		}
		
		public function push(layoutDefinition:LayoutDefinition):void {
			_layouts.push(layoutDefinition);
		}
		
		public function getDefault():LayoutDefinition {
			for each (var value:LayoutDefinition in _layouts) {
				if (value.defaultLayout)
					return value;
			}
			return null;
		}
		
		public function toXml():XML {
			var xml:XML = <layouts/>;
			for each (var value:LayoutDefinition in _layouts) {
				xml.appendChild(value.toXml());
			}
			return xml;
		}
	}
}