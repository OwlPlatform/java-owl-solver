package com.owlplatform.solver.protocol.messages;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.owlplatform.common.util.NumericUtils;

/**
 * JUnit tests for the {@link HandshakeMessage} class.
 * 
 * @author Robert Moore
 * 
 */
public class HandshakeMessageTest {

  /**
   * An alternative protocol string for testing.
   */
  private static final String PROTO_STRING = "Fake Protocol";
  
  /**
   * An alternative protocol string for testing, but with different characters.
   */
  private static final String PROTO_STRING_2 = "Make Protocol";

  /**
   * An alternative reserved bits value.
   */
  private static final byte RESERVED = (byte) 0xD3;

  /**
   * An alternative version value.
   */
  private static final byte VERSION = 55;

  private static final String INIT_TO_STRING = "Solver Handshake: 0, , 0x00, 0x00";
  
  /**
   * Expected toString() value for the default handshake message.
   */
  private static final String DEF_TO_STRING = "Solver Handshake: "
      + HandshakeMessage.PROTOCOL_STRING_LENGTH + ", "
      + HandshakeMessage.PROTOCOL_STRING + ", "
      + NumericUtils.toHexString(HandshakeMessage.PROTOCOL_VERSION) + ", "
      + NumericUtils.toHexString(HandshakeMessage.PROTOCOL_RESERVED_BITS);

  /**
   * Expected toString() value for an alternative handshake.
   */
  private static final String ALT_TO_STRING = "Solver Handshake: "
      + PROTO_STRING.length() + ", " + PROTO_STRING + ", " + NumericUtils.toHexString(VERSION) + ", "
      + NumericUtils.toHexString(RESERVED);

  /**
   * An empty handshake message.
   */
  private HandshakeMessage h1;
  /**
   * An empty handshake message.
   */
  private HandshakeMessage h2;

  /**
   * Creates {@code h1} and {@code h2}, empty handshake messages.
   */
  @Before
  public void createHS() {
    h1 = new HandshakeMessage();
    h2 = new HandshakeMessage();
  }

  /**
   * Test to ensure that equal handshake messages have equal hash values.
   */
  @Test
  public void testHashCode() {
    Assert.assertEquals(h1.hashCode(), h2.hashCode());

    h1.setVersionNumber(VERSION);

    Assert.assertFalse(h1.hashCode() == h2.hashCode());

    h1 = HandshakeMessage.getDefaultMessage();
    h2 = HandshakeMessage.getDefaultMessage();

    Assert.assertEquals(h1.hashCode(), h2.hashCode());
  }

  /**
   * Ensure the the default message uses the correct values.
   * 
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testGetDefaultMessage() throws UnsupportedEncodingException {
    HandshakeMessage h3 = HandshakeMessage.getDefaultMessage();

    Assert.assertEquals(HandshakeMessage.PROTOCOL_STRING,
        h3.getProtocolString());
    Assert.assertEquals(HandshakeMessage.PROTOCOL_STRING_LENGTH, h3
        .getProtocolString().getBytes("US-ASCII").length);
    Assert.assertEquals(HandshakeMessage.PROTOCOL_RESERVED_BITS,
        h3.getReservedBits());
    Assert.assertEquals(HandshakeMessage.PROTOCOL_VERSION,
        h3.getVersionNumber());

  }

  @Test
  public void testGetAndSet() throws UnsupportedEncodingException {
    h1.setProtocolString(PROTO_STRING);
    Assert.assertEquals(PROTO_STRING, h1.getProtocolString());
    Assert.assertEquals(PROTO_STRING.getBytes("US-ASCII").length,
        h1.getStringLength());

    h1.setReservedBits(RESERVED);
    Assert.assertEquals(RESERVED, h1.getReservedBits());

    h1.setVersionNumber(VERSION);
    Assert.assertEquals(VERSION, h1.getVersionNumber());
  }

  @Test
  public void testToString() {
    Assert.assertEquals(INIT_TO_STRING,h1.toString());
    
    
    h1 = HandshakeMessage.getDefaultMessage();
    Assert.assertEquals(DEF_TO_STRING, h1.toString());

    h1.setProtocolString(PROTO_STRING);
    h1.setVersionNumber(VERSION);
    h1.setReservedBits(RESERVED);

    Assert.assertEquals(ALT_TO_STRING, h1.toString());
  }

  @Test
  public void testEquals() {
    Assert.assertTrue(h1.equals(h2));
    Assert.assertTrue(h2.equals(h1));
    
    Assert.assertTrue(h1.equals((Object)h2));
    Assert.assertTrue(h2.equals((Object)h1));

    h1 = HandshakeMessage.getDefaultMessage();
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));
    
    h2 = HandshakeMessage.getDefaultMessage();
    h1.setVersionNumber(VERSION);
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));
    
    h1.setVersionNumber(HandshakeMessage.PROTOCOL_VERSION);
    h1.setReservedBits(RESERVED);
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));
    
    h1.setReservedBits(HandshakeMessage.PROTOCOL_RESERVED_BITS);
    h1.setProtocolString(null);
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));
    
    h1.setStringLength(HandshakeMessage.PROTOCOL_STRING_LENGTH);
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));
    
    h1.setProtocolString(HandshakeMessage.PROTOCOL_STRING);
    Assert.assertTrue(h1.equals(h2));
    Assert.assertTrue(h2.equals(h1));
    
    h1.setProtocolString(PROTO_STRING);
    h2.setProtocolString(PROTO_STRING_2);
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));

    h2.setProtocolString(PROTO_STRING);
    h2.setStringLength(1);
    Assert.assertFalse(h1.equals(h2));
    Assert.assertFalse(h2.equals(h1));
    
    Assert.assertFalse(h1.equals(Integer.valueOf(0)));
  }

}
