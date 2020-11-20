package com.metazion.jm.algo;

import java.util.ArrayList;

public class LeaderboardTree<Extra extends LeaderboardTreeExtra> {

    class LeaderboardNode {
        public int lowerKey = 0;
        public int upperKey = 0;
        public int number = 0;

        public ArrayList<Extra> extraList = new ArrayList<Extra>();

        public LeaderboardNode left = null;
        public LeaderboardNode right = null;

        public LeaderboardNode prev = null;
        public LeaderboardNode next = null;
    }

    LeaderboardNode root = null;

    LeaderboardNode head = null;
    LeaderboardNode tail = null;

    public void setup(int lowerKey, int upperKey) {
        root = setupNode(root, lowerKey, upperKey);
    }

    public void insert(int key, Extra extra) {
        insertIntoNode(root, key, extra);
    }

    public void remove(int key, Extra extra) {
        removeFromNode(root, key, extra);
    }

    public void change(int oldKey, int newKey, Extra extra) {
        remove(oldKey, extra);
        insert(newKey, extra);
    }

    public int getRanking(int key, Extra extra) {
        return getRankingOfNode(root, key, extra) + 1;
    }

    public LeaderboardTreeData getRankingN(int n) {
        return getRankingNOfNode(root, n, n);
    }

    public ArrayList<LeaderboardTreeData> getTopN(int n) {
        ArrayList<LeaderboardTreeData> dataList = new ArrayList<LeaderboardTreeData>();
        int count = 0;
        LeaderboardNode cursor = tail;
        while (cursor != null) {
            for (Extra extra : cursor.extraList) {
                LeaderboardTreeData data = new LeaderboardTreeData();
                data.ranking = ++count;
                data.key = cursor.lowerKey;
                data.extra = extra;
                dataList.add(data);
                if (count >= n) {
                    return dataList;
                }
            }
            cursor = cursor.prev;
        }
        return dataList;
    }

    private LeaderboardNode setupNode(LeaderboardNode node, int lowerKey, int upperKey) {
        if (lowerKey > upperKey) {
            return null;
        }

        node = new LeaderboardNode();
        node.lowerKey = lowerKey;
        node.upperKey = upperKey;
        node.number = 0;
        node.extraList.clear();

        if (isLeafNode(node)) {
            if (head == null) {
                head = node;
            }

            if (tail != null) {
                tail.next = node;
                node.prev = tail;
            }

            tail = node;
            return node;
        }

        if (upperKey > lowerKey) {
            final int middleKey = getMiddleKey(lowerKey, upperKey);
            node.left = setupNode(node.left, lowerKey, middleKey);
            node.right = setupNode(node.right, middleKey + 1, upperKey);
        }

        return node;
    }

    private void insertIntoNode(LeaderboardNode node, int key, Extra extra) {
        if (node == null) {
            return;
        }

        if (!isInsideNode(node, key)) {
            return;
        }

        ++node.number;

        if (isLeafNode(node)) {
            node.extraList.add(extra);
            node.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
            return;
        }

        final int middleKey = getMiddleKey(node.lowerKey, node.upperKey);
        if (key <= middleKey) {
            insertIntoNode(node.left, key, extra);
        } else {
            insertIntoNode(node.right, key, extra);
        }
    }

    private void removeFromNode(LeaderboardNode node, int key, Extra extra) {
        if (node == null) {
            return;
        }

        if (!isInsideNode(node, key)) {
            return;
        }

        --node.number;

        if (isLeafNode(node)) {
            node.extraList.remove(extra);
            node.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
            return;
        }

        final int middleKey = getMiddleKey(node.lowerKey, node.upperKey);
        if (key <= middleKey) {
            removeFromNode(node.left, key, extra);
        } else {
            removeFromNode(node.right, key, extra);
        }
    }

    private int getRankingOfNode(LeaderboardNode node, int key, Extra extra) {
        int ranking = 0;

        if (node == null) {
            return ranking;
        }

        if (key < node.lowerKey) {
            ranking += node.number;
            return ranking;
        }

        if (key > node.upperKey) {
            ranking += 0;
            return ranking;
        }

        if (isLeafNode(node)) {
            ranking += Math.max(node.extraList.indexOf(extra), 0);
            return ranking;
        }

        final int middleKey = getMiddleKey(node.lowerKey, node.upperKey);
        if (key <= middleKey) {
            ranking += node.right != null ? node.right.number : 0;
            ranking += getRankingOfNode(node.left, key, extra);
        } else {
            ranking += getRankingOfNode(node.right, key, extra);
        }

        return ranking;
    }

    private LeaderboardTreeData getRankingNOfNode(LeaderboardNode node, int n, int ranking) {
        if (node == null) {
            return null;
        }

        if (ranking > node.number) {
            return null;
        }

        if (isLeafNode(node)) {
            LeaderboardTreeData data = null;
            for (Extra extra : node.extraList) {
                ranking -= 1;
                if (ranking <= 0) {
                    data = new LeaderboardTreeData();
                    data.key = node.lowerKey;
                    data.ranking = n;
                    data.extra = extra;
                    break;
                }
            }
            return data;
        }

        if (node.right != null && ranking > node.right.number) {
            ranking -= node.right.number;
            return getRankingNOfNode(node.left, n, ranking);
        } else {
            return getRankingNOfNode(node.right, n, ranking);
        }
    }

    private int getMiddleKey(int lowerKey, int upperKey) {
        final int middleKey = lowerKey + ((upperKey - lowerKey) >> 1);
        return middleKey;
    }

    private boolean isInsideNode(LeaderboardNode node, int key) {
        return key >= node.lowerKey && key <= node.upperKey;
    }

    private boolean isLeafNode(LeaderboardNode node) {
        return node.lowerKey == node.upperKey;
    }
}
