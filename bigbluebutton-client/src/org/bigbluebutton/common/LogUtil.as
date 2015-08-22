/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 *
 * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
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
package org.bigbluebutton.common
{
	import org.as3commons.logging.api.LOGGER_FACTORY;
	import org.as3commons.logging.setup.LevelTargetSetup;
	import org.as3commons.logging.setup.LogSetupLevel;
	import org.as3commons.logging.setup.target.IFormattingLogTarget;
	import org.as3commons.logging.setup.target.TraceTarget;
	import org.bigbluebutton.core.BBB;
	import org.bigbluebutton.util.logging.JSNLogTarget;
	import org.bigbluebutton.util.logging.LogWindowTarget;
	import org.bigbluebutton.util.logging.ServerLogTarget;

	/**
	 * The logging can be configured in config.xml. The <logging> tag can be configured like the following
	 *
	 * <logging enabled="true" target="trace" level="info" uri="http://HOST"/>
	 * - enabled : true or false
	 * - target  : + trace     : write logs using trace function
	 *             + logwindow : write logs in LogWindow view
	 *             + server    : send logs to the server
	 *             + jsnlog    : uses jsnlog javascript library (experimental)
	 * - level   : none|fatal_only|fatal|error_only|error|warn_only|warn|info_only|info|debug_only|debug|all
	 *             for more information about "level" please check the org.as3commons.logging.setup.LogSetupLevel class
	 * - uri     : HOST will contain the URL of the logging server
	 */
	public class LogUtil
	{
		public static const TRACE:String="trace";
		public static const LOG_WINDOW:String="logwindow";
		public static const SERVER:String="server";
		public static const JSNLOG:String="jsnlog";

		private static const DEFAULT_FORMAT:String="{dateUTC} {time} :: {name} :: [{logLevel}] {message}";

		private static var loggingEnabled:Boolean;
		private static var logLevel:String="info";
		private static var loggingTargetName:String="trace";

		/**
		 * Initialises logging from the application configuration.
		 */
		public static function initLogging(force:Boolean=false):void
		{
			var logTarget:IFormattingLogTarget;

			if (force)
			{
				logTarget=new TraceTarget();
			}
			else
			{
				var lxml:XML=BBB.initConfigManager().config.logging;
				logLevel=String(lxml.@level).toUpperCase();
				if (lxml.@enabled != undefined)
				{
					loggingEnabled=(lxml.@enabled.toString().toUpperCase() == "TRUE") ? true : false;
				}
				if (lxml.@target != undefined)
				{
					loggingTargetName=lxml.@target.toString().toLowerCase();
				}
				if (loggingEnabled)
				{
					switch (loggingTargetName)
					{
						case TRACE:
							logTarget=new TraceTarget();
							break;
						case LOG_WINDOW:
							logTarget=new LogWindowTarget();
							break;
						case SERVER:
							logTarget=new ServerLogTarget(String(lxml.@uri));
							break;
						case JSNLOG:
							logTarget=new JSNLogTarget();
							break;
						default:
							// no logging target set						
							break;
					}
				}
			}

			if (logTarget)
			{
				logTarget.format=DEFAULT_FORMAT;
				LOGGER_FACTORY.setup=new LevelTargetSetup(logTarget, LogSetupLevel[logLevel.toUpperCase()]);
			}
			else
			{
				disableLogging();
			}
		}

		/**
		 * Disables logging across the applicatio.
		 */
		public static function disableLogging():void
		{
			LOGGER_FACTORY.setup=null;
		}
	}
}
