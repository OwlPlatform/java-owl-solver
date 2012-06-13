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

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.listeners.ConnectionListener;
import com.owlplatform.solver.listeners.SampleListener;
import com.owlplatform.solver.protocol.codec.AggregatorSolverProtocolCodecFactory;
import com.owlplatform.solver.protocol.messages.HandshakeMessage;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;
import com.owlplatform.solver.rules.SubscriptionRequestRule;

/**
 * A simple interface to the aggregator to be used by Java-based solvers.
 * Handles connection set-up and tear-down, as well as automatic handshaking.
 * 
 * @author Robert Moore II
 * 
 */
public class SolverAggregatorInterface implements SolverIoAdapter {

	/**
	 * The {@link HandshakeMessage} received from the aggregator.
	 */
	private HandshakeMessage receivedHandshake = null;

	/**
	 * The HandshakeMessage sent to the aggregator.
	 */
	private HandshakeMessage sentHandshake = null;

	/**
	 * The {@link SubscriptionMessage} request sent to the aggregator.
	 */
	private SubscriptionMessage sentSubscription = null;

	/**
	 * The {@code SubscriptionMessage} response received from the aggregator.
	 */
	private SubscriptionMessage receivedSubscription = null;

	/**
	 * Logging facility for this class.
	 */
	private static final Logger log = LoggerFactory
			.getLogger(SolverAggregatorInterface.class);

	/**
	 * How long to wait when connecting and disconnecting from the aggregator,
	 * in milliseconds.
	 */
	private long connectionTimeout = 10000;

	/**
	 * How long to wait between connection attempts to the aggregator, in
	 * milliseconds.
	 */
	private long connectionRetryDelay = 10000;

	/**
	 * Whether or not to try and stay connected to the aggregator.
	 */
	private boolean stayConnected = false;

	/**
	 * Whether or not to disconnect from the aggregator if an exception is
	 * thrown.
	 */
	private boolean disconnectOnException = true;

	/**
	 * The hostname or IP address of the aggregator.
	 */
	private String host;

	/**
	 * The port number the aggregator is listening on for solvers.
	 */
	private int port = 7009;

	/**
	 * The session of the connected aggregator, or {@code null} if no connection
	 * is established.
	 */
	private IoSession session;

	private SocketConnector connector;

	private SolverIoHandler ioHandler = new SolverIoHandler(this);
	
	private volatile boolean connected = false;

	public boolean isConnected() {
		return this.connected;
	}

	SubscriptionRequestRule[] rules = new SubscriptionRequestRule[] { SubscriptionRequestRule
			.generateGenericRule() };

	/**
	 * @return the rules
	 */
	public SubscriptionRequestRule[] getRules() {
		return this.rules;
	}

	/**
	 * @param rules
	 *            the rules to set
	 */
	public void setRules(SubscriptionRequestRule[] rules) {
		this.rules = rules;
	}

	protected ConcurrentLinkedQueue<SampleListener> sampleListeners = new ConcurrentLinkedQueue<SampleListener>();
	protected ConcurrentLinkedQueue<ConnectionListener> connectionListeners = new ConcurrentLinkedQueue<ConnectionListener>();
	
	/**
	 * Creates a new SolverAggregatorConnection with a SolverIoHandler as the IO handler.
	 */
	public SolverAggregatorInterface()
	{
		this.ioHandler = new SolverIoHandler(this);
	}
	
	/**
	 * Creates a new SolverAggregatorConnection with the provided SolverIoHandler as the IO handler.
	 */
	public SolverAggregatorInterface(SolverIoHandler ioHandler)
	{
		if(ioHandler == null)
		{
			throw new IllegalArgumentException("Cannot use a null IO Handler for aggregator interface.");
		}
		this.ioHandler = ioHandler;
		this.ioHandler.setSolverIoAdapter(this);
	}

	protected boolean setConnector() {
		if (this.host == null) {
			log.error("No host value set, cannot set up socket connector.");
			return false;
		}
		if (this.port < 0 || this.port > 65535) {
			log.error("Port value is invalid {}.", this.port);
			return false;
		}

		connector = new NioSocketConnector();
		connector.getSessionConfig().setTcpNoDelay(true);
		if (!connector.getFilterChain().contains(
				AggregatorSolverProtocolCodecFactory.CODEC_NAME)) {
			connector.getFilterChain().addLast(
					AggregatorSolverProtocolCodecFactory.CODEC_NAME,
					new ProtocolCodecFilter(
							new AggregatorSolverProtocolCodecFactory(false)));
		}
		connector.setHandler(this.ioHandler);
		log.debug("Connector set up successful.");
		return true;
	}

	private abstract static class ConnectionProcessor implements Runnable {
		protected SolverAggregatorInterface aggregator;

