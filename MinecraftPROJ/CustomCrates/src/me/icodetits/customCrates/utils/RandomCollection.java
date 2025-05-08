package me.icodetits.customCrates.utils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;
import lombok.Setter;

public class RandomCollection<E> {
	@Getter @Setter private List<Item<E>> weightMap;
	@Getter @Setter private ThreadLocalRandom random;

	public RandomCollection() {
		this(ThreadLocalRandom.current());
	}

	public RandomCollection(ThreadLocalRandom random) {
		this.random = random;
		this.weightMap = new CopyOnWriteArrayList<Item<E>>();
	}

	public void add(double weight, E result) {
		if (weight <= 0.0D)
			return;
		this.weightMap.add(new Item<E>(result, weight));
	}

	public void remove(E result) {
		for (Item<E> item : this.weightMap) {
			if (item.getType().equals(result)) {
				this.weightMap.remove(item);
			}
		}
	}

	public double getWeight(E result) {
		for (Item<E> item : this.weightMap) {
			if (item.getType().equals(result)) {
				return item.getChance();
			}
		}

		return 0.0D;
	}

	public E next() {
		Collections.shuffle(this.weightMap);
		
		double chance = randomChance();

		for (Item<E> entry : this.weightMap) {
			if (chance <= entry.getChance()) {
				return entry.getType();
			}
		}
		
		return next();
	}

	public void destroy() {
		this.random = null;
		this.weightMap.clear();
		this.weightMap = null;
	}
	
	public double randomChance() {
		return (Math.random() * 100.0D);
	}
	
	@Override
	public RandomCollection<E> clone() {
		RandomCollection<E> cloned = new RandomCollection<E>();
		for (Item<E> entry : this.weightMap) {
			cloned.add(entry.getChance(), entry.getType());
		}
		return cloned;
	}
	
	public static class Item<Type> {
		@Getter @Setter private Type type;
		@Getter @Setter private double chance;

		public Item(Type type, double chance) {
			setType(type);
			setChance(chance);
		}
	}
}
