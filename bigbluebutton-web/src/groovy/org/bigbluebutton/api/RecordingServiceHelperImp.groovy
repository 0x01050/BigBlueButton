package org.bigbluebutton.api;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import java.io.File;
import java.util.ArrayList;

import org.bigbluebutton.api.domain.Recording;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordingServiceHelperImp implements RecordingServiceHelper {
	private static Logger log = LoggerFactory.getLogger(RecordingServiceHelperImp.class);
	/*
	<recording>
		<id>Demo Meeting-3243244</id>
		<state>available</state>
		<published>true</published>
		<start_time>3553545445</start_time>
		<end_time>5674545234</end_time>
		<playback>
			<format>simple</format>
			<link>http://server.com/simple/playback?recordingID=Demo Meeting-3243244</link>
		</playback>
		<meta>
			<title>Test Recording 2</title>
			<subject>English 232 session</subject>
			<description>Second  test recording</description>
			<creator>Omar Shammas</creator>
			<contributor>Blindside</contributor>
			<language>en_US</language>
		</meta>
	</recording>
	*/
	
	public void writeRecordingInfo(String path, Recording info) {
		def writer = new StringWriter()
		def builder = new groovy.xml.MarkupBuilder(writer)
		def metadataXml = builder.recording {
			builder.id(info.getId())
			builder.state(info.getState())
			builder.published(info.isPublished())
			builder.start_time(info.getStartTime())
			builder.end_time(info.getEndTime())
			builder.playback {
				builder.format(info.getPlaybackFormat())
				builder.link(info.getPlaybackLink())	
				builder.md5(info.getPlaybackMd5())	
			}
			builder.download {
				builder.format(info.getDownloadFormat())
				builder.link(info.getDownloadLink())	
				builder.md5(info.getDownloadMd5())	
			}
			Map<String,String> metainfo = info.getMetadata();
			builder.meta{
				metainfo.keySet().each { key ->
					builder."$key"(metainfo.get(key))
				}
			}
			 
		}
		
		def xmlEventFile = new File(path + File.separatorChar + "metadata.xml")
		xmlEventFile.write writer.toString()
	}
		
	public Recording getRecordingInfo(String id, String recordingDir, String playbackFormat) {
		String path = recordingDir + File.separatorChar + playbackFormat;		
		File dir = new File(path);
		if (dir.isDirectory()) {
			def recording = new XmlSlurper().parse(new File(path + File.separatorChar + id + File.separatorChar + "metadata.xml"));
			return getInfo(recording);
		}
		return null;
	}
	
	private Recording getInfo(GPathResult rec) {
		Recording r = new Recording();
		r.setId(rec.id.text());
		r.setState(rec.state.text());
		r.setPublished(Boolean.parseBoolean(rec.published.text()));
		r.setStartTime(rec.start_time.text());
		r.setEndTime(rec.end_time.text());
		r.setPlaybackFormat(rec.playback.format.text());
		r.setPlaybackLink(rec.playback.link.text());
		r.setPlaybackMd5(rec.playback.md5.text());	
		r.setDownloadFormat(rec.download.format.text());
		r.setDownloadLink(rec.download.link.text());
		r.setDownloadMd5(rec.download.md5.text());	
		Map<String, String> meta = new HashMap<String, String>();		
		rec.meta.children().each { anode ->
				log.debug("metadata: "+anode.name()+" "+anode.text())
				meta.put(anode.name().toString(), anode.text().toString());
		}
		r.setMetadata(meta);
		return r;
	}

}
