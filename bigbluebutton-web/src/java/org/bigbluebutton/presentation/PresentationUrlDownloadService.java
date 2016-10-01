package org.bigbluebutton.presentation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bigbluebutton.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresentationUrlDownloadService {
    private static Logger log = LoggerFactory
            .getLogger(PresentationUrlDownloadService.class);

    private final int maxRedirects = 5;
    private PageExtractor pageExtractor;
    private DocumentConversionService documentConversionService;
    private String presentationBaseURL;
    private String presentationDir;

    public void processUploadedPresentation(UploadedPresentation uploadedPres) {
        documentConversionService.processDocument(uploadedPres);
    }

    public void processUploadedFile(String meetingId, String presId,
            String filename, File presFile) {
        UploadedPresentation uploadedPres = new UploadedPresentation(meetingId,
                presId, filename, presentationBaseURL);
        uploadedPres.setUploadedFile(presFile);
        processUploadedPresentation(uploadedPres);
    }

    public void extractPage(String sourceMeetingId, String presentationId,
            Integer presentationSlide, String destinationMeetingId) {

        // Construct the source meeting path
        File sourceMeetingPath = new File(presentationDir + File.separator
                + sourceMeetingId + File.separator + sourceMeetingId
                + File.separator + presentationId);

        final String presentationFilter = presentationId;
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(presentationFilter);
            }
        };

        File[] children = sourceMeetingPath.listFiles(filter);

        if (children.length != 1) {
            log.error("Not matching file with prefix {} found at {}",
                    sourceMeetingId, sourceMeetingPath);
            return;
        } else {
            File sourcePresentationFile = children[0];
            String filenameExt = FilenameUtils
                    .getExtension(sourcePresentationFile.getName());
            String presId = generatePresentationId(presentationId);
            String newFilename = Util.createNewFilename(presId, filenameExt);

            File uploadDir = createPresentationDirectory(destinationMeetingId,
                    presentationDir, presId);
            String newFilePath = uploadDir.getAbsolutePath()
                    + File.separatorChar + newFilename;

            pageExtractor.extractPage(sourcePresentationFile, new File(
                    newFilePath), presentationSlide);

            File pres = new File(newFilePath);
            processUploadedFile(destinationMeetingId, presId, "default-"
                    + presentationSlide.toString() + "." + filenameExt, pres);
        }
    }

    public String generatePresentationId(String name) {
        long timestamp = System.currentTimeMillis();
        return DigestUtils.shaHex(name) + "-" + timestamp;
    }

    public File createPresentationDirectory(String meetingId,
            String presentationDir, String presentationId) {
        String meetingPath = presentationDir + File.separatorChar + meetingId
                + File.separatorChar + meetingId;
        String presPath = meetingPath + File.separatorChar + presentationId;
        File dir = new File(presPath);
        log.debug("Creating dir [{}]", presPath);
        if (dir.mkdirs()) {
            return dir;
        }
        return null;
    }

    private String followRedirect(String meetingId, String redirectUrl,
            int redirectCount, String origUrl) {

        if (redirectCount > maxRedirects) {
            log.error("Max redirect reached for meeting=[{}] with url=[{}]",
                    meetingId, origUrl);
            return null;
        }

        URL presUrl;
        try {
            presUrl = new URL(redirectUrl);
        } catch (MalformedURLException e) {
            log.error("Malformed url=[{}] for meeting=[{}]", redirectUrl,
                    meetingId);
            return null;
        }

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) presUrl.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = conn.getHeaderField("Location");
                    return followRedirect(meetingId, newUrl, redirectCount + 1,
                            origUrl);
                } else {
                    log.error(
                            "Invalid HTTP response=[{}] for url=[{}] with meeting[{}]",
                            status, redirectUrl, meetingId);
                    return null;
                }
            } else {
                return redirectUrl;
            }
        } catch (IOException e) {
            log.error("IOException for url=[{}] with meeting[{}]", redirectUrl,
                    meetingId);
            return null;
        }
    }

    public boolean savePresentation(final String meetingId,
            final String filename, final String urlString) {

        String finalUrl = followRedirect(meetingId, urlString, 0, urlString);

        if (finalUrl == null)
            return false;

        boolean success = false;
        GetMethod method = new GetMethod(finalUrl);
        HttpClient client = new HttpClient();
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                FileUtils.copyInputStreamToFile(
                        method.getResponseBodyAsStream(), new File(filename));
                log.info("Downloaded presentation at [{}]", finalUrl);
                success = true;
            }
        } catch (HttpException e) {
            log.error("HttpException while downloading presentation at [{}]",
                    finalUrl);
        } catch (IOException e) {
            log.error("IOException while downloading presentation at [{}]",
                    finalUrl);
        } finally {
            method.releaseConnection();
        }

        return success;
    }

    public void setPageExtractor(PageExtractor extractor) {
        this.pageExtractor = extractor;
    }

    public void setPresentationDir(String presDir) {
        presentationDir = presDir;
    }

    public void setPresentationBaseURL(String presentationBaseUrl) {
        presentationBaseURL = presentationBaseUrl;
    }

    public void setDocumentConversionService(
            DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

}
