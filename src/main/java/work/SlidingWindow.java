package work;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlidingWindow {
  private ConcurrentMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();
  // 시간 경계선 문제해결을 위한 마지막 요청 시간 정보
  private long lastMS = 0;
  // 1분
  private final double minute = 60000.0;
  // 졔약 시간 Window
  private final int windowSizeInMs;
  // windows 시간당 최대 요총 횟수
  private final int maxRequestPerWindow;

  /**
   * @param maxRequestPerWindow 지정된 시간당 최대 요청 갯수
   * @param windowSizeInMs 지정된 제약 시간
   */
  protected SlidingWindow(int maxRequestPerWindow, int windowSizeInMs) {
    this.maxRequestPerWindow = maxRequestPerWindow;
    this.windowSizeInMs = windowSizeInMs;
  }

  boolean allow() {
    long now = System.currentTimeMillis();
    // windows 키값 설정
    long curWindowKey = now / windowSizeInMs * windowSizeInMs;
    // 현재 window에서의 요청 횟수 등록
    windows.putIfAbsent(curWindowKey, new AtomicInteger(0));
    // 60초 이전 키
    long preWindowKey = curWindowKey - windowSizeInMs;
    // 이전 윈도우크기에 대한 요청 횟수 가져오기
    AtomicInteger preCount = windows.get(preWindowKey);
//    INFO: now=1612179124154,curWindowKey=1612179120000, preWindowKey=1612179060000
//    log.info("now={},curWindowKey={}, preWindowKey={}",now,curWindowKey,preWindowKey);
    if (preCount == null) {
          lastMS = now;
//      60초 이전에 데이터가 없으면 현재 갯수를 증가하고 값 비교 반환
      return windows.get(curWindowKey).incrementAndGet() <= maxRequestPerWindow;
    }
    long ago = now - lastMS;
    // 60초동안 요청이 없는경우 모든 조건을 초기화한다.
    if (ago >= windowSizeInMs) {
      log.info("init!! ago={}, windowSizeInMs={}",ago,windowSizeInMs);
      lastMS = now;
      windows = new ConcurrentHashMap<>();
      return true;
    }

    // ex) 75초에 요청이 발생한경우 60초와 120초의 0.25(25%) 지점 나머지 지점의 0.75(75%)를 계산한다.
    double preWeight = 1-(ago/minute);
    // 제한시간내 호출된 횟수 * 요청시간 나머지 지점 비율 + 제한시간 초과후 호출된 횟수
    long count = (long) (preCount.get() * preWeight + windows.get(curWindowKey).incrementAndGet());
    log.info("### preCount={},preWeight={},curCount={}, ago={}, result={}",preCount.get(),preWeight,windows.get(curWindowKey).get(),ago,count <= maxRequestPerWindow);
//    상태값 변경
    lastMS = now;
    // maxRequestPerWindow 보다 작으면 요청 처리, 그렇지 않으면 거부
    return count <= maxRequestPerWindow;
  }
}

