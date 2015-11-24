/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 * 
 * Copyright (c) 2015 BigBlueButton Inc. and by respective authors (see below).
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
package org.bigbluebutton.presentation.handlers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;

public abstract class AbstractPageConverterHandler extends
    NuAbstractProcessHandler {

  private static Logger log = LoggerFactory
      .getLogger(AbstractPageConverterHandler.class);

  private NuProcess nuProcess;
  private int exitCode;
  final private StringBuilder stdoutBuilder = new StringBuilder();
  final private StringBuilder stderrBuilder = new StringBuilder();

  @Override
  public void onPreStart(NuProcess nuProcess) {
    this.nuProcess = nuProcess;
  }

  @Override
  public void onStart(NuProcess nuProcess) {
    super.onStart(nuProcess);
  }

  @Override
  public void onStdout(ByteBuffer buffer, boolean closed) {
    if (buffer != null) {
      CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
      stdoutBuilder.append(charBuffer);
    }
    log.debug("Conversion success\n" + stdoutBuilder.toString());
  }

  @Override
  public void onStderr(ByteBuffer buffer, boolean closed) {
    if (buffer != null) {
      CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
      stderrBuilder.append(charBuffer);
    }
    log.debug("Conversion error\n" + stderrBuilder.toString());
  }

  @Override
  public void onExit(int statusCode) {
    exitCode = statusCode;
    log.debug("Conversion process exited with status=" + exitCode);
  }

  protected Boolean stdoutContains(String value) {
    return stdoutBuilder.indexOf(value) > -1;
  }

  public abstract Boolean isConversionSuccessfull();
}
