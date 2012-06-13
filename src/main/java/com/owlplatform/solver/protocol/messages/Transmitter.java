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

import com.owlplatform.common.util.NumericUtils;


/**
 * Represents a transmitter within a {@code SubscriptionMessage} rule, specified
 * by a base address and a mask.
 * 
 * @author Robert Moore II
 * 
 */
public class Transmitter {

	/**
	 * The length, in octets, of the transmitter identifier.
	 */
	public static final int TRANSMITTER_ID_SIZE = 16;

	/**
	 * The base address of the transmitter(s) referenced in some subscription
	 * rule.
	 */
	private byte[] baseId;

	/**
	 * The bitwise mask of the transmitter(s) referenced in some subscription
	 * rule.
	 */
	private byte[] mask;

	/**
	 * Returns the base address for this transmitter.
	 * 
	 * @return the base address for this transmitter.
	 */
	public byte[] getBaseId() {
		return this.baseId;
	}

	/**
	 * Sets the base address for this transmitter.
	 * 
	 * @param baseId
	 *            the base address for this transmitter.
	 */
	public void setBaseId(byte[] baseId) {
		if (baseId == null) {
			throw new IllegalArgumentException(
					"Transmitter base ID cannot be null.");
		}
		if (baseId.length != TRANSMITTER_ID_SIZE) {
			throw new IllegalArgumentException(String.format(
					"Transmitter base ID must be %d bytes long.", Integer
							.valueOf(TRANSMITTER_ID_SIZE)));
		}
		this.baseId = baseId;
	}

	/**
	 * Returns the bit mask for this transmitter.
	 * 
	 * @return the bit mask for this transmitter.
	 */
	public byte[] getMask() {
		return this.mask;
	}

	/**
	 * Sets the bit mask for this transmitter.
	 * 
	 * @param mask
	 *            the bit mask for this transmitter.
	 */
	public void setMask(byte[] mask) {
		if (mask == null) {
			throw new IllegalArgumentException(
					"Transmitter mask cannot be null.");
		}
		if (mask.length != TRANSMITTER_ID_SIZE) {
			throw new IllegalArgumentException(String.format(
					"Transmitter mask must be %d bytes long.", Integer
							.valueOf(TRANSMITTER_ID_SIZE)));
		}
		this.mask = mask;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Transmitter (").append(
				NumericUtils.toHexString(this.getBaseId())).append(" | ")
				.append(NumericUtils.toHexString(this.getMask())).append(')');
		return sb.toString();
	}
}
