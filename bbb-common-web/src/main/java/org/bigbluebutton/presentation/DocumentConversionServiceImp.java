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

import org.bigbluebutton.api.messaging.MessagingService;
import org.bigbluebutton.api2.IBbbWebApiGWApp;
import org.bigbluebutton.presentation.imp.ImageToSwfSlidesGenerationService;
import org.bigbluebutton.presentation.imp.OfficeToPdfConversionService;
import org.bigbluebutton.presentation.imp.PdfToSwfSlidesGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentConversionServiceImp implements DocumentConversionService {
  private static Logger log = LoggerFactory
      .getLogger(DocumentConversionServiceImp.class);

  private IBbbWebApiGWApp gw;
  private OfficeToPdfConversionService officeToPdfConversionService;
  private PdfToSwfSlidesGenerationService pdfToSwfSlidesGenerationService;
  private ImageToSwfSlidesGenerationService imageToSwfSlidesGenerationService;

  public void processDocument(UploadedPresentation pres) {
    SupportedDocumentFilter sdf = new SupportedDocumentFilter(gw);
    log.info("Start presentation conversion. meetingId=" + pres.getMeetingId()
        + " presId=" + pres.getId() + " name=" + pres.getName() + " current=" + pres.isCurrent());

    if (sdf.isSupported(pres)) {
      String fileType = pres.getFileType();

      if (SupportedFileTypes.isOfficeFile(fileType)) {
        pres = officeToPdfConversionService.convertOfficeToPdf(pres);
        OfficeToPdfConversionSuccessFilter ocsf = new OfficeToPdfConversionSuccessFilter(gw);
        if (ocsf.didConversionSucceed(pres)) {
          // Successfully converted to pdf. Call the process again, this time it
          // should be handled by
          // the PDF conversion service.
          processDocument(pres);
        }
      } else if (SupportedFileTypes.isPdfFile(fileType)) {
        pdfToSwfSlidesGenerationService.generateSlides(pres);
      } else if (SupportedFileTypes.isImageFile(fileType)) {
        imageToSwfSlidesGenerationService.generateSlides(pres);
      } else {

      }

    } else {
      // TODO: error log
    }

    log.info("End presentation conversion. meetingId=" + pres.getMeetingId()
        + " presId=" + pres.getId() + " name=" + pres.getName());

  }

  public void setBbbWebApiGWApp(IBbbWebApiGWApp m) {
    gw = m;
  }

  public void setOfficeToPdfConversionService(OfficeToPdfConversionService s) {
    officeToPdfConversionService = s;
  }

  public void setPdfToSwfSlidesGenerationService(
      PdfToSwfSlidesGenerationService s) {
    pdfToSwfSlidesGenerationService = s;
  }

  public void setImageToSwfSlidesGenerationService(
      ImageToSwfSlidesGenerationService s) {
    imageToSwfSlidesGenerationService = s;
  }
}
