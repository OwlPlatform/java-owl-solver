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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.listeners.ConnectionListener;
import com.owlplatform.solver.listeners.SampleListener;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;
import com.owlplatform.solver.rules.SubscriptionRequestRule;

public class SolverAggregatorConnection {

	private static final class Handler implements ConnectionListener,
			SampleListener {

		private final SolverAggregatorConnection parent;

		public Handler(SolverAggregatorConnection parent) {
			this.parent = parent;
		}

		@Override
		public void connectionEnded(SolverAggregatorInterface aggregator) {
			this.parent.connectionEnded(aggregator);
		}

		@Override
		public void connectionEstablished(SolverAggregatorInterface aggregator) {
			// TODO Auto-generated method stub

		}

		@Override
		public void connectionInterrupted(SolverAggregatorInterface aggregator) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sampleReceived(SolverAggregatorInterface aggregator,
				SampleMessage sample) {
			this.parent.sampleReceived(aggregator, sample);
		}

		@Override
		public void subscriptionReceived(SolverAggregatorInterface aggregator,
				SubscriptionMessage response) {
			this.parent.subscriptionAcknowledged = true;
		}
	}

	private static final Logger log = LoggerFactory
			.getLogger(SolverAggregatorConnection.class);

	protected final SolverAggregatorInterface agg = new SolverAggregatorInterface();

	protected final LinkedBlockingQueue<SampleMessage> sampleQueue = new LinkedBlockingQueue<SampleMessage>(
			1000);

	protected final Handler handler = new Handler(this);

	protected boolean connectionDead = false;

	protected final Map<Integer, SubscriptionRequestRule> ruleMap = new ConcurrentHashMap<Integer, SubscriptionRequestRule>();

	protected volatile int nextRuleNum = 0;

	protected boolean warnBufferFull = false;
	
	protected boolean subscriptionAcknowledged = false;

	/**
	 * Creates a new Aggregator interface for a solver. The aggregator will not
	 * be connected until {@link #connect()} is called.
	 */
	public SolverAggregatorConnection() {
		this(1024);
	}

	/**
	 * Creates a new Aggregator interface for a solver with the specified buffer
	 * size.
	 * 
	 * @param bufferSize
	 *            the number of samples to buffer for the solver. Samples
	 *            received after the buffer is full will be discarded and a
	 *            warning will be logged.
	 */
	public SolverAggregatorConnection(final int bufferSize) {
		super();
		this.agg.setConnectionRetryDelay(5000l);
		this.agg.setConnectionTimeout(5000l);
		this.agg.setDisconnectOnException(true);
		this.agg.setStayConnected(true);
		this.agg.setHost("localhost");
		this.agg.setPort(7008);

		this.agg.addSampleListener(this.handler);
		this.agg.addConnectionListener(this.handler);
	}

	/**
	 * Connects to the aggregator if it is not already connected.
	 * 
	 * @return {@code true} if the connection succeeds, else {@code false}.
	 */
	public boolean connect() {
		return this.agg.doConnectionSetup();
	}

	/**
	 * Disconnects from the aggregator.
	 */
	public void disconnect() {
		this.agg.doConnectionTearDown();
	}

	/**
	 * Sets the hostname/IP address for the aggregator. If the aggregator is
	 * already connected, then the new host will be used the next time a
	 * connection is established. The default value is "localhost".
	 * 
	 * @param host
	 *            the new hostname/IP address for the aggregator.
	 */
	public void setHost(final String host) {
		this.agg.setHost(host);
	}

	/**
	 * Sets the port number for the aggregator. If the aggregator is already
	 * connected, then the new port will be used the next time a connection is
	 * established. The default value is 7008.
	 * 
	 * @param port
	 */
	public void setPort(final int port) {
		this.agg.setPort(port);
	}

	/**
	 * Returns the next sample from the Aggregator, blocking until it is
	 * available if none are currently buffered. If the connection to the
	 * aggregator has been completely shut down, then this method will throw an
	 * IllegalStateException.
	 * 
	 * @return the next sample received from the Aggregator.
	 * @throws Exception
	 *             is this method is called after the Aggregator has been
	 *             disconnected.
	 */
	public SampleMessage getNextSample() throws Exception {
		if (!this.connectionDead) {
			try {
				return this.sampleQueue.take();
			} catch (InterruptedException e) {
				log.error(
						"Interrupted while waiting for next sample to arrive.",
						e);
			}
			return null;
		}
		throw new IllegalStateException(
				"Connection to the aggregator has terminated.");

	}

