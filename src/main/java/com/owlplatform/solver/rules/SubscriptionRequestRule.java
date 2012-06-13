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

import java.util.Arrays;

import com.owlplatform.solver.protocol.messages.Transmitter;

public class SubscriptionRequestRule {

	private byte physicalLayer;

	private Transmitter[] transmitters;

	private long updateInterval;

	public byte getPhysicalLayer() {
		return physicalLayer;
	}

	public void setPhysicalLayer(byte physicalLayer) {
		this.physicalLayer = physicalLayer;
	}

	public int getNumTransmitters() {
		if (this.transmitters == null) {
			return 0;
		}
		return this.transmitters.length;
	}

	public Transmitter[] getTransmitters() {
		return transmitters;
	}

	public void setTransmitters(Transmitter[] transmitters) {
		this.transmitters = transmitters;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	public static SubscriptionRequestRule generateGenericRule() {
		SubscriptionRequestRule rule = new SubscriptionRequestRule();

		rule.setUpdateInterval(0l);

		// TODO: Need to determine PHY definition for "ALL"
		rule.setPhysicalLayer((byte) 0);

		return rule;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Subscription rule: ");
		sb.append("Phy (").append(this.getPhysicalLayer());
		sb.append(") Txers {");
		if (this.getNumTransmitters() > 0) {
			boolean isFirst = true;
			for (Transmitter txer : this.getTransmitters()) {
				if (!isFirst) {
					sb.append(", ");
				}
				sb.append(txer);
				isFirst = false;
			}
		}
		sb.append("} Interval: ").append(this.getUpdateInterval()).append("ms");

		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if(o instanceof SubscriptionRequestRule)
	    {
	        return this.equals((SubscriptionRequestRule)o);
	    }
	    return super.equals(o);
	}
	
	public boolean equals(SubscriptionRequestRule rule)
	{
	    if(this.physicalLayer != rule.physicalLayer)
	    {
	        return false;
	    }
	    if(this.updateInterval != rule.updateInterval)
	    {
	        return false;
	    }
	    if(this.getNumTransmitters() != rule.getNumTransmitters())
	    {
	        return false;
	    }
	    if(this.transmitters != null)
	    {
	        // Check all transmitters
	        for(Transmitter txer : this.transmitters)
	        {
	            boolean matched = false;
	            for(Transmitter oTxer : rule.transmitters)
	            {
	                if(Arrays.equals(txer.getBaseId(), oTxer.getBaseId()) && Arrays.equals(txer.getMask(), oTxer.getMask()))
	                {
	                    matched = true;
	                    break;
	                }
	            }
	            if(!matched)
	            {
	                return false;
	            }
	        }
	    }
	    return true;
	    
	}
}
