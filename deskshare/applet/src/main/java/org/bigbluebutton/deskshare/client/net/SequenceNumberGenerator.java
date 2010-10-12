package org.bigbluebutton.deskshare.client.net;

import java.util.concurrent.atomic.AtomicInteger;


public class SequenceNumberGenerator {

	private final AtomicInteger sequenceNum;
	
	public SequenceNumberGenerator() {
		sequenceNum = new AtomicInteger();
	}
	
	public int getNext() {
		return sequenceNum.incrementAndGet();
	}
}
