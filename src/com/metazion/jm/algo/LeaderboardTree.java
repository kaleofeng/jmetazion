package com.metazion.jm.algo;

import java.util.ArrayList;

public class LeaderboardTree<Extra extends LeaderboardExtra> {

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

	public void insert(int score, Extra extra) {
		insertIntoNode(root, score, extra);
	}

	public void remove(int score, Extra extra) {
		removeFromNode(root, score, extra);
	}

	public void change(int oldKey, int newKey, Extra extra) {
		remove(oldKey, extra);
		insert(newKey, extra);
	}

	public int getRanking(int score, Extra extra) {
		return getRankingOfNode(root, score, extra) + 1;
	}

	public ArrayList<LeaderboardData> getTopN(int n) {
		ArrayList<LeaderboardData> dataList = new ArrayList<LeaderboardData>();

		int count = 0;
		LeaderboardNode cursor = tail;
		while (cursor != null) {
			for (Extra extra : cursor.extraList) {
				LeaderboardData data = new LeaderboardData();
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

	private void insertIntoNode(LeaderboardNode node, int score, Extra extra) {
		if (node == null) {
			return;
		}

		if (!isInsideNode(node, score)) {
			return;
		}

		++node.number;

		if (isLeafNode(node)) {
			node.extraList.add(extra);
			node.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
			return;
		}

		final int middleKey = getMiddleKey(node.lowerKey, node.upperKey);
		if (score <= middleKey) {
			insertIntoNode(node.left, score, extra);
		} else {
			insertIntoNode(node.right, score, extra);
		}
	}

	private void removeFromNode(LeaderboardNode node, int score, Extra extra) {
		if (node == null) {
			return;
		}

		if (!isInsideNode(node, score)) {
			return;
		}

		--node.number;

		if (isLeafNode(node)) {
			node.extraList.remove(extra);
			node.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
			return;
		}

		final int middleKey = getMiddleKey(node.lowerKey, node.upperKey);
		if (score <= middleKey) {
			removeFromNode(node.left, score, extra);
		} else {
			removeFromNode(node.right, score, extra);
		}
	}

	private int getRankingOfNode(LeaderboardNode node, int score, Extra extra) {
		int ranking = 0;

		if (node == null) {
			return ranking;
		}

		if (score < node.lowerKey) {
			ranking += node.number;
			return ranking;
		}

		if (score > node.upperKey) {
			ranking += 0;
			return ranking;
		}

		if (isLeafNode(node)) {
			ranking += Math.max(node.extraList.indexOf(extra), 0);
			return ranking;
		}

		final int middleKey = getMiddleKey(node.lowerKey, node.upperKey);
		if (score <= middleKey) {
			ranking += node.right != null ? node.right.number : 0;
			ranking += getRankingOfNode(node.left, score, extra);
		} else {
			ranking += getRankingOfNode(node.right, score, extra);
		}

		return ranking;
	}

	private int getMiddleKey(int lowerKey, int upperKey) {
		final int middleKey = lowerKey + ((upperKey - lowerKey) >> 1);
		return middleKey;
	}

	private boolean isInsideNode(LeaderboardNode node, int score) {
		return score >= node.lowerKey && score <= node.upperKey;
	}

	private boolean isLeafNode(LeaderboardNode node) {
		return node.lowerKey == node.upperKey;
	}
}