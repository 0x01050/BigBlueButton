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
package org.bigbluebutton.main.model {
    import org.as3commons.logging.api.ILogger;
    import org.as3commons.logging.api.getClassLogger;
    import org.bigbluebutton.main.model.modules.ModulesDispatcher;

    public class PortTestProxy {

		private static const LOGGER:ILogger = getClassLogger(PortTestProxy);

        private var tunnel:Boolean;
        private var port:String;
        private var hostname:String;
        private var application:String;
        private var modulesDispatcher:ModulesDispatcher;
        private var portTest:PortTest;

        public function PortTestProxy(modulesDispatcher:ModulesDispatcher) {
            this.modulesDispatcher = modulesDispatcher;
        }

        public function connect( tunnel:Boolean, hostname:String = "", 
																port:String = "", application:String = "", 
																testTimeout:Number = 10000):void {
            this.tunnel = tunnel;
            portTest = new PortTest(tunnel, hostname, port, application, testTimeout);
            portTest.addConnectionSuccessListener(connectionListener);

            portTest.connect();
        }

        private function connectionListener(status:String, tunnel:Boolean, hostname:String, port:String, application:String):void {
            if (status == "SUCCESS") {
                modulesDispatcher.sendPortTestSuccessEvent(port, hostname, tunnel, application);
            } else {
                modulesDispatcher.sendPortTestFailedEvent(port, hostname, tunnel, application);
            }
        }
    }
}
