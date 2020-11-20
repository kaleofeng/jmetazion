package com.metazion.jm.algo;

import java.util.ArrayList;

public class LeaderboardPile<Extra extends LeaderboardPileExtra> {

    class LeaderboardBucket {
        public int lowerKey = 0;
        public int upperKey = 0;
        public int number = 0;

        public ArrayList<Extra> extraList = new ArrayList<Extra>();
    }

    private int lowerKey = 0;
    private int upperKey = 0;
    private int bucketNumber = 0;
    private ArrayList<LeaderboardBucket> buckets = new ArrayList<LeaderboardBucket>();

    public void setup(int lowerKey, int upperKey, int bucketNumber) {
        assert (upperKey - lowerKey) / bucketNumber > 0;
        assert (upperKey - lowerKey) % bucketNumber == 0;

        this.lowerKey = lowerKey;
        this.upperKey = upperKey;
        this.bucketNumber = bucketNumber;
        setupBucket();
    }

    public void insert(int key, Extra extra) {
        insertIntoBucket(key, extra);
    }

    public void remove(int key, Extra extra) {
        removeFromBucket(key, extra);
    }

    public void change(int oldKey, int newKey, Extra extra) {
        remove(oldKey, extra);
        insert(newKey, extra);
    }

    public int getRanking(int key, Extra extra) {
        return getRankingOfBucket(key, extra) + 1;
    }

    public LeaderboardPileData getRankingN(int n) {
        LeaderboardPileData data = null;

        int ranking = n;
        int bucketIndex = 0;
        for (LeaderboardBucket bucket : buckets) {
            if (ranking > bucket.number) {
                ranking -= bucket.number;
                bucketIndex += 1;
            } else {
                break;
            }
        }

        if (bucketIndex < 0 || bucketIndex >= bucketNumber) {
            return data;
        }

        LeaderboardBucket bucket = buckets.get(bucketIndex);
        if (bucket == null) {
            return data;
        }

        if (ranking <= 0 || ranking > bucket.number) {
            return data;
        }

        for (Extra extra : bucket.extraList) {
            ranking -= 1;
            if (ranking <= 0) {
                data = new LeaderboardPileData();
                data.key = extra.key;
                data.ranking = n;
                data.extra = extra;
                break;
            }
        }

        return data;
    }

    public ArrayList<LeaderboardPileData> getTopN(int n) {
        ArrayList<LeaderboardPileData> dataList = new ArrayList<LeaderboardPileData>();
        int count = 0;
        for (LeaderboardBucket bucket : buckets) {
            for (Extra extra : bucket.extraList) {
                LeaderboardPileData data = new LeaderboardPileData();
                data.ranking = ++count;
                data.key = extra.key;
                data.extra = extra;
                dataList.add(data);
                if (count >= n) {
                    return dataList;
                }
            }
        }
        return dataList;
    }

    private void setupBucket() {
        if (lowerKey > upperKey) {
            return;
        }

        for (int index = 0; index < bucketNumber; ++index) {
            LeaderboardBucket bucket = new LeaderboardBucket();
            bucket.lowerKey = getBucketLowerKey(index);
            bucket.upperKey = getBucketUpperKey(index);
            bucket.number = 0;
            bucket.extraList.clear();
            buckets.add(bucket);
        }
    }

    private void insertIntoBucket(int key, Extra extra) {
        final int bucketIndex = getBucketIndex(key);
        LeaderboardBucket bucket = buckets.get(bucketIndex);
        if (bucket != null) {
            extra.key = key;
            bucket.extraList.add(extra);
            bucket.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
            bucket.number += 1;
        }
    }

    private void removeFromBucket(int key, Extra extra) {
        final int bucketIndex = getBucketIndex(key);
        LeaderboardBucket bucket = buckets.get(bucketIndex);
        if (bucket != null) {
            extra.key = key;
            bucket.extraList.remove(extra);
            bucket.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
            bucket.number -= 1;
        }
    }

    private int getRankingOfBucket(int key, Extra extra) {
        int ranking = 0;

        final int bucketIndex = getBucketIndex(key);
        for (int index = 0; index < bucketIndex; ++index) {
            LeaderboardBucket bucket = buckets.get(index);
            if (bucket != null) {
                ranking += bucket.number;
            }
        }

        LeaderboardBucket bucket = buckets.get(bucketIndex);
        if (bucket != null) {
            extra.key = key;
            ranking += getBucketRanking(bucket, extra);
        }

        return ranking;
    }

    private int getBucketLowerKey(int index) {
        final int bucketLowerKey = getBucketUpperKey(index + 1) + 1;
        return bucketLowerKey;
    }

    private int getBucketUpperKey(int index) {
        final int stepKey = getStepKey();
        final int bucketUpperKey = upperKey - stepKey * index;
        return bucketUpperKey;
    }

    private int getBucketIndex(int key) {
        if (key < lowerKey) {
            return bucketNumber - 1;
        }

        if (key > upperKey) {
            return 0;
        }

        final int stepKey = getStepKey();
        final int diffKey = upperKey - key;
        final int index = diffKey / stepKey;
        return index;
    }

    private int getBucketRanking(LeaderboardBucket bucket, Extra extra) {
        int ranking = 0;
        for (Extra bucketExtra : bucket.extraList) {
            if (extra.compareTo(bucketExtra) > 0) {
                ranking += 1;
            } else {
                break;
            }
        }
        return ranking;
    }

    public int getStepKey() {
        final int stepKey = (upperKey - lowerKey + 1) / bucketNumber;
        return stepKey;
    }
}
