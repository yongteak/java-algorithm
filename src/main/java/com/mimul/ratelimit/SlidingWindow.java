package com.mimul.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlidingWindow {//} extends RateLimiter {
  private final ConcurrentMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();
  private final int windowSizeInMs;
  private final int maxRequestPerWindow;

  /**
   *
   * @param maxRequestPerWindow 지정된 시간당 최대 요청 갯수
   * @param windowSizeInMs 지정된 제약 시간
   */
  protected SlidingWindow(int maxRequestPerWindow, int windowSizeInMs) {
    this.maxRequestPerWindow = maxRequestPerWindow;
    this.windowSizeInMs = windowSizeInMs;
  }

  boolean allow() {
    long now = System.currentTimeMillis();
    // 현재 window 크기 / 1000000
    // 161215..
    long curWindowKey = now / windowSizeInMs * windowSizeInMs;
    // 현재 window에서의 요청 횟수 등록
    windows.putIfAbsent(curWindowKey, new AtomicInteger(0));
    // 지정된 제약 시간 이전 키 설정
    long preWindowKey = curWindowKey - windowSizeInMs;
    // 이전 윈도우크기에 대한 요청 횟수 가져오기
    AtomicInteger preCount = windows.get(preWindowKey);
//    log.info("now={}, curWindowKey={},preWindowKey={},preCount={}", now, curWindowKey,preWindowKey,preCount);
    if (preCount == null) {
      // 현재 윈도우 크기 +1 후 maxRequestPerSec보다 작으면 요청 처리
//      1, 6
      return windows.get(curWindowKey).incrementAndGet() <= maxRequestPerWindow;
    }
    // 이전 기간 비율
//    1 - (1612159313170 - 161215) / 1000.0
    double preWeight = 1 - (now - curWindowKey) / 1000.0;
    // 비율 계산, 제한시간내 호출된 횟수 * 이전 기간 비율 + 제한시간 초과후 호출된 횟수
    long count = (long) (preCount.get() * preWeight + windows.get(curWindowKey).incrementAndGet());
    System.out.println("출력할데이터");
    // maxRequestPerSec보다 작으면 요청 처리, 그렇지 않으면 거부
    return count <= maxRequestPerWindow;
  }
}
