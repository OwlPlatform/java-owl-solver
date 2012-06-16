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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link Transmitter}.
 * 
 * @author Robert Moore
 * 
 */
public class TransmitterTest {

  /**
   * Device ID as an integer.
   */
  public static final int DEV123I = 123;

  /**
   * Device ID as a long.
   */
  public static final long DEV123L = 123l;

  /**
   * Device ID as a byte[] of length 1.
   */
  public static final byte[] DEV123B = new byte[] { 123 };

  /**
   * Device ID as a byte[] of length 16.
   */
  public static final byte[] DEV123FB = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 123 };

  /**
   * Device ID as a byte[] of length 18.
   */
  public static final byte[] DEV123OFB = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 123 };

  /**
   * A mask value that matches only an exact transmitter.
   */
  public static final byte[] FULL_MASK = new byte[] { (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

  /**
   * Tests the various construtors of the {@code Transmitter} class.
   */
  @Test
  public void testConstructors() {
    Transmitter t1 = new Transmitter();
    Transmitter t2 = new Transmitter(DEV123B);
    t1.setBaseId(DEV123FB);
    t1.setMask(TransmitterTest.FULL_MASK);
    Assert.assertEquals(t1, t2);
    Assert.assertEquals(t2, t1);

    t1 = new Transmitter(DEV123I);
    Assert.assertEquals(t1, t2);
    Assert.assertEquals(t2, t1);

    t1 = new Transmitter(DEV123L);
    Assert.assertEquals(t1, t2);
    Assert.assertEquals(t2, t1);

    t1 = new Transmitter(DEV123B);
    Assert.assertEquals(t1, t2);
    Assert.assertEquals(t2, t1);

    t1 = new Transmitter(DEV123FB);
    Assert.assertEquals(t1, t2);
    Assert.assertEquals(t2, t1);

    t1 = new Transmitter(DEV123OFB);
    Assert.assertEquals(t1, t2);
    Assert.assertEquals(t2, t1);
  }

  /**
   * A test case to ensure that null byte[] values passed to the constructor
   * will throw an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullArrayConstructor() {
    new Transmitter(null);
  }

  /**
   * A test case to ensure that null byte[] values passed to
   * {@code setBaseId(byte[])} will throw an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullSetBaseId() {
    Transmitter t = new Transmitter();
    t.setBaseId(null);
  }

  /**
   * A test case to ensure that null byte[] values passed to
   * {@code setMask(byte[])} will throw an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullSetMask() {
    Transmitter t = new Transmitter();
    t.setMask(null);
  }

  /**
   * Test case to check that setting the base ID to a byte[] shorter than 16
   * bytes throws an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void setInvalidLengthBaseId() {
    Transmitter t = new Transmitter();
    t.setBaseId(DEV123B);
  }

  /**
   * Test case to check that setting the mask to a byte[] shorter than 16 bytes
   * throws an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void setInvalidLengthMask() {
    Transmitter t = new Transmitter();
    t.setMask(DEV123B);
  }

  /**
   * Test the setters and getters.
   */
  @Test
  public void testSetGet() {
    Transmitter t = new Transmitter();
    t.setBaseId(DEV123FB);
    Assert.assertEquals(DEV123FB, t.getBaseId());

    t.setBaseId(FULL_MASK);
    Assert.assertEquals(FULL_MASK, t.getBaseId());

    t.setMask(DEV123FB);
    Assert.assertEquals(DEV123FB, t.getMask());

    t.setMask(FULL_MASK);
    Assert.assertEquals(FULL_MASK, t.getMask());
  }

  /**
   * Check that the toString() method is consistent.
   */
  @Test
  public void testToString() {
    Transmitter t1 = new Transmitter(DEV123I);
    Transmitter t2 = new Transmitter(DEV123FB);

    Assert.assertEquals(t1.toString(), t2.toString());
  }

  /**
   * Check that the equals method works correctly.
   */
  @Test
  public void testEquality() {
    Transmitter t1 = new Transmitter(DEV123L);
    Transmitter t2 = new Transmitter(DEV123B);

    Assert.assertTrue(t1.equals(t2));
    Assert.assertTrue(t2.equals(t1));
    
    Assert.assertTrue(t1.equals((Object)t2));
    Assert.assertTrue(t2.equals((Object)t1));

    Assert.assertTrue(t1.hashCode() == t2.hashCode());

    t2 = new Transmitter(FULL_MASK);
    Assert.assertFalse(t1.equals(t2));
    Assert.assertFalse(t2.equals(t1));

    Assert.assertFalse(t1.equals(Integer.valueOf(0)));
  }
}
