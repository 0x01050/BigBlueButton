<recording>
  <head>
    <title>This is a sample document</title>
  </head>
  <event timestamp="1296681157242" module="PARTICIPANT" name="ParticipantJoinEvent">
    <status>{raiseHand=false, hasStream=false, presenter=false}</status>
    <userId>1</userId>
    <role>MODERATOR</role>
  </event>
  <event timestamp="1296681159083" module="PARTICIPANT" name="ParticipantStatusChangeEvent">
    <status>presenter</status>
    <userId>1</userId>
    <value>true</value>
  </event>
  <event timestamp="1296681159414" module="PRESENTATION" name="AssignPresenterEvent">
    <name>FRED</name>
    <userid>1</userid>
    <assignedBy>1</assignedBy>
  </event>
  <event timestamp="1296681167181" module="VOICE" name="ParticipantJoinedEvent">
    <bridge>70919</bridge>
    <locked>false</locked>
    <callername>FRED</callername>
    <muted>true</muted>
    <talking>false</talking>
    <participant>116</participant>
    <callernumber>FRED</callernumber>
  </event>
  <event timestamp="1296681167689" module="VOICE" name="StartRecordingEvent">
    <bridge>70919</bridge>
    <filename>/var/freeswitch/meetings/1b199e88-7df7-4842-a5f1-0e84b781c5c8-20110202-041247.wav</filename>
    <recordingTimestamp>1296681167678312</recordingTimestamp>
  </event>
  <event timestamp="1296681176232" module="VOICE" name="ParticipantMutedEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <muted>false</muted>
  </event>
  <event timestamp="1296681182485" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681184880" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681190019" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681191185" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681192118" module="CHAT" name="PublicChatEvent">
    <color>0</color>
    <locale>en</locale>
    <message>hello</message>
    <sender>FRED</sender>
  </event>
  <event timestamp="1296681197131" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>6</numberOfPages>
    <presentationName>aSimple-Layout</presentationName>
    <pagesCompleted>1</pagesCompleted>
  </event>
  <event timestamp="1296681197346" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>6</numberOfPages>
    <presentationName>aSimple-Layout</presentationName>
    <pagesCompleted>2</pagesCompleted>
  </event>
  <event timestamp="1296681197540" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>aSimple-Layout</presentationName>
    <numberOfPages>6</numberOfPages>
    <pagesCompleted>3</pagesCompleted>
  </event>
  <event timestamp="1296681197726" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>aSimple-Layout</presentationName>
    <numberOfPages>6</numberOfPages>
    <pagesCompleted>4</pagesCompleted>
  </event>
  <event timestamp="1296681198064" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>aSimple-Layout</presentationName>
    <numberOfPages>6</numberOfPages>
    <pagesCompleted>5</pagesCompleted>
  </event>
  <event timestamp="1296681198167" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>aSimple-Layout</presentationName>
    <numberOfPages>6</numberOfPages>
    <pagesCompleted>6</pagesCompleted>
  </event>
  <event timestamp="1296681199630" module="CHAT" name="PublicChatEvent">
    <sender>FRED</sender>
    <locale>en</locale>
    <color>0</color>
    <message>how are you?</message>
  </event>
  <event timestamp="1296681206076" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681208060" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681208273" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681211499" module="PRESENTATION" name="ConversionCompletedEvent">
    <presentationName>aSimple-Layout</presentationName>
    <slidesInfo>&lt;uploadedpresentation&gt;
  &lt;conference id='1b199e88-7df7-4842-a5f1-0e84b781c5c8' room='1b199e88-7df7-4842-a5f1-0e84b781c5c8'&gt;
    &lt;presentation name='aSimple-Layout'&gt;
      &lt;slides count='6'&gt;
        &lt;slide number='1' name='slide/1' thumb='thumbnail/1' /&gt;
        &lt;slide number='2' name='slide/2' thumb='thumbnail/2' /&gt;
        &lt;slide number='3' name='slide/3' thumb='thumbnail/3' /&gt;
        &lt;slide number='4' name='slide/4' thumb='thumbnail/4' /&gt;
        &lt;slide number='5' name='slide/5' thumb='thumbnail/5' /&gt;
        &lt;slide number='6' name='slide/6' thumb='thumbnail/6' /&gt;
      &lt;/slides&gt;
    &lt;/presentation&gt;
  &lt;/conference&gt;
