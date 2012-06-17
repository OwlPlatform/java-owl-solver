package com.owlplatform.solver.protocol.messages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.rules.SubscriptionRequestRule;

/**
 * Test class for {@link SubscriptionMessage}.
 * 
 * @author Robert Moore
 * 
 */
public class SubscriptionMessageTest {

  /**
   * Static timestamp in the past.
   */
  private static final long TIME_0 = 0l;

  /**
   * Dynamic timestamp.
   */
  private static final long TIME_NOW = System.currentTimeMillis();

  /**
   * Static timestamp in the future.
   */
  private static final long TIME_1 = Long.MAX_VALUE;

  /**
   * Message length prefix for an "empty" message.
   */
  private static final int PREFIX_DEFAULT = 5;

  /**
   * Message length prefix when using rules "r21".
   */
  private static final int PREFIX_R21 = 31;

  /**
   * Message length prefix when using rules "r34".
   */
  private static final int PREFIX_R34 = 63;

  /**
   * A testing rule.
   */
  private static SubscriptionRequestRule[] r12;
  /**
   * A testing rule.
   */
  private static SubscriptionRequestRule[] r123;
  /**
   * A testing rule.
   */
  private static SubscriptionRequestRule[] r21;
  /**
   * A testing rule.
   */
  private static SubscriptionRequestRule[] r34;

  /**
   * toString() value of a newly-created subscription request obvious.
   */
  private static final String STRING_NONE = "Subscription Message (Rsp) ";

  /**
   * toString() value of a subscription response containing rule r34.
   */
  private static final String STRING_R34 = "Subscription Message (Rsp) \tSubscription rule: Phy (2) Txers {} Interval: 0ms\tSubscription rule: Phy (0) Txers {Transmitter (0x0000000000000000000000000000007B | 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF)} Interval: 0ms";

  /**
   * toString() value of a subscription request containing rule r34.
   */
  private static final String STRING_REQ_R34 = "Subscription Message (Req) \tSubscription rule: Phy (2) Txers {} Interval: 0ms\tSubscription rule: Phy (0) Txers {Transmitter (0x0000000000000000000000000000007B | 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF)} Interval: 0ms";

  /**
   * Creates a bunch of rules for use in the tests.
   */
  @BeforeClass
  public static void initRules() {
    SubscriptionRequestRule rule1 = new SubscriptionRequestRule();

    SubscriptionRequestRule rule2 = new SubscriptionRequestRule();
    rule2.setUpdateInterval(100);

    SubscriptionRequestRule rule3 = new SubscriptionRequestRule();
    rule3.setPhysicalLayer(SampleMessage.PHYSICAL_LAYER_WIFI);

    SubscriptionRequestRule rule4 = new SubscriptionRequestRule();
    rule4.setTransmitters(new Transmitter[] { new Transmitter(123) });

    r12 = new SubscriptionRequestRule[] { rule1, rule2 };
    r123 = new SubscriptionRequestRule[] { rule1, rule2, rule3 };
    r21 = new SubscriptionRequestRule[] { rule2, rule1 };
    r34 = new SubscriptionRequestRule[] { rule3, rule4 };

  }

  /**
   * Test message
   */
  private SubscriptionMessage m1;
  /**
   * Test message.
   */
  private SubscriptionMessage m2;

  /**
   * Creates blank messages for testing.
   */
  @Before
  public void createMessages() {
    this.m1 = new SubscriptionMessage();
    this.m2 = new SubscriptionMessage();
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    Assert.assertEquals(this.m1.hashCode(), this.m2.hashCode());

    this.m1.setRules(r12);
    this.m2.setRules(r21);
    Assert.assertEquals(this.m1.hashCode(), this.m2.hashCode());

    this.m1.setRules(r34);
    Assert.assertFalse(this.m1.hashCode() == this.m2.hashCode());

  }

  /**
   * Tests the computed length prefix.
   */
  @Test
  public void testGetLengthPrefix() {
    Assert.assertEquals(PREFIX_DEFAULT, this.m1.getLengthPrefix());
    this.m1.setRules(r21);
    Assert.assertEquals(PREFIX_R21, this.m1.getLengthPrefix());
    this.m1.setRules(r34);
    Assert.assertEquals(PREFIX_R34, this.m1.getLengthPrefix());
  }

  /**
   * Tests to ensure that the number of rules is computed correctly.
   */
  @Test
  public void testGetNumRules() {
    Assert.assertEquals(0, this.m1.getNumRules());
    this.m1.setRules(r123);
    Assert.assertEquals(3, this.m1.getNumRules());
    this.m1.setRules(r12);
    Assert.assertEquals(2, this.m1.getNumRules());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    Assert.assertEquals(STRING_NONE, this.m1.toString());
    this.m1.setRules(r34);
    Assert.assertEquals(STRING_R34, this.m1.toString());
    this.m1.setMessageType(SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID);
    Assert.assertEquals(STRING_REQ_R34, this.m1.toString());
  }

  /**
   * Tests the equals() methods.
   */
  @Test
  public void testEquals() {

    Assert.assertFalse(this.m1.equals(Integer.valueOf(0)));

    Assert.assertTrue(this.m1.equals(this.m1));

    Assert.assertTrue(this.m1.equals(this.m2));
    Assert.assertTrue(this.m2.equals(this.m1));

    Assert.assertTrue(this.m1.equals((Object) this.m2));
    Assert.assertTrue(this.m2.equals((Object) this.m1));

    this.m1.setRules(r12);
    this.m2.setRules(r12);
    Assert.assertTrue(this.m1.equals((Object) this.m2));
    Assert.assertTrue(this.m2.equals((Object) this.m1));

    this.m1.setRules(r21);
    Assert.assertTrue(this.m1.equals((Object) this.m2));
    Assert.assertTrue(this.m2.equals((Object) this.m1));

    this.m1.setRules(r123);
    Assert.assertFalse(this.m1.equals((Object) this.m2));
    Assert.assertFalse(this.m2.equals((Object) this.m1));
    
    this.m1.setRules(r34);
    Assert.assertFalse(this.m1.equals((Object) this.m2));
    Assert.assertFalse(this.m2.equals((Object) this.m1));

  }

  /**
   * Tests the message type values.
   */
  @Test
  public void testMessageType() {
    this.m1.setMessageType(SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID);
    Assert.assertEquals(SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID,
        this.m1.getMessageType());

    this.m2.setMessageType(SubscriptionMessage.RESPONSE_MESSAGE_ID);
    Assert.assertEquals(SubscriptionMessage.RESPONSE_MESSAGE_ID,
        this.m2.getMessageType());
  }

  /**
   * Test the creation timestamp value.
   */
  @Test
  public void testCreationTime() {
    Assert.assertTrue(this.m1.getCreationTime() > TIME_0);
    Assert.assertTrue(this.m1.getCreationTime() >= TIME_NOW);
    Assert.assertTrue(this.m1.getCreationTime() < TIME_1);

    this.m1.setCreationTime(TIME_0);
    Assert.assertEquals(TIME_0, this.m1.getCreationTime());
    this.m1.setCreationTime(TIME_1);
    Assert.assertEquals(TIME_1, this.m1.getCreationTime());
    this.m1.setCreationTime(TIME_NOW);
    Assert.assertEquals(TIME_NOW, this.m1.getCreationTime());
  }

}
