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
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.owlplatform.common.SampleMessage;

/**
 * Decoder for the {@code SampleMessage} according to the Solver-Aggregator protocol.
 * @author Robert Moore
 *
 */
public class SampleDecoder implements MessageDecoder {
  
	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer buffer) {

		// TODO: Decide on some max limit 64k is IP
		if (buffer.prefixedDataAvailable(4, 65536)) {
			buffer.mark();
			int messageLength = buffer.getInt();
			if (messageLength < 1) {
				buffer.reset();
				return MessageDecoderResult.NOT_OK;
			}

			byte messageType = buffer.get();
			buffer.reset();
			if (messageType == SampleMessage.MESSAGE_TYPE) {
				return MessageDecoderResult.OK;
			}
			return MessageDecoderResult.NOT_OK;
		}
		return MessageDecoderResult.NEED_DATA;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {

		SampleMessage message = new SampleMessage();
		
		int messageLength = in.getInt();
		
		byte type = in.get();
		if(type != SampleMessage.MESSAGE_TYPE){
			throw new ProtocolDecoderException("Invalid message type value: " + type);
		}
		--messageLength;
		message.setPhysicalLayer(in.get());
		--messageLength;
		byte[] deviceId = new byte[SampleMessage.DEVICE_ID_SIZE];
		in.get(deviceId);
		message.setDeviceId(deviceId);
		messageLength -= SampleMessage.DEVICE_ID_SIZE;
		byte[] receiverId = new byte[SampleMessage.DEVICE_ID_SIZE];
		in.get(receiverId);
		message.setReceiverId(receiverId);
		messageLength -= SampleMessage.DEVICE_ID_SIZE;
		message.setReceiverTimeStamp(in.getLong());
		messageLength -= 8;
		message.setRssi(in.getFloat());
		messageLength -= 4;
		
		if(messageLength > 0)
		{
			byte[] sensedData = new byte[messageLength];
			in.get(sensedData);
			message.setSensedData(sensedData);
		}
		
		
		out.write(message);
		
		return MessageDecoderResult.OK;
	}

	@Override
  public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
			throws Exception {
		// TODO Auto-generated method stub

	}
}
