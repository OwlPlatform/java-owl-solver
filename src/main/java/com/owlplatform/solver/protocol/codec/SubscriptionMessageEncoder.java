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

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.solver.protocol.messages.SubscriptionMessage;
import com.owlplatform.solver.protocol.messages.Transmitter;
import com.owlplatform.solver.rules.SubscriptionRequestRule;

public class SubscriptionMessageEncoder implements
		MessageEncoder<SubscriptionMessage> {

	private static final Logger log = LoggerFactory
			.getLogger(SubscriptionMessageEncoder.class);

	public void encode(IoSession session, SubscriptionMessage message,
			ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(message.getLengthPrefix() + 4);

		buffer.putInt(message.getLengthPrefix());
		buffer.put(message.getMessageType());
		buffer.putInt(message.getNumRules());
		for (SubscriptionRequestRule rule : message.getRules()) {
			buffer.put(rule.getPhysicalLayer()).putInt(
					rule.getNumTransmitters());
			if (rule.getNumTransmitters() > 0) {
				for (Transmitter txer : rule.getTransmitters()) {
					buffer.put(txer.getBaseId()).put(txer.getMask());
				}
			}
			buffer.putLong(rule.getUpdateInterval());
		}

		log.debug("Message length: {}.", buffer.capacity());

		buffer.flip();
		out.write(buffer);
	}
}
