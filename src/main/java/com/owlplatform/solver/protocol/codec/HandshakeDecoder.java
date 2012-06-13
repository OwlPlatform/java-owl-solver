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
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.solver.protocol.messages.HandshakeMessage;

public class HandshakeDecoder implements MessageDecoder {

	private static final Logger log = LoggerFactory
			.getLogger(HandshakeDecoder.class);

	public static final String CONN_STATE_KEY = HandshakeDecoder.class
			.getName()
			+ ".STATE";

	static final class HubConnectionState {
		boolean handshakeReceived = false;
	};

	private static final Charset charsetASCII = Charset.forName("US-ASCII");

	public MessageDecoderResult decodable(IoSession arg0, IoBuffer arg1) {
		HubConnectionState connState = (HubConnectionState) arg0
				.getAttribute(CONN_STATE_KEY);

		if (connState == null) {
			log.debug("Creating new handshake connection state for {}.", arg0);
			connState = new HubConnectionState();
			connState.handshakeReceived = false;
			arg0.setAttribute(CONN_STATE_KEY, connState);
		}

		if (connState.handshakeReceived) {
			return MessageDecoderResult.NOT_OK;
		}

		if (!arg1.prefixedDataAvailable(4,
				HandshakeMessage.PROTOCOL_STRING_LENGTH)) {
			log
					.debug("Not yet decodable with only {} bytes.", arg1
							.remaining());
			return MessageDecoderResult.NEED_DATA;
		}

		// TODO: Need better logic to determine decodability
		return MessageDecoderResult.OK;
	}

	public MessageDecoderResult decode(IoSession arg0, IoBuffer arg1,
			ProtocolDecoderOutput arg2) throws Exception {
		HubConnectionState connState = (HubConnectionState) arg0
				.getAttribute(CONN_STATE_KEY);
		if (connState == null) {
			log.debug("Creating new handshake connection state for {}.", arg0);
			connState = new HubConnectionState();
			connState.handshakeReceived = false;
			arg0.setAttribute(CONN_STATE_KEY, connState);
		}

		// If handshake already received, skip this decoder
		if (connState.handshakeReceived) {
			log.warn("Handshake already received.");
			return MessageDecoderResult.NOT_OK;
		}

		if (arg1.prefixedDataAvailable(4,
				HandshakeMessage.PROTOCOL_STRING_LENGTH)) {
			HandshakeMessage message = new HandshakeMessage();
			message.setStringLength(arg1.getInt());
			if (message.getStringLength() != HandshakeMessage.PROTOCOL_STRING_LENGTH) {
				throw new RuntimeException(String.format(
						"Handshake protocol string length is incorrect: %d",
						message.getStringLength()));
			}

			message.setProtocolString(String.valueOf(arg1.getString(message
					.getStringLength(), HandshakeDecoder.charsetASCII
					.newDecoder())));
			message.setVersionNumber(arg1.get());
			message.setReservedBits(arg1.get());

			arg2.write(message);
			log.debug("Wrote {}.", message);
			connState.handshakeReceived = true;
			return MessageDecoderResult.OK;
		}
		// Entire message is not yet available
		log.warn("Insufficient buffer size: {}.", arg1.remaining());
		return MessageDecoderResult.NEED_DATA;
	}

	public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
			throws Exception {
		// Nothing to do
	}

}
