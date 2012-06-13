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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.common.util.NumericUtils;
import com.owlplatform.solver.protocol.messages.Transmitter;

public class SubscriptionRuleFilter {

	private static final Logger log = LoggerFactory
			.getLogger(SubscriptionRuleFilter.class);

	public static boolean applyRule(SubscriptionRequestRule rule,
			SampleMessage sampleMessage) {
		// TODO: Need to implement update period rules

		if (!checkPhysicalLayer(rule, sampleMessage)) {
			log.debug("Sample physical layer does not match rule.");
			return false;
		}

		// Check the transmitter ID value

		if (!checkTransmitters(rule, sampleMessage)) {
			log.debug("No transmitter values matched.");
			return false;
		}
		return true;
	}

	protected static boolean checkPhysicalLayer(SubscriptionRequestRule rule,
			SampleMessage sampleMessage) {
		// TODO: Hard-coded "unknown" physical layer should be defined somewhere
		if (rule.getPhysicalLayer() == 0) {
			return true;
		}
		return rule.getPhysicalLayer() == sampleMessage.getPhysicalLayer();
	}

	protected static boolean checkTransmitters(SubscriptionRequestRule rule,
			SampleMessage sampleMessage) {
		if (rule.getTransmitters() == null
				|| rule.getTransmitters().length == 0) {
			log.debug("No transmitter filters defined, skipping this check.");
		} else {
			boolean matched = false;
			for (Transmitter transmitter : rule.getTransmitters()) {
				byte[] baseId = transmitter.getBaseId();
				if (baseId == null) {
					log.warn("Base ID is null, skipping this rule.");
					continue;
				}
				byte[] deviceId = sampleMessage.getDeviceId();
				if (deviceId == null) {
					log.error("Device ID is null, rejecting this sample.");
					return false;
				}

				byte[] mask = transmitter.getMask();
				if (mask == null) {
					log.warn("Mask is null, skipping this rule.");
					continue;
				}

				if (baseId.length != mask.length
						|| baseId.length != deviceId.length) {
					log
							.warn("BaseId/Mask and DeviceId lengths do not match! Skipping this rule.");
					continue;
				}

				boolean txerMatched = true;
				for (int i = 0; i < deviceId.length; ++i) {
					if ((deviceId[i] & mask[i]) != baseId[i]) {
						log
								.debug(
										"Rule {}: DeviceId/Mask does not match BaseId at index {}.",
										rule, i);
						log.debug("Mask: " + NumericUtils.toHexString(mask[i]) + "Device: " + NumericUtils.toHexString(deviceId[i]) + " Base: " + NumericUtils.toHexString(baseId[i]));
						txerMatched = false;
						break;
					}
				}
				if (txerMatched) {
					matched = true;
					break;
				}
			}
			// Couldn't find a base/mask that matched this device ID so reject
			if (!matched) {
				return false;
			}
		}
		return true;
	}
}
