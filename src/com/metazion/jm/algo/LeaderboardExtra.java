package com.metazion.jm.algo;

public abstract class LeaderboardExtra {

	// unique identification
	@Override
	public boolean equals(Object obj) {
		LeaderboardExtra extra = (LeaderboardExtra) obj;
		return this.compareTo(extra) == 0;
	}

	// determines the order
	public abstract int compareTo(LeaderboardExtra other);
}