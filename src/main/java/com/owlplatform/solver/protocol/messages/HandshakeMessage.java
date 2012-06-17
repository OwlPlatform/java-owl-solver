/*
 * GRAIL Real Time Localization System
 * Copyright (C) 2011 Rutgers University and Robert Moore
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.owlplatform.solver.protocol.messages;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.common.util.NumericUtils;

/**
 * Represents a handshake message between a Solver and an Aggregator as defined
 * in the GRAIL RTLS v3 Aggregator-Solver protocol.
 * 
 * @author Robert Moore II
 * 
 */
public class HandshakeMessage {

  /**
   * Logging facility.
   */
  private static final Logger log = LoggerFactory
      .getLogger(HandshakeMessage.class);

  /**
   * The length of the handshake message in octets.
   */
  public static final int MESSAGE_LENGTH = 27;

  /**
   * The protocol string used in the handshake message.
   */
  public static final String PROTOCOL_STRING = "GRAIL solver protocol";

  /**
   * The length, in octets, of the protocol string sent in the handshake
   * message.
   */
  public static final int PROTOCOL_STRING_LENGTH = 21;

  /**
   * The protocol version number.
   */
  public static final byte PROTOCOL_VERSION = 0;

  /**
   * Bitfield indicating which extensions (if any) are in use by the sender.
   */
  public static final byte PROTOCOL_RESERVED_BITS = 0;

  /**
   * Generates a basic handshake message according to the most recent protocol
   * definition.
   * 
   * @return a basic handshake message.
   */
  public static HandshakeMessage getDefaultMessage() {
    HandshakeMessage message = new HandshakeMessage();
    message.setProtocolString(HandshakeMessage.PROTOCOL_STRING);
    message.setReservedBits(HandshakeMessage.PROTOCOL_RESERVED_BITS);
    message.setStringLength(HandshakeMessage.PROTOCOL_STRING_LENGTH);
    message.setVersionNumber(HandshakeMessage.PROTOCOL_VERSION);
    return message;
  }

  /**
   * The length of the protocol string, in octets.
   */
  protected int stringLength;

  /**
   * The protocol string for this handshake message.
   */
  protected String protocolString;

  /**
   * The version number for this handshake message.
   */
  protected byte versionNumber;

  /**
   * The bitfield indicating which extensions are in use (if any) for this
   * handshake message.
   */
  protected byte reservedBits;

  /**
   * Retrieves the length of the protocol string (in octets) for this handshake
   * message.
   * 
   * @return the length fo the protocol string in octets.
   */
  public int getStringLength() {
    return this.stringLength;
  }

  /**
   * Sets the length of the protocol string (in octets) for this handshake
   * message.
   * 
   * @param stringLength
   */
  public void setStringLength(int stringLength) {
    this.stringLength = stringLength;
  }

  /**
   * Retrieves the protocol string for this handshake message.
   * 
   * @return the protocol string for this handshake message.
   */
  public String getProtocolString() {
    return this.protocolString;
  }

  /**
   * Sets the protocol string for this handshake message.
   * 
   * @param protocolString
   *          the protocol string for this handshake message.
   */
  public void setProtocolString(String protocolString) {
    this.protocolString = protocolString;
    if (protocolString != null) {
      try {
        this.stringLength = protocolString.getBytes("US-ASCII").length;
      } catch (UnsupportedEncodingException uee) {
        this.stringLength = 0;
        log.error("Couldn't encode to US-ASCII.", uee);
      }
    } else {
      this.stringLength = 0;
    }
  }

  /**
   * Retrieves the version number for this handshake message.
   * 
   * @return the version number for this handshake message.
   */
  public byte getVersionNumber() {
    return this.versionNumber;
  }

  /**
   * Sets the version number for this handshake message.
   * 
   * @param versionNumber
   *          the version number for this handshake message.
   */
  public void setVersionNumber(byte versionNumber) {
    this.versionNumber = versionNumber;
  }

  /**
   * Retrieves the reserved bitfield for this handshake message.
   * 
   * @return the reserved bitfield for this handshake message.
   */
  public byte getReservedBits() {
    return this.reservedBits;
  }

  /**
   * Sets the reserved bitfield for this handshake message.
   * 
   * @param reservedBits
   *          the reserved bitfield for this handshake message.
   */
  public void setReservedBits(byte reservedBits) {
    this.reservedBits = reservedBits;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Solver Handshake: ")
        .append(this.getStringLength())
        .append(", ")
        .append(
            this.getProtocolString() == null ? "" : this.getProtocolString())
        .append(", ").append(NumericUtils.toHexString(this.getVersionNumber()))
        .append(", ").append(NumericUtils.toHexString(this.getReservedBits()));
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof HandshakeMessage) {
      return this.equals((HandshakeMessage) o);
    }
    return super.equals(o);
  }

  /**
   * Compares this {@code HandshakeMessage} to antoher based on version number,
   * reserved bits, and protocol string.
   * 
   * @param message
   *          another {@code HandshakeMessage}
   * @return {@code true} if this object and {@code message} are equal, else
   *         {@code false}.
   */
  public boolean equals(HandshakeMessage message) {
    if (this.versionNumber != message.versionNumber)
      return false;
    if (this.reservedBits != message.reservedBits)
      return false;
    if (this.stringLength != message.stringLength)
      return false;
    if (this.protocolString != null) {
      if (message.protocolString == null) {
        return false;
      }
      try {

        byte[] myStringBytes = this.protocolString.getBytes("ASCII");
        byte[] yourStringBytes = message.protocolString.getBytes("ASCII");
        if (!Arrays.equals(myStringBytes, yourStringBytes))
          return false;
      } catch (UnsupportedEncodingException uee) {
        log.error("Could not decode ASCII characters.");
        return false;
      }
    } else if (message.protocolString != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {

    int hash = Arrays.hashCode(new byte[] { this.versionNumber,
        this.reservedBits });
    if (this.protocolString != null) {
      hash ^= this.stringLength;
      hash ^= this.protocolString.hashCode();
    }
    return hash;
  }
}
