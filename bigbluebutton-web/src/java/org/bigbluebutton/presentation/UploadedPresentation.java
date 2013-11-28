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

package org.bigbluebutton.presentation;

import java.io.File;

public final class UploadedPresentation {
	private final String conference;
	private final String room;
	private final String name;
	private File uploadedFile;
	private String fileType = "unknown";
	private int numberOfPages = 0;
	private boolean lastStepSuccessful = false;
	private boolean isDownloadable = false;
	private String fileNameToDownload = "";
	
	public UploadedPresentation(String conference, String room, String name) {
		this.conference = conference;
		this.room = room;
		this.name = name;
		this.isDownloadable = false;
	}

	public boolean isDownloadable() {
		return isDownloadable;
	}

	public void setDownloadable() {
		this.isDownloadable = true;
	}

	public void setFileNameToDownload(String name) {
		this.fileNameToDownload = name;
	}

	public String getFileNameToDownload() {
		return fileNameToDownload;
	}

	public File getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(File uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public String getConference() {
		return conference;
	}

	public String getRoom() {
		return room;
	}

	public String getName() {
		return name;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public boolean isLastStepSuccessful() {
		return lastStepSuccessful;
	}

	public void setLastStepSuccessful(boolean lastStepSuccessful) {
		this.lastStepSuccessful = lastStepSuccessful;
	}
	
	
}
