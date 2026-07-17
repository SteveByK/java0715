/**
 * Outbox integration context.
 *
 * <p>Persists domain events in the same transaction as business changes. A relay
 * can later publish these rows to Kafka, RocketMQ or another message platform.</p>
 */
package com.stevebyk.java0715.outbox;
