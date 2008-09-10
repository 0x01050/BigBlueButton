package org.red5.server.api;

// TODO: Auto-generated Javadoc
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

/**
 * The bandwidth configure for connection that has an extra
 * property "upstreamBandwidth" which is not used by Bandwidth
 * Control Framework in Red5.
 * 
 * @author Steven Gong (steven.gong@gmail.com)
 * @version $Id$
 */
public interface IConnectionBWConfig extends IBandwidthConfigure {
	
	/**
	 * Set the upstream bandwidth to be notified to the client.
	 * Upstream is the data that is sent from the client to the server.
	 * 
	 * @param bw Bandwidth
	 */
	void setUpstreamBandwidth(long bw);

	/**
	 * Get the upstream bandwidth to be notified to the client.
	 * Upstream is the data that is sent from the client to the server.
	 * 
	 * @return      Upstream (from client to server) bandwidth configuration
	 */
	long getUpstreamBandwidth();

	/**
	 * Getter for downstream bandwidth.
	 * 
	 * @return Downstream bandwidth, from server to client
	 */
    long getDownstreamBandwidth();
}
