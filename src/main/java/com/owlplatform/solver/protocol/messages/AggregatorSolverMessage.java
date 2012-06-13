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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for all messages between the aggregator and solvers, excluding
 * the handshake message.
 * 
 * @author Robert Moore II
 * 
 */
public abstract class AggregatorSolverMessage {

	/**
	 * Logging facility for this class.
	 */
	private static Logger log = LoggerFactory
			.getLogger(AggregatorSolverMessage.class);

	/**
	 * The time that the packet arrived at the solver.
	 */
	private long creationTime;

	/**
	 * The type of message being sent.
	 */
	// Only used for decoding messages
	private byte messageType;

	/**
	 * Retrieves the type of message.
	 * 
	 * @return the type of message.
	 */
	public byte getMessageType() {
		return this.messageType;
	}

	/**
	 * Sets the type of this message.
	 * 
	 * @param messageType
	 *            the type of this message.
	 */
	public void setMessageType(byte messageType) {
		this.messageType = messageType;
	}

	/**
	 * Retrieves the time that this message was created as a UNIX timestamp
	 * value.
	 * 
	 * @return the creation time of this message as a UNIX timestamp value.
	 */
	public long getCreationTime() {
		return this.creationTime;
	}

	/**
	 * Sets the creation time of this message to the solver as a UNIX timestamp
	 * value.
	 * 
	 * @param arrivalTime
	 *            the creation time of this messag as a UNIX timestamp value.
	 */
	public void setCreationTime(long arrivalTime) {
		this.creationTime = arrivalTime;
	}

	/**
	 * Creates a new AggregatorSolverMessage and sets the creation time to the
	 * current time.
	 */
	protected AggregatorSolverMessage() {
		this.setCreationTime(System.currentTimeMillis());
	}
}
