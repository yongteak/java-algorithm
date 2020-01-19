package com.mimul.ratelimit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenBucket extends RateLimiter {
  private int tokens;
  private int capacity;
  private long lastRefillTime;

  public TokenBucket(int maxRequestPerSec) {
    super(maxRequestPerSec);
    this.tokens = maxRequestPerSec;
    this.capacity = maxRequestPerSec;
    this.lastRefillTime = scaledTime();
  }

  @Override
  public boolean allow() {
    synchronized (this) {
      refillTokens();
      if (this.tokens == 0) {
        return false;
      }
      this.tokens--;
      return true;
    }
  }

  private void refillTokens() {
    final long now = scaledTime();
    final double elapsedTime = (now - lastRefillTime);
    int cnt = (int) (elapsedTime * maxRequestPerSec);
    this.tokens = Math.min(this.tokens + cnt, capacity);
    lastRefillTime = now;
  }

  private long scaledTime() {
    return System.currentTimeMillis() / 1000;
  }
}