&lt;/uploadedpresentation&gt;</slidesInfo>
  </event>
  <event timestamp="1296681211723" module="PRESENTATION" name="SharePresentationEvent">
    <presentationName>aSimple-Layout</presentationName>
    <share>true</share>
  </event>
  <event timestamp="1296681211763" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.0</xOffset>
  </event>
  <event timestamp="1296681212874" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681214035" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681214698" module="PRESENTATION" name="GotoSlideEvent">
    <slide>0</slide>
  </event>
  <event timestamp="1296681215613" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681217191" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681218136" module="PRESENTATION" name="GotoSlideEvent">
    <slide>1</slide>
  </event>
  <event timestamp="1296681218433" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681219233" module="PRESENTATION" name="GotoSlideEvent">
    <slide>2</slide>
  </event>
  <event timestamp="1296681220616" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681220730" module="PRESENTATION" name="GotoSlideEvent">
    <slide>3</slide>
  </event>
  <event timestamp="1296681222417" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0013297872340425532</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.0</xOffset>
  </event>
  <event timestamp="1296681222420" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0026595744680851063</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>9.98003992015968E-4</xOffset>
  </event>
  <event timestamp="1296681222425" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.003989361702127659</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.001996007984031936</xOffset>
  </event>
  <event timestamp="1296681222445" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.013297872340425532</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.005988023952095809</xOffset>
  </event>
  <event timestamp="1296681222449" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.015957446808510637</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.006986027944111776</xOffset>
  </event>
  <event timestamp="1296681222458" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0199468085106383</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.008982035928143712</xOffset>
  </event>
  <event timestamp="1296681222465" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.022606382978723406</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.00998003992015968</xOffset>
  </event>
  <event timestamp="1296681222476" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.030585106382978722</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.013972055888223553</xOffset>
  </event>
  <event timestamp="1296681222482" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.041223404255319146</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.01996007984031936</xOffset>
  </event>
  <event timestamp="1296681222505" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.061170212765957445</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.03293413173652695</xOffset>
  </event>
  <event timestamp="1296681222506" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.06515957446808511</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.036926147704590816</xOffset>
  </event>
  <event timestamp="1296681222520" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.07047872340425532</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.04091816367265469</xOffset>
  </event>
  <event timestamp="1296681222538" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.07446808510638298</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.043912175648702596</xOffset>
  </event>
  <event timestamp="1296681222545" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.07446808510638298</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.04491017964071856</xOffset>
  </event>
  <event timestamp="1296681223116" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.07180851063829788</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.04491017964071856</xOffset>
  </event>
  <event timestamp="1296681223119" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.07047872340425532</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.043912175648702596</xOffset>
  </event>
  <event timestamp="1296681223121" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0678191489361702</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.04291417165668663</xOffset>
  </event>
  <event timestamp="1296681223148" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0199468085106383</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.014970059880239521</xOffset>
  </event>
  <event timestamp="1296681223176" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.009308510638297872</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.003992015968063872</xOffset>
  </event>
  <event timestamp="1296681223179" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.007978723404255319</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.0</xOffset>
  </event>
  <event timestamp="1296681223212" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0026595744680851063</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>-0.00499001996007984</xOffset>
  </event>
  <event timestamp="1296681223220" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>-0.006986027944111776</xOffset>
  </event>
  <event timestamp="1296681223227" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>-0.0013297872340425532</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>-0.007984031936127744</xOffset>
  </event>
  <event timestamp="1296681223258" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>-0.005319148936170213</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>-0.00998003992015968</xOffset>
  </event>
  <event timestamp="1296681223284" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>-0.006648936170212766</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>-0.00998003992015968</xOffset>
  </event>
  <event timestamp="1296681223297" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681223302" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>-0.009308510638297872</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>-0.011976047904191617</xOffset>
  </event>
  <event timestamp="1296681223993" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0499999999999998</widthRatio>
    <yOffset>-0.015197568389057751</yOffset>
    <heightRatio>1.05</heightRatio>
    <xOffset>-0.015207679878338562</xOffset>
  </event>
  <event timestamp="1296681224098" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.1</widthRatio>
    <yOffset>-0.01934235976789168</yOffset>
    <heightRatio>1.1</heightRatio>
    <xOffset>-0.018145527127563055</xOffset>
  </event>
  <event timestamp="1296681224115" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.15</widthRatio>
    <yOffset>-0.023126734505087884</yOffset>
    <heightRatio>1.15</heightRatio>
    <xOffset>-0.020827909398594118</xOffset>
  </event>
  <event timestamp="1296681224373" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681224560" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.2000000000000002</widthRatio>
    <yOffset>-0.026595744680851064</yOffset>
    <heightRatio>1.2</heightRatio>
    <xOffset>-0.02328675981370592</xOffset>
  </event>
  <event timestamp="1296681224605" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.25</widthRatio>
    <yOffset>-0.029787234042553193</yOffset>
    <heightRatio>1.25</heightRatio>
    <xOffset>-0.02554890219560878</xOffset>
  </event>
  <event timestamp="1296681224657" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.2999999999999998</widthRatio>
    <yOffset>-0.03273322422258593</yOffset>
    <heightRatio>1.3</heightRatio>
    <xOffset>-0.02763703362505758</xOffset>
  </event>
  <event timestamp="1296681225394" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681226433" module="PRESENTATION" name="GotoSlideEvent">
    <slide>4</slide>
  </event>
  <event timestamp="1296681228700" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681230100" module="VOICE" name="ParticipantLeftEvent">
    <bridge>70919</bridge>
    <participant>116</participant>
  </event>
  <event timestamp="1296681230166" module="VOICE" name="StopRecordingEvent">
    <bridge>70919</bridge>
    <filename>/var/freeswitch/meetings/1b199e88-7df7-4842-a5f1-0e84b781c5c8-20110202-041247.wav</filename>
    <recordingTimestamp>1296681230143916</recordingTimestamp>
  </event>
  <event timestamp="1296681244331" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>1</pagesCompleted>
  </event>
  <event timestamp="1296681244696" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>flight-school</presentationName>
    <numberOfPages>12</numberOfPages>
    <pagesCompleted>2</pagesCompleted>
  </event>
  <event timestamp="1296681245098" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>flight-school</presentationName>
    <numberOfPages>12</numberOfPages>
    <pagesCompleted>3</pagesCompleted>
  </event>
  <event timestamp="1296681245479" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>4</pagesCompleted>
  </event>
  <event timestamp="1296681245822" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>5</pagesCompleted>
  </event>
  <event timestamp="1296681246191" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>6</pagesCompleted>
  </event>
  <event timestamp="1296681246823" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>7</pagesCompleted>
  </event>
  <event timestamp="1296681247628" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>flight-school</presentationName>
    <numberOfPages>12</numberOfPages>
    <pagesCompleted>8</pagesCompleted>
  </event>
  <event timestamp="1296681247803" module="CHAT" name="PublicChatEvent">
    <sender>FRED</sender>
    <locale>en</locale>
    <color>0</color>
    <message>hi fred</message>
  </event>
  <event timestamp="1296681248384" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>9</pagesCompleted>
  </event>
  <event timestamp="1296681250338" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>flight-school</presentationName>
    <numberOfPages>12</numberOfPages>
    <pagesCompleted>10</pagesCompleted>
  </event>
  <event timestamp="1296681250737" module="PRESENTATION" name="GenerateSlideEvent">
    <numberOfPages>12</numberOfPages>
    <presentationName>flight-school</presentationName>
    <pagesCompleted>11</pagesCompleted>
  </event>
  <event timestamp="1296681250967" module="PRESENTATION" name="GenerateSlideEvent">
    <presentationName>flight-school</presentationName>
    <numberOfPages>12</numberOfPages>
    <pagesCompleted>12</pagesCompleted>
  </event>
  <event timestamp="1296681255454" module="VOICE" name="ParticipantJoinedEvent">
    <bridge>70919</bridge>
    <locked>false</locked>
    <callername>FRED</callername>
    <muted>true</muted>
    <talking>false</talking>
    <participant>118</participant>
    <callernumber>FRED</callernumber>
  </event>
  <event timestamp="1296681255586" module="VOICE" name="StartRecordingEvent">
    <bridge>70919</bridge>
    <filename>/var/freeswitch/meetings/1b199e88-7df7-4842-a5f1-0e84b781c5c8-20110202-041415.wav</filename>
    <recordingTimestamp>1296681255581210</recordingTimestamp>
  </event>
  <event timestamp="1296681258849" module="VOICE" name="ParticipantMutedEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <muted>false</muted>
  </event>
  <event timestamp="1296681260696" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681262734" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681263061" module="VOICE" name="ParticipantMutedEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <muted>true</muted>
  </event>
  <event timestamp="1296681263990" module="VOICE" name="ParticipantMutedEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <muted>false</muted>
  </event>
  <event timestamp="1296681264113" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681265283" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681289270" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681290494" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681294442" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681295598" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681295746" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681297296" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681297779" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681300758" module="PRESENTATION" name="ConversionCompletedEvent">
    <presentationName>flight-school</presentationName>
    <slidesInfo>&lt;uploadedpresentation&gt;
  &lt;conference id='1b199e88-7df7-4842-a5f1-0e84b781c5c8' room='1b199e88-7df7-4842-a5f1-0e84b781c5c8'&gt;
    &lt;presentation name='flight-school'&gt;
      &lt;slides count='12'&gt;
        &lt;slide number='1' name='slide/1' thumb='thumbnail/1' /&gt;
        &lt;slide number='2' name='slide/2' thumb='thumbnail/2' /&gt;
        &lt;slide number='3' name='slide/3' thumb='thumbnail/3' /&gt;
        &lt;slide number='4' name='slide/4' thumb='thumbnail/4' /&gt;
        &lt;slide number='5' name='slide/5' thumb='thumbnail/5' /&gt;
        &lt;slide number='6' name='slide/6' thumb='thumbnail/6' /&gt;
        &lt;slide number='7' name='slide/7' thumb='thumbnail/7' /&gt;
        &lt;slide number='8' name='slide/8' thumb='thumbnail/8' /&gt;
        &lt;slide number='9' name='slide/9' thumb='thumbnail/9' /&gt;
        &lt;slide number='10' name='slide/10' thumb='thumbnail/10' /&gt;
        &lt;slide number='11' name='slide/11' thumb='thumbnail/11' /&gt;
        &lt;slide number='12' name='slide/12' thumb='thumbnail/12' /&gt;
      &lt;/slides&gt;
    &lt;/presentation&gt;
  &lt;/conference&gt;