		public ConnectionProcessor(SolverAggregatorInterface aggregator) {
			this.aggregator = aggregator;
		}
	}

	/**
	 * Initiates a connection to the Aggregator (if it is not yet connected).
	 * 
	 * @return true if the connection is established.
	 */
	public boolean doConnectionSetup() {
		if (this.connector == null) {
			if (!this.setConnector()) {
				log.error("Unable to set up connection to the aggregator.");
				return false;
			}
		}

		if (this.session != null) {
			log.error("Already connected!");
			return false;
		}

		do {
			if (this.connect()) {
				log.debug("Connection succeeded!");
				return true;
			}

			if (this.stayConnected) {
				try {
					log
							.warn(String
									.format(
											"Connection to aggregator at %s:%d failed, waiting %dms before retrying.",
											this.host, this.port,
											this.connectionRetryDelay));
					Thread.sleep(this.connectionRetryDelay);
				} catch (InterruptedException ie) {
					// Ignored
				}
			}
		} while (this.stayConnected);

		this.disconnect();
		this.finishConnection();

		return false;
	}

	void finishConnection() {
		this.connector.dispose();
		this.connector = null;
		for (ConnectionListener listener : this.connectionListeners) {
			listener.connectionEnded(this);
		}
	}

	public void doConnectionTearDown() {
		// Make sure we don't automatically reconnect
		this.stayConnected = false;
		this.disconnect();
	}

	protected boolean connect() {

		ConnectFuture connFuture = this.connector
				.connect(new InetSocketAddress(this.host, this.port));
		if (!connFuture.awaitUninterruptibly(connectionTimeout)) {
			return false;
		}
		if (!connFuture.isConnected()) {
			return false;
		}

		try {
			log.debug("Attempting connection to {}:{}.", this.host, this.port);
			this.session = connFuture.getSession();
		} catch (RuntimeIoException ioe) {
			log.error(String.format(
					"Could not create session to aggregator %s:%d.", this.host,
					this.port), ioe);
			return false;
		}
		return true;
	}

	protected void disconnect() {
		// FIXME: Perform this operation in a non-NIO Thread!
		if (this.session != null && !this.session.isClosing()) {
			log.debug("Closing connection to aggregator at {} (waiting {}ms).",
					this.session.getRemoteAddress(), this.connectionTimeout);
			this.session.close(false).awaitUninterruptibly(connectionTimeout);
			this.session = null;
			this.sentHandshake = null;
			this.receivedHandshake = null;
			this.sentSubscription = null;
			this.receivedSubscription = null;
			for (ConnectionListener listener : this.connectionListeners) {
				listener.connectionInterrupted(this);
			}
		}
	}

	public void connectionClosed(IoSession session) {
		this.connected = false;
		this.disconnect();
		if (this.stayConnected) {
			log.info("Reconnecting to aggregator at {}:{}", this.host,
					this.port);
			new Thread(new ConnectionProcessor(this) {

				public void run() {
					if (aggregator.doConnectionSetup()) {
						return;
					}
					this.aggregator.finishConnection();
				}
			}, "Reconnect Thread").start();
		} else {
			this.finishConnection();
		}
	}

	public void connectionOpened(IoSession session) {

		if (this.session == null) {
			this.session = session;
		}

		log.info("Connected to {}.", session.getRemoteAddress());
		
		for (ConnectionListener listener : this.connectionListeners) {
			listener.connectionEstablished(this);
		}

		log.debug("Attempting to write handshake.");
		this.session.write(HandshakeMessage.getDefaultMessage());
	}

	public void handshakeReceived(IoSession session,
			HandshakeMessage handshakeMessage) {
		log.debug("Received {}", handshakeMessage);
		this.receivedHandshake = handshakeMessage;
		Boolean handshakeCheck = this.checkHandshake();
		if (handshakeCheck == null) {
			return;
		}

		if (Boolean.FALSE.equals(handshakeCheck)) {
			log.warn("Handshakes did not match.");
			this.disconnect();
		}
		if (Boolean.TRUE.equals(handshakeCheck)) {
			SubscriptionMessage msg = this.generateGenericSubscriptionMessage();
			log.debug("Attempting to write {}.", msg);
			this.session.write(msg);
			this.connected = true;
		}
	}

	public void handshakeSent(IoSession session,
			HandshakeMessage handshakeMessage) {
		log.debug("Sent {}", handshakeMessage);
		this.sentHandshake = handshakeMessage;
		Boolean handshakeCheck = this.checkHandshake();
		if (handshakeCheck == null) {
			return;
		}

		if (Boolean.FALSE.equals(handshakeCheck)) {
			log.warn("Handshakes did not match.");
			this.disconnect();
		}
		if (Boolean.TRUE.equals(handshakeCheck)) {
			SubscriptionMessage msg = this.generateGenericSubscriptionMessage();
			log.debug("Attempting to write {}.", msg);
			this.session.write(msg);
			this.connected= false;
		}
	}

