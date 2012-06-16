/*
 * Owl Platform Solver-Aggregator Library for Java
 * Copyright (C) 2012 Robert Moore and the Owl Platform
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

import java.util.Arrays;

import com.owlplatform.common.util.NumericUtils;

/**
 * Represents a transmitter within a {@code SubscriptionMessage} rule, specified
 * by a base address and a mask.
 * 
 * @author Robert Moore II
 * 
 */
public class Transmitter {

  /**
   * The length, in octets, of the transmitter identifier.
   */
  public static final int TRANSMITTER_ID_SIZE = 16;

  /**
   * Mask value that will match only the specified device ID.
   */
  private static final byte[] MASK_EXACT = new byte[] { (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

  /**
   * The base address of the transmitter(s) referenced in some subscription
   * rule.
   */
  private byte[] baseId;

  /**
   * The bitwise mask of the transmitter(s) referenced in some subscription
   * rule.
   */
  private byte[] mask;

  /**
   * Creates a new {@code Transmitter} object with a null baseId and mask value.
   */
  public Transmitter() {
    super();
  }

  /**
   * <p>
   * Convenience constructor that matches exactly the specific device identified
   * by {code deviceId}. If {@code deviceId} is not exactly 16 bytes long, it
   * will be extended or truncated to be precisely 16 bytes.
   * </p>
   * 
   * <p>
   * More precisely, if {@code deviceId} is {@code len} bytes and {@code len} is
   * shorter than 16 bytes, it will be copied into the lowest bytes of the
   * resulting Transmitter's {@code baseId} value. maintaining the original byte
   * ordering. If {@code len} is longer than 16 bytes, then the HIGHEST 16 bytes
   * (<i>i.e.,</i> deviceId[len-16,len-1]) will be copied into the {@code baseId}
   * value.
   * 
   * @param deviceId
   *          the exact device ID to match.
   */
  public Transmitter(byte[] deviceId) {
    if (deviceId == null) {
      throw new IllegalArgumentException("Transmitter base ID cannot be null.");
    }

    this.baseId = new byte[16];
    int length = deviceId.length;
    /*
     * length <= 16 baseId[16-length,15] gets deviceId[0,length]
     * 
     * length > 16 baseId[0,15] gets deviceId[length-16,length]
     */
    System.arraycopy(deviceId, length <= 16 ? 0 : length - 16, this.baseId,
        length <= 16 ? 16 - length : 0, length <= 16 ? length : 16);
    this.mask = Arrays.copyOf(MASK_EXACT, 16);
  }

  /**
   * Returns the base address for this transmitter.
   * 
   * @return the base address for this transmitter.
   */
  public byte[] getBaseId() {
    return this.baseId;
  }

  /**
   * Convenience constructor that matches exactly the specific device identified
   * by the 64-bit deviceId value.
   * 
   * @param deviceId
   *          the device ID to match
   */
  public Transmitter(long deviceId) {
    this.baseId = new byte[16];
    long devCopy = deviceId;
    int byteIndex = 15;
    while (devCopy != 0) {
      this.baseId[byteIndex] = (byte) (devCopy & 0xFF);
      --byteIndex;
      devCopy >>= 8;
    }

    this.mask = Arrays.copyOf(MASK_EXACT, 16);

  }

  /**
   * Convenience constructor that matches exactly the specific device identified
   * by the 32-bit deviceId value.
   * 
   * @param deviceId
   *          the device ID to match
   */
  public Transmitter(int deviceId) {
    this(deviceId & 0xFFFFFFFFL);
  }

  /**
   * Sets the base address for this transmitter.
   * 
   * @param baseId
   *          the base address for this transmitter.
   */
  public void setBaseId(byte[] baseId) {
    if (baseId == null) {
      throw new IllegalArgumentException("Transmitter base ID cannot be null.");
    }
    if (baseId.length != TRANSMITTER_ID_SIZE) {
      throw new IllegalArgumentException(String.format(
          "Transmitter base ID must be %d bytes long.",
          Integer.valueOf(TRANSMITTER_ID_SIZE)));
    }
    this.baseId = baseId;
  }

  /**
   * Returns the bit mask for this transmitter.
   * 
   * @return the bit mask for this transmitter.
   */
  public byte[] getMask() {
    return this.mask;
  }

  /**
   * Sets the bit mask for this transmitter.
   * 
   * @param mask
   *          the bit mask for this transmitter.
   */
  public void setMask(byte[] mask) {
    if (mask == null) {
      throw new IllegalArgumentException("Transmitter mask cannot be null.");
    }
    if (mask.length != TRANSMITTER_ID_SIZE) {
      throw new IllegalArgumentException(String.format(
          "Transmitter mask must be %d bytes long.",
          Integer.valueOf(TRANSMITTER_ID_SIZE)));
    }
    this.mask = mask;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Transmitter (")
        .append(NumericUtils.toHexString(this.getBaseId())).append(" | ")
        .append(NumericUtils.toHexString(this.getMask())).append(')');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Transmitter) {
      return this.equals((Transmitter) o);
    }
    return super.equals(o);
  }

  /**
   * Determines if this {@code Transmitter} is equal to another based on a
   * simple comparison of their baseId and mask values. If this Transmitter's
   * baseId is equal to the other's baseId, and this mask is equal to the
   * other's mask, then the two are equal.
   * 
   * @param o
   *          another {@code Transmitter}.
   * @return {@code true} if {@code o} has the equal baseId and mask values as
   *         this {@code Transmitter}, else {@code false}.
   */
  public boolean equals(Transmitter o) {
    return Arrays.equals(this.baseId, o.baseId)
        && Arrays.equals(this.mask, o.mask);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.baseId) ^ Arrays.hashCode(this.mask);
  }
}
