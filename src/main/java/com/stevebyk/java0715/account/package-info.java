/**
 * Account bounded context.
 *
 * <p>Owns the account aggregate, balance snapshot, account status lifecycle,
 * deposits, fund holds and balance mutation invariants. Other contexts must
 * change money through {@link com.stevebyk.java0715.account.AccountService}
 * instead of editing account persistence objects directly.</p>
 */
package com.stevebyk.java0715.account;
