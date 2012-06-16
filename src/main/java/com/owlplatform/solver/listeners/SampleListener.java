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

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.SolverAggregatorInterface;

/**
 * Implementing classes will respond to the arrival of Sample messages from the
 * aggregator.
 * 
 * @author Robert Moore
 * 
 */
public interface SampleListener {

  /**
   * Called each time a {@code SampleMessage} arrives and is decoded. Calls may
   * be invoked within different threads, so any code executed within the method
   * body should be thread-safe.
   * 
   * @param aggregator
   * @param sample
   */
  public void sampleReceived(SolverAggregatorInterface aggregator,
      SampleMessage sample);
}
