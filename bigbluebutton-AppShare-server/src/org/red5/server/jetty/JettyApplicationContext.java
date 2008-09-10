package org.red5.server.jetty;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 *
 * Copyright (c) 2006-2007 by respective authors (see below). All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

import org.mortbay.jetty.webapp.WebAppContext;
import org.red5.server.api.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Class that wraps a Jetty webapp context.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Joachim Bauch (jojo@struktur.de)
 */
public class JettyApplicationContext implements IApplicationContext {

    /** Logger. */
	protected static Logger log = LoggerFactory.getLogger(JettyApplicationContext.class);

	/** Store a reference to the Jetty webapp context. */
	private WebAppContext context;
	
	/**
	 * Wrap the passed Jetty webapp context.
	 * 
	 * @param context the context
	 */
	protected JettyApplicationContext(WebAppContext context) {
		this.context = context;
	}
	
	/** {@inheritDoc} */
	public void stop() {
		if (!context.isRunning()) {
			if (log.isDebugEnabled()) {
				log.debug("Application context already stopped.");
			}
			return;
		}
		
		try {
			context.stop();
		} catch (Exception e) {
			log.error("Could not stop application context.", e);
		}
	}

}
