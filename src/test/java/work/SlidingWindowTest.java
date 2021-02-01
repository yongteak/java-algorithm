package work;

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
		int min = 1;
		int max = 15;
		int ago = 0;
		for (int i = 0; i < 120; i++) {
			boolean result = slidingWindow.allow();
			int sleepSec = (int)(Math.random() * (max - min + 1) + min);
			Uninterruptibles.sleepUninterruptibly(sleepSec, TimeUnit.SECONDS);
			ago += sleepSec;
			if(result) {
				log.info("#{}, sleep={},ago={},result={}", i, sleepSec, ago,result);
			}
		}
	}
}
