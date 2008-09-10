﻿package org.red5.samples.publisher.events{	/**	 * RED5 Open Source Flash Server - http://www.osflash.org/red5	 *	 * Copyright (c) 2006-2008 by respective authors (see below). All rights reserved.	 *	 * This library is free software; you can redistribute it and/or modify it under the	 * terms of the GNU Lesser General Public License as published by the Free Software	 * Foundation; either version 2.1 of the License, or (at your option) any later	 * version.	 *	 * This library is distributed in the hope that it will be useful, but WITHOUT ANY	 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A	 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.	 *	 * You should have received a copy of the GNU Lesser General Public License along	 * with this library; if not, write to the Free Software Foundation, Inc.,	 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA	 */	 	import com.adobe.cairngorm.control.CairngormEvent;		import flash.display.Stage;		import org.red5.samples.publisher.control.DashboardController;		/**	 * @copy org.red5.samples.publisher.command.StartFullScreenCommand	 * @author Thijs Triemstra	 */		public class StartFullScreenEvent extends CairngormEvent 	{			/**		* 		*/					public var stage : Stage;				/**		 * 		 * @param stage		 */					public function StartFullScreenEvent( stage : Stage ) 		{			super( DashboardController.EVENT_START_FULLSCREEN );			this.stage = stage;		}	}}