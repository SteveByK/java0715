package com.stevebyk.java0715.auth;

/**
 * Refresh-token value returned to the client together with its database hash.
 */
record IssuedRefreshToken(String token, String tokenId, String tokenHash) {
}
