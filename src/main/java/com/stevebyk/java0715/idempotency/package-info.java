/**
 * Idempotency support context.
 *
 * <p>Protects externally submitted commands from duplicate money movement.
 * Business services check this boundary before mutating aggregates.</p>
 */
package com.stevebyk.java0715.idempotency;
