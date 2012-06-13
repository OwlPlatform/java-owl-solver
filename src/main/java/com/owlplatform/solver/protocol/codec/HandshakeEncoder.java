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

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.solver.protocol.messages.HandshakeMessage;

public class HandshakeEncoder implements MessageEncoder<HandshakeMessage> {

	private static final Logger log = LoggerFactory
			.getLogger(HandshakeEncoder.class);

	private static final String CONN_STATE_KEY = HandshakeEncoder.class
			.getName()
			+ ".STATE";

	private static final class HubConnectionState {
		boolean handshakeSent = false;
	};

	public void dispose(IoSession arg0) throws Exception {
		// No objects created by this encoder.
	}

	public void encode(IoSession session, HandshakeMessage message,
			ProtocolEncoderOutput out) throws Exception {
		HubConnectionState connState = (HubConnectionState) session
				.getAttribute(CONN_STATE_KEY);
		if (connState == null) {
			connState = new HubConnectionState();
			connState.handshakeSent = false;
			session.setAttribute(CONN_STATE_KEY, connState);
		}

		log.debug("Sending handshake message.");

		if (connState.handshakeSent) {
			log.warn("Handshake already sent.");
			return;
		}

		IoBuffer buffer = IoBuffer.allocate(HandshakeMessage.MESSAGE_LENGTH);

		buffer.putInt(message.getStringLength());
		buffer.put(message.getProtocolString().getBytes("ASCII"));
		buffer.put(message.getVersionNumber());
		buffer.put(message.getReservedBits());

		buffer.flip();

		out.write(buffer);

		connState.handshakeSent = true;

	}

}
