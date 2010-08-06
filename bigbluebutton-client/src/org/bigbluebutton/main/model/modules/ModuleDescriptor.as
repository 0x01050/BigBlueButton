/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2009 by respective authors (see below). All rights reserved.
 * 
 * BigBlueButton is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 3 of the License, or (at your option) any later 
 * version. 
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with BigBlueButton; if not, If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id: $
 */
package org.bigbluebutton.main.model.modules
{
	import flash.events.Event;
	import flash.events.ProgressEvent;
	import flash.system.ApplicationDomain;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.core.IFlexModuleFactory;
	import mx.events.ModuleEvent;
	import mx.modules.ModuleLoader;
	import mx.utils.StringUtil;
	
	import org.bigbluebutton.common.IBigBlueButtonModule;
	import org.bigbluebutton.common.LogUtil;
	
	public class ModuleDescriptor
	{
		private var _attributes:Object;
		private var _loader:BigBlueButtonModuleLoader;
		private var _module:IBigBlueButtonModule;
		private var _loaded:Boolean = false;
		private var _started:Boolean = false;
		private var _connected:Boolean = false;
				
		private var callbackHandler:Function;
		
		public var unresolvedDependancies:ArrayCollection;
		public var resolved:Boolean = false;
		
		public function ModuleDescriptor(attributes:XML)
		{
			unresolvedDependancies = new ArrayCollection();
			_attributes = new Object();
			_loader = new BigBlueButtonModuleLoader();
			
			parseAttributes(attributes);			
		}
		
		public function setApplicationDomain(appDomain:ApplicationDomain):void{
			_loader.applicationDomain = appDomain;
		}

		public function addAttribute(attribute:String, value:Object):void {
			_attributes[attribute] = value;
		}
		
		public function getName():String{
			return _attributes["name"] as String;
		}
		
		public function getAttribute(name:String):Object {
			return _attributes[name];
		}
		
		public function get attributes():Object {
			return _attributes;
		}
		
		public function get module():IBigBlueButtonModule {
			return _module;
		}
		
		public function get loaded():Boolean {
			return _loaded;
		}
		
		public function set started(value:Boolean):void {
			_started = value;
		}
		
		private function parseAttributes(item:XML):void {
			var attNamesList:XMLList = item.@*;

			for (var i:int = 0; i < attNamesList.length(); i++)
			{ 
			    var attName:String = attNamesList[i].name();
			    var attValue:String = item.attribute(attName);
			    _attributes[attName] = attValue;
			}
			
			populateDependencies();
		}
		
		
		public function load(resultHandler:Function):void {
			callbackHandler = resultHandler;
			_loader.addEventListener("loading", onLoading);
			_loader.addEventListener("progress", onLoadProgress);
			_loader.addEventListener("ready", onReady);
			_loader.addEventListener("error", onErrorLoading);
			_loader.url = _attributes.url;
			LogUtil.debug("Loading " + _attributes.url);
			_loader.loadModule();
		}
		
		public function unload():void {
			_loader.url = "";
		}

		private function onReady(event:Event):void {
			LogUtil.debug(getName() + "finished loading");
			var modLoader:ModuleLoader = event.target as ModuleLoader;
			_module = modLoader.child as IBigBlueButtonModule;
			if (_module != null) {
				LogUtil.debug("Module " + _attributes.name + " has been loaded");
				_loaded = true;
				callbackHandler(ModuleManager.MODULE_LOAD_READY, _attributes.name);
			} else {
				LogUtil.error("Module loaded is null.");
			}
			
		}	

		private function onLoadProgress(e:ProgressEvent):void {
			if (getName() == "PresentModule") LogUtil.debug("PresentModule " + Math.round((e.bytesLoaded/e.bytesTotal) * 100) + " loaded");
			callbackHandler(ModuleManager.MODULE_LOAD_PROGRESS, 
					_attributes.name, Math.round((e.bytesLoaded/e.bytesTotal) * 100));
		}	
		
		private function onErrorLoading(e:ModuleEvent):void{
			LogUtil.error("Error loading " + getName() + e.errorText);
		}
		
		private function onLoading(e:Event):void{
			LogUtil.debug(getName() + " is loading");
		}
		
		public function useProtocol(protocol:String):void {
			_attributes.uri = _attributes.uri.replace(/rtmp:/gi, protocol + ":");
			LogUtil.debug(_attributes.name + " uri = " + _attributes.uri);
		}
		
		public function hasUnresolvedDependency(module:String):Boolean{
			return unresolvedDependancies.contains(module);
		}
		
		public function removeDependency(module:String):void{
			for (var i:int = 0; i<unresolvedDependancies.length; i++){
				if (unresolvedDependancies[i] == module) unresolvedDependancies.removeItemAt(i);
			}
		}
		
		private function populateDependencies():void{
			var dependString:String = _attributes["dependsOn"] as String;
			if (dependString == null) return;
			
			var trimmedString:String = StringUtil.trimArrayElements(dependString, ",");
			var dependancies:Array = trimmedString.split(",");
			
			for (var i:int = 0; i<dependancies.length; i++){
				unresolvedDependancies.addItem(dependancies[i]);
			}
		}
	}
}