&lt;/uploadedpresentation&gt;</slidesInfo>
  </event>
  <event timestamp="1296681300900" module="PRESENTATION" name="SharePresentationEvent">
    <presentationName>flight-school</presentationName>
    <share>true</share>
  </event>
  <event timestamp="1296681300902" module="PRESENTATION" name="ResizeAndMoveSlideEvent">
    <widthRatio>1.0</widthRatio>
    <yOffset>0.0</yOffset>
    <heightRatio>1.0</heightRatio>
    <xOffset>0.0</xOffset>
  </event>
  <event timestamp="1296681302074" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681303058" module="PRESENTATION" name="GotoSlideEvent">
    <slide>5</slide>
  </event>
  <event timestamp="1296681303926" module="PRESENTATION" name="GotoSlideEvent">
    <slide>0</slide>
  </event>
  <event timestamp="1296681304176" module="PRESENTATION" name="GotoSlideEvent">
    <slide>1</slide>
  </event>
  <event timestamp="1296681304856" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681305786" module="PRESENTATION" name="GotoSlideEvent">
    <slide>2</slide>
  </event>
  <event timestamp="1296681306737" module="PRESENTATION" name="GotoSlideEvent">
    <slide>3</slide>
  </event>
  <event timestamp="1296681307553" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>false</talking>
  </event>
  <event timestamp="1296681307929" module="PRESENTATION" name="GotoSlideEvent">
    <slide>4</slide>
  </event>
  <event timestamp="1296681308869" module="PRESENTATION" name="GotoSlideEvent">
    <slide>5</slide>
  </event>
  <event timestamp="1296681310115" module="VOICE" name="ParticipantTalkingEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
    <talking>true</talking>
  </event>
  <event timestamp="1296681315494" module="VOICE" name="ParticipantLeftEvent">
    <bridge>70919</bridge>
    <participant>118</participant>
  </event>
  <event timestamp="1296681315499" module="VOICE" name="StopRecordingEvent">
    <bridge>70919</bridge>
    <filename>/var/freeswitch/meetings/1b199e88-7df7-4842-a5f1-0e84b781c5c8-20110202-041415.wav</filename>
    <recordingTimestamp>1296681315487066</recordingTimestamp>
  </event>
  <event timestamp="1296681317164" module="PARTICIPANT" name="ParticipantLeftEvent">
    <userId>1</userId>
  </event>
  <event timestamp="1296681317181" module="PARTICIPANT" name="EndAndKickAllEvent"/>
</recording>
