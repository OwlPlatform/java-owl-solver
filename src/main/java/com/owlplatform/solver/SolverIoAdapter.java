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

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.protocol.messages.HandshakeMessage;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;

/**
 * An interface to define classes that respond to events on a Solver-Aggregator connection.
 * @author Robert Moore
 *
 */
public interface SolverIoAdapter
{
	/**
	 * Called when a solver connects to the aggregator.
	 * @param session
	 */
    public void connectionOpened(IoSession session);
    
    /**
     * Called when a solver disconnects from the aggregator.
     * @param session
     */
    public void connectionClosed(IoSession session);
    
    /**
     * Called when an exception is thrown while communicating with the aggregator.
     * @param session
     * @param exception
     */
    public void exceptionCaught(IoSession session, Throwable exception);
    
    /**
     * Called when a Handshake message has been received from the solver.
     * @param session
     * @param handshakeMessage
     */
    public void handshakeReceived(IoSession session, HandshakeMessage handshakeMessage);
    
    /**
     * Called after a Handshake message has been sent to the solver.
     * @param session
     * @param handshakeMessage
     */
    public void handshakeSent(IoSession session, HandshakeMessage handshakeMessage);
    
    /**
     * Called when a SubscriptionMessage (request) has been received.
     * @param session
     * @param subscriptionMessage
     */
    public void subscriptionRequestReceived(IoSession session, SubscriptionMessage subscriptionMessage);
    
    /**
     * Called after a SubscriptionMessage (request) has been sent.
     * @param session
     * @param subscriptionMessage
     */
    public void subscriptionRequestSent(IoSession session, SubscriptionMessage subscriptionMessage);
    
    /**
     * Called after a SubscriptionMessage (response) has been sent.
     * @param session
     * @param subscriptionMessage
     */
    public void subscriptionResponseSent(IoSession session, SubscriptionMessage subscriptionMessage);
    
    /**
     * Called when a SubscriptionMessage (response) has been received.
     * @param session
     * @param subscriptionMessage
     */
    public void subscriptionResponseReceived(IoSession session, SubscriptionMessage subscriptionMessage);
    
    /**
     * Called after a Sample message has been sent.
     * @param session
     * @param sampleMessage
     */
    public void solverSampleSent(IoSession session, SampleMessage sampleMessage);
    
    /**
     * Called when a Sample message has been received.
     * @param session
     * @param sampleMessage
     */
    public void solverSampleReceived(IoSession session, SampleMessage sampleMessage);
    
    /**
     * Called after an IoSession has become idle for a period of time.
     * @see IoHandler#sessionIdle(IoSession, IdleStatus)
     * @param session
     * @param idleStatus
     */
    public void sessionIdle(IoSession session, IdleStatus idleStatus);
}