	/**
	 * Returns {@code true} if there is a Sample available for immediate
	 * consumption on the next call to {@link #getNextSample()}. Note that this
	 * is a "soft" state, meaning that if more than one thread has access to
	 * this {@code SolverAggregatorConnection}, there is no guarantee that the
	 * next call will actually succeed without blocking, as another thread may
	 * have consumed the available Sample.
	 * 
	 * @return {@code true} if {@code getNextSample()} can be called without
	 *         blocking, else {@code false}.
	 */
	public boolean hasNext() {
		return !this.sampleQueue.isEmpty();
	}

	/**
	 * Returns whether or not the connection to the aggregator is still "live",
	 * meaning that it may at some point return a Sample. A connection is
	 * considered "live" as long as {@link #disconnect()} has not yet been
	 * called, even if the connection hasn't yet been established.
	 * 
	 * @return {@code true} if the connection is "live", else {@code false}.
	 */
	public boolean isConnectionLive() {
		return !this.connectionDead;
	}

	/**
	 * Adds a Subscription Request Rule to the aggregator interface. If the
	 * aggregator is already connected, the rule will be sent immediately,
	 * otherwise it will be sent with all rules when the aggregator is
	 * connected.
	 * 
	 * @param rule
	 *            the rule to add to this aggregator.
	 * @return the rule number, which can be used later to remove a Subscription
	 *         Request Rule.
	 */
	public int addRule(final SubscriptionRequestRule rule) {
		Integer theRuleNum = Integer.valueOf(this.nextRuleNum);
		synchronized (this.ruleMap) {
			if (!this.ruleMap.values().contains(rule)) {
				this.ruleMap.put(theRuleNum, rule);
				
				SubscriptionRequestRule[] newRules = this.ruleMap.values().toArray(new SubscriptionRequestRule[]{});
				this.agg.setRules(newRules);
				if (this.agg.isConnected()) {
					SubscriptionMessage msg = new SubscriptionMessage();
					msg.setRules(new SubscriptionRequestRule[] { rule });
					msg.setMessageType(SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID);
					this.agg.getSession().write(msg);
				}
				++this.nextRuleNum;
				return theRuleNum.intValue();
			}
			log.warn("Rule {} is already configured for use.", rule);
			return -1;
		}
	}

	/**
	 * Removes a rule from this aggregator based on the rule number returned by
	 * {@link #addRule(SubscriptionRequestRule)}. At this time, it will not
	 * cancel the subscription request rule on the aggregator, but instead will
	 * cause a reconnection to the aggregator to refresh the rules.
	 * 
	 * @param ruleNum
	 *            the number of the rule to cancel.
	 * @return the rule that was cancelled, if one matching the rule number was
	 *         present for this aggregator.
	 */
	public SubscriptionRequestRule removeRule(final int ruleNum) {
		synchronized (this.ruleMap) {
			SubscriptionRequestRule rule = this.ruleMap.remove(Integer
					.valueOf(ruleNum));
			if (this.agg.isConnected()) {
				this.agg.disconnect();
			}
			return rule;
		}
	}

	/**
	 * Returns {@code true} if this interface will log a warning message each
	 * time a sample is dropped due to a full buffer.  By default, this interface
	 * will not warn about dropped packets, as the warning itself may cause even greater
	 * sample loss.
	 * 
	 * @return {@code true} if this interface will warn on dropped samples, else
	 *         {@code false}.
	 */
	public boolean isBufferWarningEnabled() {
		return this.warnBufferFull;
	}

	/**
	 * Set this to {@code true} to enable warning messages each time a sample
	 * cannot be delivered due to a full buffer.  Take care when setting this value
	 * to {@code true}, as excessive log messages may increase sample loss.
	 * 
	 * @param warnBufferFull
	 *            {@code true} to generate a warning log message for each lost sample.
	 */
	public void setBufferWarning(boolean warnBufferFull) {
		this.warnBufferFull = warnBufferFull;
	}
	
	@Override
	public String toString(){
		return "Aggregator @ " + this.agg.getHost() + ":" + this.agg.getPort();
	}

	void connectionEnded(SolverAggregatorInterface aggregator) {
		this.connectionDead = true;
		synchronized (this.sampleQueue) {
			this.sampleQueue.notifyAll();
		}
	}

	void sampleReceived(SolverAggregatorInterface aggregator,
			SampleMessage sample) {
		if (!this.sampleQueue.offer(sample) && this.warnBufferFull) {
			log.warn("Unable to insert a sample due to a full buffer.");
		}
	}

	public boolean isSubscriptionAcknowledged() {
		return subscriptionAcknowledged;
	}

}
