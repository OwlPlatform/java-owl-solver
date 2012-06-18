package com.owlplatform.solver.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.protocol.messages.Transmitter;

/**
 * JUnit test class for {@link SubscriptionRequestRule}.
 * 
 * @author Robert Moore
 * 
 */
public class SubscriptionRequestRuleTest {

  /**
   * The default update interval for generic subscription messages.
   */
  private static final long INTERVAL_DEFAULT = 0l;

  /**
   * The default physical layer identifier for generic subscription messages.
   */
  private static final byte PHY_DEFAULT = 0;

  /**
   * The default number of transmitters in a generic subscription message.
   */
  private static final int NUM_TXERS_DEFAULT = 0;

  /**
   * An update interval of 1 second.
   */
  private static final long INTERVAL_1SEC = 1000;

  /**
   * An update interval of 7.5 seconds.
   */
  private static final long INTERVAL_7_5SEC = 7500;

  /**
   * A test transmitter for exact matches
   */
  private static final Transmitter TX_EXACT1 = new Transmitter(1234);

  /**
   * A test transmitter for exact matches.
   */
  private static final Transmitter TX_EXACT2 = new Transmitter(1234567);

  /**
   * A test transmitter for exact matches.
   */
  private static final Transmitter TX_EXACT3 = new Transmitter(new byte[] {
      (byte) 0xAB, (byte) 0xCD, (byte) 0xDE, (byte) 0xF0 });

  /**
   * Transmitter array for testing.
   */
  private static final Transmitter[] TX_ARR_A1 = new Transmitter[] { TX_EXACT1,
      TX_EXACT2 };
  /**
   * Transmitter array for testing.
   */
  private static final Transmitter[] TX_ARR_A2 = new Transmitter[] { TX_EXACT1,
      TX_EXACT2 };
  /**
   * Transmitter array for testing.
   */
  private static final Transmitter[] TX_ARR_B1 = new Transmitter[] { TX_EXACT2,
      TX_EXACT1, TX_EXACT3 };
  /**
   * Transmitter array for testing.
   */
  private static final Transmitter[] TX_ARR_B2 = new Transmitter[] { TX_EXACT3,
      TX_EXACT1, TX_EXACT2 };
  
  /**
   * Transmitter array for testing.
   */
  private static final Transmitter[] TX_ARR_B3 = new Transmitter[] { TX_EXACT2,
      TX_EXACT1, TX_EXACT2 };

  private static final Collection<Transmitter> TX_COLL_A1 = new LinkedList<Transmitter>();
  
  static {
    TX_COLL_A1.add(TX_EXACT1);
    TX_COLL_A1.add(TX_EXACT2);
  }
  
  /**
   * Tests the setter and getter methods.
   */
  @Test
  public void testSetAndGet() {
    SubscriptionRequestRule rule = new SubscriptionRequestRule();
    rule.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_ALL);
    Assert.assertEquals(SampleMessage.PHYSICAL_LAYER_ALL,
        rule.getPhysicalLayer());
    rule.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WIFI);
    Assert.assertEquals(SampleMessage.PHYSICAL_LAYER_WIFI,
        rule.getPhysicalLayer());

    rule.setUpdateInterval(INTERVAL_1SEC);
    Assert.assertEquals(INTERVAL_1SEC, rule.getUpdateInterval());
    rule.setUpdateInterval(INTERVAL_7_5SEC);
    Assert.assertEquals(INTERVAL_7_5SEC, rule.getUpdateInterval());

    rule.setTransmitters(TX_ARR_A1);
    Assert.assertTrue(Arrays.equals(TX_ARR_A2, rule.getTransmitters()));
    Assert.assertEquals(TX_ARR_A2.length, rule.getNumTransmitters());

  }

  /**
   * Make sure generic rule generation does what's expected.
   */
  @Test
  public void testGenericRule() {
    SubscriptionRequestRule generic = SubscriptionRequestRule
        .generateGenericRule();
    Assert.assertEquals(0l, generic.getUpdateInterval());
    Assert.assertEquals(SampleMessage.PHYSICAL_LAYER_ALL,
        generic.getPhysicalLayer());
    Assert.assertEquals(0, generic.getNumTransmitters());
    Assert.assertNull(generic.getTransmitters());
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    SubscriptionRequestRule s1 = new SubscriptionRequestRule();
    SubscriptionRequestRule s2 = new SubscriptionRequestRule();

    s1.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WINS);
    s2.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WINS);

    s1.setUpdateInterval(INTERVAL_7_5SEC);
    s2.setUpdateInterval(INTERVAL_7_5SEC);

    s1.setTransmitters(TX_ARR_A1);
    s2.setTransmitters(TX_ARR_A2);

    Assert.assertEquals(s1.toString(), s2.toString());
  }

  /**
   * Tests the equals() methods.
   */
  @Test
  public void testEquals() {
    SubscriptionRequestRule s1 = new SubscriptionRequestRule();
    SubscriptionRequestRule s2 = new SubscriptionRequestRule();

    s1.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WINS);
    s2.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WINS);

    s1.setUpdateInterval(INTERVAL_7_5SEC);
    s2.setUpdateInterval(INTERVAL_7_5SEC);

    s1.setTransmitters(TX_ARR_A1);
    s2.setTransmitters(TX_ARR_A2);

    Assert.assertTrue(s1.equals(s2));
    Assert.assertTrue(s2.equals(s1));

    s2.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_PIPSQUEAK);
    Assert.assertFalse(s1.equals(s2));
    Assert.assertFalse(s2.equals(s1));

    s2.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WINS);
    s2.setUpdateInterval(INTERVAL_1SEC);
    Assert.assertFalse(s1.equals(s2));
    Assert.assertFalse(s2.equals(s1));

    s2.setUpdateInterval(INTERVAL_7_5SEC);
    s2.setTransmitters((Transmitter[])null);
    Assert.assertFalse(s1.equals(s2));
    Assert.assertFalse(s2.equals(s1));

    s2.setTransmitters(TX_ARR_B2);
    Assert.assertFalse(s1.equals(s2));
    Assert.assertFalse(s2.equals(s1));
    
    s1.setTransmitters(TX_ARR_B2);
    s2.setTransmitters(TX_ARR_B3);
    Assert.assertFalse(s1.equals(s2));
    Assert.assertFalse(s2.equals(s1));
    
    s1.setTransmitters(TX_ARR_A1);
    s2.setTransmitters(TX_ARR_A2);
    Assert.assertTrue(s1.equals((Object) s2));
    Assert.assertTrue(s2.equals((Object) s1));
    
    
    s1.setTransmitters(TX_COLL_A1);
    Assert.assertTrue(s1.equals((Object) s2));
    Assert.assertTrue(s2.equals((Object) s1));
    
    Assert.assertFalse(s1.equals(Integer.valueOf(0)));
  }
  
  /**
   * Test to ensure that hash code values are consistent.
   */
  @Test
  public void testHashCode(){
    SubscriptionRequestRule s1 = new SubscriptionRequestRule();
    SubscriptionRequestRule s2 = new SubscriptionRequestRule();
    
    Assert.assertEquals(s1.hashCode(),s2.hashCode());
    
    s1.setTransmitters(TX_ARR_A1);
    s2.setTransmitters(TX_ARR_A2);
    
    Assert.assertEquals(s1.hashCode(),s2.hashCode());
    
    s1.setTransmitters(TX_ARR_B3);
    Assert.assertFalse(s1.hashCode() == s2.hashCode());
    }
}
