/*
 * BigBlueButton - http://www.bigbluebutton.org
 * 
 * Copyright (c) 2008-2010 by respective authors (see below). All rights reserved.
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
 * @author Jeremy Thomerson <jthomerson@genericconf.com>
 * $Id: $
 */
package org.bigbluebutton.deskshare.client.frame;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

public class CaptureRegionFrame {
	private static final long serialVersionUID = 1L;

	private Button btnStartStop;
	private CaptureRegionListener client;
	private boolean capturing = false;
	private WindowlessFrame frame;
	
	public CaptureRegionFrame(CaptureRegionListener client, int borderWidth) {
		frame = new WindowlessFrame(borderWidth);
		this.client = client;
		frame.setCaptureRegionListener(client);
		frame.setToolbar(createToolbar());
	}
	
	public void setHeight(int h) {
		frame.setHeight(h);
	}
	
	public void setWidth(int w) {
		frame.setWidth(w);
	}
	
	public void setLocation(int x, int y) {
		frame.setLocation(x, y);
	}
	
	public void setVisible(boolean visible) {
		frame.setVisible(visible);	
	}
	
	
	private JPanel createToolbar() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		capturing = false;
		btnStartStop = new Button("Start Capture");
		btnStartStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (capturing) {
					capturing = false;
					btnStartStop.setLabel("Start Capture");
					stopCapture();
				} else {
					capturing = true;
					btnStartStop.setLabel("Stop Capture");
					startCapture();
				}					
			}
		});
		panel.add(btnStartStop);
		return panel;
	}
	
	private void startCapture() {
		client.onStartCapture();
	}
	
	private void stopCapture() {
		client.onStopCapture();
	}
}
