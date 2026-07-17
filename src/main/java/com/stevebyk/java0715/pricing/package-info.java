/**
 * Pricing bounded context.
 *
 * <p>Owns exchange-rate rules, fee rules and short-lived remittance quotes.
 * Quote consumption is protected with database locking so downstream settlement
 * cannot reuse an already consumed price decision.</p>
 */
package com.stevebyk.java0715.pricing;
