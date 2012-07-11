/* BigBlueButton - http://www.bigbluebutton.org
 * 
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
 * Author: Richard Alam <ritzalam@gmail.com>
 * 		   DJP <DJP@architectes.org>
 * 
 * @version $Id: $
 */
package org.bigbluebutton.presentation;

import org.bigbluebutton.api.messaging.MessagingService;
import org.bigbluebutton.presentation.imp.ImageToSwfSlidesGenerationService;
import org.bigbluebutton.presentation.imp.OfficeToPdfConversionService;
import org.bigbluebutton.presentation.imp.PdfToSwfSlidesGenerationService;
import org.bigbluebutton.presentation.imp.PdfToTextFilesGenerationService;

public class DocumentConversionServiceImp implements DocumentConversionService {
	private MessagingService messagingService;
	private OfficeToPdfConversionService officeToPdfConversionService;
	private PdfToSwfSlidesGenerationService pdfToSwfSlidesGenerationService;
	private ImageToSwfSlidesGenerationService imageToSwfSlidesGenerationService;
	private PdfToTextFilesGenerationService pdfToTextFilesGenerationService;
	
	public void processDocument(UploadedPresentation pres) {
		SupportedDocumentFilter sdf = new SupportedDocumentFilter(messagingService);
		if (sdf.isSupported(pres)) {
			String fileType = pres.getFileType();
			
			if (SupportedFileTypes.isOfficeFile(fileType)) {
				officeToPdfConversionService.convertOfficeToPdf(pres);
				OfficeToPdfConversionSuccessFilter ocsf = new OfficeToPdfConversionSuccessFilter(messagingService);
				if (ocsf.didConversionSucceed(pres)) {
					// Successfully converted to pdf. Call the process again, this time it should be handled by 
					// the PDF conversion service.
					processDocument(pres);
				}
			} else if (SupportedFileTypes.isPdfFile(fileType)) {
				pdfToSwfSlidesGenerationService.generateSlides(pres);
				//accessibility
				pdfToTextFilesGenerationService.generateFiles(pres);
			} else if (SupportedFileTypes.isImageFile(fileType)) {
				imageToSwfSlidesGenerationService.generateSlides(pres);
			} else {
				
			}
						
		} else {
			// TODO: error log
		}
	}
	
	public void setMessagingService(MessagingService m) {
		messagingService = m;
	}
	
	public void setOfficeToPdfConversionService(OfficeToPdfConversionService s) {
		officeToPdfConversionService = s;
	}
	
	public void setPdfToSwfSlidesGenerationService(PdfToSwfSlidesGenerationService s) {
		pdfToSwfSlidesGenerationService = s; 
	}
	
	public void setPdfToSwfSlidesGenerationService(PdfToTextFilesGenerationService s) {
		pdfToTextFilesGenerationService = s; 
	}
	
	public void setImageToSwfSlidesGenerationService(ImageToSwfSlidesGenerationService s) {
		imageToSwfSlidesGenerationService = s;
	}
}
