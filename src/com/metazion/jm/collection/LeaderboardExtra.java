package com.metazion.jm.collection;

public abstract class LeaderboardExtra {

	@Override
	public boolean equals(Object obj) {
		LeaderboardExtra extra = (LeaderboardExtra) obj;
		return this.compareTo(extra) == 0;
	}

	abstract int compareTo(LeaderboardExtra other);
}