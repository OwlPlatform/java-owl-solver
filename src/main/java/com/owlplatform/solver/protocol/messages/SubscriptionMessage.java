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

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.solver.rules.SubscriptionRequestRule;

/**
 * Represents a subscription request or response, sent by the Solver or
 * Aggregator, respectively, according to the GRAIL RTLS v3 Aggregator-Solver
 * protocol.
 * 
 * @author Robert Moore II
 * 
 */
public class SubscriptionMessage extends AggregatorSolverMessage {

	/**
	 * Logging facility for this class.
	 */
	private static final Logger log = LoggerFactory
			.getLogger(SubscriptionMessage.class);

	/**
	 * Message type identifier for request messages sent by the solver.
	 */
	public static final byte SUBSCRIPTION_MESSAGE_ID = 3;

	/**
	 * Message type identifier for response messages sent by the aggregator.
	 */
	public static final byte RESPONSE_MESSAGE_ID = 4;

	/**
	 * The array of subscription rules referenced in this message.
	 */
	private SubscriptionRequestRule[] rules;

	public int getLengthPrefix() {
		// Message ID
		int length = 1;
		// Number of rules
					length += 4;
		if (this.rules != null) {
			for (SubscriptionRequestRule rule : this.rules) {
				// Physical Layer
				++length;
				// Transmitters
				length += 4 + rule.getNumTransmitters()
						* Transmitter.TRANSMITTER_ID_SIZE * 2;
				// Update interval
				length += 8;
			}
			
		}
		
		return length;
	}

	/**
	 * Returns the number of rules specified in this subscription message.
	 * 
	 * @return the number of rules specified in this subscription message.
	 */
	public int getNumRules() {
		if (this.rules == null) {
			return 0;
		}
		return this.rules.length;
	}

	/**
	 * Returns the array of subscription rules for this subscription message.
	 * 
	 * @return the array of subscription rules for this subscription message.
	 */
	public SubscriptionRequestRule[] getRules() {
		return this.rules;
	}

	/**
	 * Sets the array of subscription rules for this subscription message.
	 * 
	 * @param rules
	 *            the array of subscription rules for this subscription message.
	 */
	public void setRules(SubscriptionRequestRule[] rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Subscription Message (");
		if (this.getMessageType() == SUBSCRIPTION_MESSAGE_ID) {
			sb.append("Req) ");
		} else {
			sb.append("Rsp) ");
		}

		if (this.getRules() != null) {
			for (SubscriptionRequestRule rule : this.getRules()) {
				sb.append('\t').append(rule);
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
	    if(o instanceof SubscriptionMessage)
	    {
	        return this.equals((SubscriptionMessage)o);
	    }
	    return super.equals(o);
	}
	
	public boolean equals(SubscriptionMessage msg)
	{
	    if(this.getNumRules() != msg.getNumRules())
	    {
	        return false;
	    }
	    if(this.rules != null && msg.rules != null)
	    {
	        for(SubscriptionRequestRule rule : this.rules)
	        {
	            boolean matchedRules = false;
	            for(SubscriptionRequestRule oRule : msg.getRules())
	            {
	                if(rule.equals(oRule))
	                {
	                    matchedRules = true;
	                    break;
	                }
	            }
	            if(!matchedRules)
	            {
	                return false;
	            }
	        }
	    }
	    return true;
	}
}
