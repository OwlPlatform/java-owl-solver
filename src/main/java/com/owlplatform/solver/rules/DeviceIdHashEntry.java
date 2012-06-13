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

package com.owlplatform.solver.rules;

import java.util.concurrent.ConcurrentHashMap;

import com.owlplatform.common.util.HashableByteArray;


public class DeviceIdHashEntry {
	private boolean passedRules = false;

	private long updateInterval = 0l;
	
	private ConcurrentHashMap<HashableByteArray, Long> nextPermittedTransmit = new ConcurrentHashMap<HashableByteArray, Long>();

	public boolean isPassedRules() {
		return passedRules;
	}

	public void setPassedRules(boolean passedRules) {
		this.passedRules = passedRules;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	public long getNextPermittedTransmit(byte[] receiverId) {
		HashableByteArray hash = new HashableByteArray(receiverId);
		
		Long nextTransmit = this.nextPermittedTransmit.get(hash);
		if(nextTransmit == null)
		{
			this.nextPermittedTransmit.put(hash,Long.valueOf(Long.MIN_VALUE));
			return Long.MIN_VALUE;
		}
		
		return nextTransmit.longValue();
	}

	public void setNextPermittedTransmit(byte[] receiverId, long nextPermittedTransmit) {
		HashableByteArray hash = new HashableByteArray(receiverId);
		this.nextPermittedTransmit.put(hash,Long.valueOf(nextPermittedTransmit));
	}

}
