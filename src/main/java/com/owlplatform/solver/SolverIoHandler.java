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

package com.owlplatform.solver;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.protocol.messages.HandshakeMessage;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;

public class SolverIoHandler implements IoHandler {

	private static final Logger log = LoggerFactory
			.getLogger(SolverIoHandler.class);

	protected SolverIoAdapter solverIoAdapter;

	public void setSolverIoAdapter(SolverIoAdapter solverIoAdapter) {
		this.solverIoAdapter = solverIoAdapter;
	}

	public SolverIoHandler(SolverIoAdapter solverIoAdapter) {
		this.solverIoAdapter = solverIoAdapter;
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
	    log.warn("Exception for {}: {}", session, cause);
		if(this.solverIoAdapter != null)
		{
		    this.solverIoAdapter.exceptionCaught(session, cause);
		}
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		log.debug("{} <-- {}", session, message);
		if (this.solverIoAdapter == null) {
			log.warn("No IoAdapter defined, ignoring message from {}.\n{}",
					session, message);
			return;
		}
		if (message instanceof HandshakeMessage) {
			this.solverIoAdapter.handshakeReceived(session,
					(HandshakeMessage) message);
		} else if (message instanceof SubscriptionMessage) {
			SubscriptionMessage subMsg = (SubscriptionMessage) message;
			if (subMsg.getMessageType() == SubscriptionMessage.RESPONSE_MESSAGE_ID) {
				this.solverIoAdapter.subscriptionResponseReceived(session,
						subMsg);
			} else if (subMsg.getMessageType() == SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID) {
				this.solverIoAdapter.subscriptionRequestReceived(session,
						(SubscriptionMessage) message);
			} else {
				log.error("Incorrect message ID received from {}: {}", session,
						message);
			}
		} else if (message instanceof SampleMessage) {
			this.solverIoAdapter.solverSampleReceived(session,
					(SampleMessage) message);
		} else {
			log.warn("Unknown message type received from {}: {}", session,
					message);
		}
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		log.debug("{} --> {}", session, message);
		if (this.solverIoAdapter == null) {
			log.warn("No IoAdapter defined, ignoring message to {}.\n{}",
					session, message);
			return;
		}
		if (message instanceof HandshakeMessage) {
			this.solverIoAdapter.handshakeSent(session,
					(HandshakeMessage) message);
		} else if (message instanceof SubscriptionMessage) {
			SubscriptionMessage subMsg = (SubscriptionMessage)message;
			if(subMsg.getMessageType() == SubscriptionMessage.RESPONSE_MESSAGE_ID)
			{
				this.solverIoAdapter.subscriptionResponseSent(session,
						(SubscriptionMessage) message);
			}
			else if(subMsg.getMessageType() == SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID)
			{
				this.solverIoAdapter.subscriptionRequestSent(session, subMsg);
			}
			else
			{
				log.warn("Incorrect message ID received from {}: {}", session, message);
			}
			
		} else if (message instanceof SampleMessage) {
			this.solverIoAdapter.solverSampleSent(session,
					(SampleMessage) message);
		} else {
			log.warn("Unknown message type sent to {}: {}", session, message);
		}
	}

	public void sessionClosed(IoSession session) throws Exception {
		log.debug("Session closed {}.", session);
		if (this.solverIoAdapter != null) {
			this.solverIoAdapter.connectionClosed(session);
		}
	}

	public void sessionCreated(IoSession session) throws Exception {
		// Don't worry about this, handle sessionOpened instead.
	}

	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		log.debug("Session idle{}.", session, status);
		if (this.solverIoAdapter != null) {
			this.solverIoAdapter.sessionIdle(session, status);
		}

	}

	public void sessionOpened(IoSession session) throws Exception {
		log.debug("Session opened {}.", session);
		if (this.solverIoAdapter != null) {
			this.solverIoAdapter.connectionOpened(session);
		}
	}

}
