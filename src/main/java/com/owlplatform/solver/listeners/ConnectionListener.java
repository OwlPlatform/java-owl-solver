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
package com.owlplatform.solver.listeners;

import com.owlplatform.solver.SolverAggregatorInterface;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;

/**
 * Interface for classes that wish to be notified of connection-related events
 * from the Aggregator. This includes opened, interrupted, and closed
 * connections, and when a subscription response is received.
 * 
 * @author Robert Moore
 * 
 */
public interface ConnectionListener {
  /**
   * Called when a connection to the aggregator is terminated and will not be
   * reestablished.
   * 
   * @param aggregator
   */
  public void connectionEnded(SolverAggregatorInterface aggregator);

  /**
   * Called when a connection to the aggregator is opened.
   * 
   * @param aggregator
   */
  public void connectionEstablished(SolverAggregatorInterface aggregator);

  /**
   * Called when a connection to the aggregator is terminated, but may be
   * reestablished.
   * 
   * @param aggregator
   */
  public void connectionInterrupted(SolverAggregatorInterface aggregator);

  /**
   * Called whenever a subscription response message is received from the
   * aggregator.
   * 
   * @param aggregator
   * @param response
   */
  public void subscriptionReceived(SolverAggregatorInterface aggregator,
      SubscriptionMessage response);

}
