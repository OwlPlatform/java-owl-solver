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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.protocol.messages.HandshakeMessage;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;

public class ThreadedSolverIoHandler extends SolverIoHandler {

	private static final Logger log = LoggerFactory
			.getLogger(ThreadedSolverIoHandler.class);

	protected ExecutorService messageHandlerPool = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public ThreadedSolverIoHandler(SolverIoAdapter solverIoAdapter) {
		super(solverIoAdapter);
	}

	private static final class MessageHandlerTask implements Runnable {

		private static final Logger log = LoggerFactory.getLogger(MessageHandlerTask.class);
		
		private IoSession session = null;
		private SolverIoAdapter ioAdapter = null;
		private boolean isSending = false;
		private Object message = null;

		@Override
		public void run() {
			if (ioAdapter == null) {
				return;
			}
			if (session == null) {
				return;
			}
			if (message == null) {
				return;
			}
			
			if(message instanceof SampleMessage)
			{
				if(isSending)
				{
					this.ioAdapter.solverSampleSent(session, (SampleMessage)message);
				}
				else
				{
					this.ioAdapter.solverSampleReceived(session, (SampleMessage)message);
				}
			}
				
			else if (message instanceof HandshakeMessage) {
					if (isSending) {
						this.ioAdapter.handshakeSent(session,
								(HandshakeMessage) message);
					} else {
						this.ioAdapter.handshakeReceived(session,
								(HandshakeMessage) message);
					}
				}
			else if(message instanceof SubscriptionMessage)
			{
				SubscriptionMessage subMessage = (SubscriptionMessage)message;
				if(subMessage.getMessageType() == SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID)
				{
					if(isSending)
					{
						this.ioAdapter.subscriptionRequestSent(session, subMessage);
					}
					else
					{
						this.ioAdapter.subscriptionRequestReceived(session, subMessage);
					}
				}
				else if(subMessage.getMessageType() == SubscriptionMessage.RESPONSE_MESSAGE_ID)
				{
					if(isSending)
					{
						this.ioAdapter.subscriptionResponseSent(session, subMessage);
					}
					else
					{
						this.ioAdapter.subscriptionResponseReceived(session, subMessage);
					}
				}
				else {
					log.warn("Unknown message type received from {}: {}",
							session, message);
				}
			}
		}

		public void setSession(IoSession session) {
			this.session = session;
		}

		public void setIoAdapter(SolverIoAdapter ioAdapter) {
			this.ioAdapter = ioAdapter;
		}

		public void setSending(boolean isSending) {
			this.isSending = isSending;
		}

		public void setMessage(Object message) {
			this.message = message;
		}
	}

	@Override
	public void exceptionCaught(final IoSession session, final Throwable cause)
			throws Exception {
		log.warn("Exception for {}: {}", session, cause);
		cause.printStackTrace();
		if (this.solverIoAdapter != null) {
			this.messageHandlerPool.submit(new Runnable() {

				@Override
				public void run() {
					solverIoAdapter.exceptionCaught(session, cause);
				}
			});

		}
	}

	public void messageReceived(final IoSession session, final Object message)
			throws Exception {
		log.debug("Received message from {}: {}", session, message);
		if (this.solverIoAdapter == null) {
			log.warn("No IoAdapter defined, ignoring message from {}.\n{}",
					session, message);
			return;
		}

		MessageHandlerTask task = new MessageHandlerTask();
		task.setIoAdapter(this.solverIoAdapter);
		task.setSession(session);
		task.setMessage(message);
		task.setSending(false);
		
		this.messageHandlerPool.submit(task);
	}

	public void messageSent(final IoSession session, final Object message)
			throws Exception {
		MessageHandlerTask task = new MessageHandlerTask();
		task.setIoAdapter(this.solverIoAdapter);
		task.setSession(session);
		task.setMessage(message);
		task.setSending(true);
		
		this.messageHandlerPool.submit(task);
	}

	public void sessionClosed(final IoSession session) throws Exception {
		log.debug("Session closed {}.", session);
		if (this.solverIoAdapter != null) {
			this.messageHandlerPool.submit(new Runnable() {

				@Override
				public void run() {
					solverIoAdapter.connectionClosed(session);
				}
			});
		}
	}

	public void sessionIdle(final IoSession session, final IdleStatus status)
			throws Exception {
		log.debug("Session idle{}.", session, status);
		if (this.solverIoAdapter != null) {
			this.messageHandlerPool.submit(new Runnable() {

				@Override
				public void run() {
					solverIoAdapter.sessionIdle(session, status);
				}
			});
		}

	}

	public void sessionOpened(final IoSession session) throws Exception {
		log.debug("Session opened {}.", session);
		if (this.solverIoAdapter != null) {
			this.messageHandlerPool.submit(new Runnable() {

				@Override
				public void run() {
					solverIoAdapter.connectionOpened(session);
				}
			});

		}
	}

}
