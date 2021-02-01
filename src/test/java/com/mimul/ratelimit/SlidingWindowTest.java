package com.mimul.ratelimit;

import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SlidingWindowTest {

	@Test
	public void testSlidingWindow() throws Exception {
//		60초당 6개
		SlidingWindow slidingWindow = new SlidingWindow(6, 60 * 1000);
		for (int i = 0; i < 120; i++) {
			boolean result = slidingWindow.allow();
			Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
			if(result)
				log.info("{}, result={}", i, result);
		}
	}
}
