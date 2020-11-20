package com.metazion.jm.algo;

public abstract class LeaderboardTreeExtra {

    @Override
    public boolean equals(Object obj) {
        LeaderboardTreeExtra extra = (LeaderboardTreeExtra) obj;
        return this.compareTo(extra) == 0;
    }

    public abstract int compareTo(LeaderboardTreeExtra other);
}