	protected SubscriptionMessage generateGenericSubscriptionMessage() {

		SubscriptionMessage subMessage = new SubscriptionMessage();
		subMessage.setRules(this.rules);
		subMessage.setMessageType(SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID);

		return subMessage;
	}

	public void solverSampleReceived(IoSession session,
			SampleMessage sampleMessage) {
		for (SampleListener listener : this.sampleListeners) {
			listener.sampleReceived(this, sampleMessage);
		}
	}

	public void solverSampleSent(IoSession session, SampleMessage sampleMessage) {
		log.error("Protocol error: Sent Sample message to the aggregator:\n{}",
				sampleMessage);
		this.disconnect();
	}

	public void sessionIdle(IoSession session, IdleStatus idleStatus) {
		// TODO: Need to implement idle checking
	}

	public void subscriptionRequestReceived(IoSession session,
			SubscriptionMessage subscriptionMessage) {
		log
				.error(
						"Protocol error: Received subscription message from the aggregator:\n{}.",
						subscriptionMessage);
		this.disconnect();
	}

	public void subscriptionRequestSent(IoSession session,
			SubscriptionMessage subsriptionMessage) {
		log.info("Sent {}", subsriptionMessage);
		this.sentSubscription = subsriptionMessage;
	}

	public void subscriptionResponseReceived(IoSession session,
			SubscriptionMessage subscriptionMessage) {
		log.info("Received {}", subscriptionMessage);
		this.receivedSubscription = subscriptionMessage;

		if (this.sentSubscription == null) {
			log
					.error(
							"Protocol error: Received a subscription response without sending a request.\n{}",
							subscriptionMessage);
			this.disconnect();
			return;
		}

		if (!this.sentSubscription.equals(this.receivedSubscription)) {
			log
					.info(
							"Server did not fully accept subscription request.\nOriginal:\n{}\nAmended\n{}",
							this.sentSubscription, this.receivedSubscription);
		}
		
		for(ConnectionListener listener : this.connectionListeners){
			listener.subscriptionReceived(this, subscriptionMessage);
		}
	}

	public void subscriptionResponseSent(IoSession session,
			SubscriptionMessage subscriptionMessage) {
		log
				.error(
						"Protocol error: Sent a subscription response to the aggregator.\n{}",
						subscriptionMessage);
		this.disconnect();
		return;
	}

	public void addSampleListener(SampleListener listener) {
		this.sampleListeners.add(listener);
	}

	public void removeSampleListener(SampleListener listener) {
		this.sampleListeners.remove(listener);
	}

	public void addConnectionListener(ConnectionListener listener) {
		this.connectionListeners.add(listener);
	}

	public void removeConnectionListener(ConnectionListener listener) {
		this.connectionListeners.remove(listener);
	}

	public void exceptionCaught(IoSession session, Throwable cause) {
		log.error("Unhandled exception for: {}", cause);
		if (this.disconnectOnException) {
			this.disconnect();
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		if (host == null) {
			throw new IllegalArgumentException(
					"Aggregator host cannot be null.");
		}
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException(
					"Aggregator port must be a valid TCP port number.");
		}
		this.port = port;
	}

	protected Boolean checkHandshake() {
		if (this.sentHandshake == null) {
			log.debug("Sent handshake is null, not checking.");
			return null;
		}
		if (this.receivedHandshake == null) {
			log.debug("Received handshake is null, not checking.");
			return null;
		}

		if (!this.sentHandshake.equals(this.receivedHandshake)) {
			log
					.error(
							"Handshakes do not match.  Closing connection to aggregator at {}.",
							this.session.getRemoteAddress());
			boolean prevValue = this.stayConnected;
			this.stayConnected = false;
			this.disconnect();
			this.stayConnected = prevValue;
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isStayConnected() {
		return stayConnected;
	}

	public void setStayConnected(boolean stayConnected) {
		this.stayConnected = stayConnected;
	}

	public boolean isDisconnectOnException() {
		return disconnectOnException;
	}

	public void setDisconnectOnException(boolean disconnectOnException) {
		this.disconnectOnException = disconnectOnException;
	}

	public long getConnectionRetryDelay() {
		return connectionRetryDelay;
	}

	public void setConnectionRetryDelay(long connectionRetryDelay) {
		this.connectionRetryDelay = connectionRetryDelay;
	}

	public IoSession getSession() {
		return session;
	}
	
	@Override
	public String toString(){
	  return "Solver-Aggregator connection @" + this.host + ":" + this.port;
	}
}

