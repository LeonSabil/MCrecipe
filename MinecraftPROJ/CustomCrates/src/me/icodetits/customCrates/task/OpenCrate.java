package me.icodetits.customCrates.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

public class OpenCrate {
	
	private static final double BASE_MOD = 12.3;
	private static final double SPEED_MOD = 1.3;
	
	@Getter private final Map<Integer, Integer> speedCache = new ConcurrentHashMap<>();
	@Getter private final AtomicInteger speedIt = new AtomicInteger(1), speedItBase = new AtomicInteger(1);
	@Getter private final AtomicInteger loopIt = new AtomicInteger(0);
	@Getter @Setter private int loopItMax = 100;

	public OpenCrate() {
		this.loopItMax *= SPEED_MOD;
		this.speedCache.put(0, 1);
		for (int j = 1; j <= 40; j++) {
			int base = (int) (j + ThreadLocalRandom.current().nextInt(j) / 1.5);
			int speed = (int) (base * SPEED_MOD);
			if (j == 20) {
				base = (int) (base * 0.85);
			} else if (j == 30) {
				base = (int) (base * 0.65);
			} else if (j == 40) {
				base = (int) (base * 0.45);
			}
			base *= BASE_MOD;
			this.speedCache.put(base, speed);
		}
	}

	public boolean should() {
		int currLoop = this.loopIt.incrementAndGet();
		int currBase = this.speedItBase.incrementAndGet();
		if (this.speedCache.containsKey(currLoop)) {
			this.speedIt.set(this.speedCache.get(currLoop));
		}
		if (currBase >= this.speedIt.get()) {
			this.speedItBase.set(0);
			return true;
		}
		return false;
	}

	public void check() {
		if (this.loopIt.get() >= this.loopItMax) {
			int maxIt = 2;
			while (maxIt-- > 0) this.speedIt.incrementAndGet();
		}
	}

	public void destroy() {
		this.loopIt.set(0);
		this.speedIt.set(1);
		this.speedItBase.set(1);
		this.speedCache.clear();
	}
}