package org.bigbluebutton.api.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;


public class Breakout {
  /**
   * <breakout parentMeetingId="f3ffe06acedf425565cc024c8ebe89a6552e8782-1489172964374" sequence="2" meetingId="f2041d123b6a4b994e7ad87ee9d348496a73472c-1489173065780"/>
   */

  @JacksonXmlProperty(isAttribute = true)
  private String parentMeetingId;

  @JacksonXmlProperty(isAttribute = true)
  private int sequence;

  @JacksonXmlProperty(isAttribute = true)
  private String meetingId;

}
