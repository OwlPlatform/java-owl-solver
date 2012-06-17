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

package com.owlplatform.solver.protocol.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.solver.protocol.messages.SubscriptionMessage;
import com.owlplatform.solver.protocol.messages.Transmitter;
import com.owlplatform.solver.rules.SubscriptionRequestRule;

/**
 * Decodes a {@code SubscriptionMessage} according to the Solver-Aggregator
 * protocol.
 * 
 * @author Robert Moore
 * 
 */
public class SubscriptionMessageDecoder implements MessageDecoder {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory
      .getLogger(SubscriptionMessageDecoder.class);

  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
    if (in.prefixedDataAvailable(4, 65535)) {
      in.mark();
      int messageLength = in.getInt();
      if (messageLength < 1) {
        in.reset();
        return MessageDecoderResult.NOT_OK;
      }

      byte messageType = in.get();
      in.reset();
      if (messageType == SubscriptionMessage.RESPONSE_MESSAGE_ID
          || messageType == SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID) {
        return MessageDecoderResult.OK;
      }

      return MessageDecoderResult.NOT_OK;
    }

    return MessageDecoderResult.NEED_DATA;
  }

  @Override
  public MessageDecoderResult decode(IoSession session, IoBuffer in,
      ProtocolDecoderOutput out) throws Exception {

    SubscriptionMessage message = new SubscriptionMessage();
    int messageLength = in.getInt();
    if (log.isDebugEnabled()) {
      log.debug("Full message length is {}.", Integer.valueOf(messageLength));
    }
    message.setMessageType(in.get());
    if (log.isDebugEnabled()) {
      log.debug("Message type {}.", Byte.valueOf(message.getMessageType()));
    }

    int numRules = in.getInt();
    if (log.isDebugEnabled()) {
      log.debug("{} rules.", Integer.valueOf(numRules));
    }
    if (numRules == 0) {
      log.warn("No subscription rules sent.");
    }

    message.setRules(new SubscriptionRequestRule[numRules]);
    for (int rulesRead = 0; rulesRead < numRules; ++rulesRead) {
      SubscriptionRequestRule rule = new SubscriptionRequestRule();
      rule.setPhysicalLayer(in.get());
      if (log.isDebugEnabled()) {
        log.debug("[Rule {}] Physical layer {}.", Integer.valueOf(rulesRead),
            Byte.valueOf(rule.getPhysicalLayer()));
      }

      int numTxers = in.getInt();
      if(log.isDebugEnabled()){
        log.debug("[Rule {}] Num txers {}.", Integer.valueOf(rulesRead), Integer.valueOf(numTxers));
      }
      rule.setTransmitters(new Transmitter[numTxers]);
      for (int txersRead = 0; txersRead < numTxers; ++txersRead) {
        Transmitter transmitter = new Transmitter();
        byte[] baseId = new byte[Transmitter.TRANSMITTER_ID_SIZE];
        byte[] mask = new byte[Transmitter.TRANSMITTER_ID_SIZE];
        in.get(baseId);
        in.get(mask);
        transmitter.setBaseId(baseId);
        transmitter.setMask(mask);
        rule.getTransmitters()[txersRead] = transmitter;
        if(log.isDebugEnabled()){
        log.debug("[Rule {}] Transmitter: {}.", Integer.valueOf(rulesRead), transmitter);
        }
      }

      rule.setUpdateInterval(in.getLong());
      if(log.isDebugEnabled()){
        log.debug("[Rule {}] Rule: {}.", Integer.valueOf(rulesRead), rule);
      }
      message.getRules()[rulesRead] = rule;
    }

    out.write(message);

    return MessageDecoderResult.OK;
  }

  @Override
  public void finishDecode(IoSession session, ProtocolDecoderOutput out)
      throws Exception {
    // Nothing to do

  }

}
