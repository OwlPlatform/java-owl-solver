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

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.protocol.messages.HandshakeMessage;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;

/**
 * Factory for generating Solver-Aggregator protocol codecs.
 * 
 * @author Robert Moore
 * 
 */
public class AggregatorSolverProtocolCodecFactory extends
    DemuxingProtocolCodecFactory {

  /**
   * Codec name for inserting into filter chains.
   */
  public static final String CODEC_NAME = "Grail Aggregator-Solver codec";

  /**
   * Creates a new protocol codec factory for either a solver or aggregator.
   * 
   * @param isServer
   *          {@code true} if this protocol codec factory is for an aggregator,
   *          or {@code false} for solvers.
   */
  public AggregatorSolverProtocolCodecFactory(boolean isServer) {
    super();
    if (isServer) {
      super.addMessageEncoder(HandshakeMessage.class, HandshakeEncoder.class);
      super.addMessageEncoder(SubscriptionMessage.class,
          SubscriptionMessageEncoder.class);
      super.addMessageEncoder(SampleMessage.class, SampleEncoder.class);

      super.addMessageDecoder(SubscriptionMessageDecoder.class);
      super.addMessageDecoder(HandshakeDecoder.class);
    } else {
      super.addMessageEncoder(SubscriptionMessage.class,
          SubscriptionMessageEncoder.class);
      super.addMessageEncoder(HandshakeMessage.class, HandshakeEncoder.class);

      super.addMessageDecoder(SubscriptionMessageDecoder.class);
      super.addMessageDecoder(HandshakeDecoder.class);
      super.addMessageDecoder(SampleDecoder.class);
    }
  }
}
