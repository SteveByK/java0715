/**
 * Locking infrastructure.
 *
 * <p>Provides deterministic account lock ordering for local concurrency. The
 * abstraction allows replacement with Redis, database advisory locks or a
 * distributed lock service when the monolith is split.</p>
 */
package com.stevebyk.java0715.lock;
