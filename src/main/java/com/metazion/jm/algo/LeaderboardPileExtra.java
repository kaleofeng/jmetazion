package com.metazion.jm.algo;

public abstract class LeaderboardPileExtra {

    public int key = 0;

    @Override
    public boolean equals(Object obj) {
        LeaderboardPileExtra extra = (LeaderboardPileExtra) obj;
        return this.compareTo(extra) == 0;
    }

    public abstract int compareTo(LeaderboardPileExtra other);
}
