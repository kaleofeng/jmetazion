package com.metazion.jm.collection;

import java.util.ArrayList;

public class LeaderboardTree<Extra extends LeaderboardExtra> {

	class LeaderboardNode {
		public int lowerScore = 0;
		public int upperScore = 0;
		public int count = 0;

		public ArrayList<Extra> extraList = new ArrayList<Extra>();

		public LeaderboardNode left = null;
		public LeaderboardNode right = null;

		public LeaderboardNode prev = null;
		public LeaderboardNode next = null;
	}

	LeaderboardNode root = null;

	LeaderboardNode head = null;
	LeaderboardNode tail = null;

	public void setup(int lowerScore, int upperScore) {
		root = setupNode(root, lowerScore, upperScore);
	}

	public void insert(int score, Extra extra) {
		insertIntoNode(root, score, extra);
	}

	public void remove(int score, Extra extra) {
		removeFromNode(root, score, extra);
	}

	public void change(int oldScore, int newScore, Extra extra) {
		remove(oldScore, extra);
		insert(newScore, extra);
	}

	public int getRanking(int score, Extra extra) {
		return getRankingOfNode(root, score, extra);
	}

	public ArrayList<Extra> getTopN(int n) {
		ArrayList<Extra> extraList = new ArrayList<Extra>();

		int count = 0;
		LeaderboardNode cursor = tail;
		while (cursor != null) {
			for (Extra extra : cursor.extraList) {
				extraList.add(extra);
				if (++count >= n) {
					return extraList;
				}
			}
			cursor = cursor.prev;
		}
		return extraList;
	}

	private LeaderboardNode setupNode(LeaderboardNode node, int lowerScore, int upperScore) {
		if (lowerScore > upperScore) {
			return null;
		}

		node = new LeaderboardNode();
		node.lowerScore = lowerScore;
		node.upperScore = upperScore;
		node.count = 0;
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

		if (upperScore > lowerScore) {
			final int middleScore = getMiddleScore(lowerScore, upperScore);
			node.left = setupNode(node.left, lowerScore, middleScore);
			node.right = setupNode(node.right, middleScore + 1, upperScore);
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

		++node.count;

		if (isLeafNode(node)) {
			node.extraList.add(extra);
			node.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
			return;
		}

		final int middleScore = getMiddleScore(node.lowerScore, node.upperScore);
		if (score <= middleScore) {
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

		--node.count;

		if (isLeafNode(node)) {
			node.extraList.remove(extra);
			node.extraList.sort((Extra left, Extra right) -> left.compareTo(right));
			return;
		}

		final int middleScore = getMiddleScore(node.lowerScore, node.upperScore);
		if (score <= middleScore) {
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

		if (score < node.lowerScore) {
			ranking += node.count;
			return ranking;
		}

		if (score > node.upperScore) {
			ranking += 0;
			return ranking;
		}

		if (isLeafNode(node)) {
			ranking += Math.max(node.extraList.indexOf(extra), 0);
			return ranking;
		}

		final int middleScore = getMiddleScore(node.lowerScore, node.upperScore);
		if (score <= middleScore) {
			ranking += node.right != null ? node.right.count : 0;
			ranking += getRankingOfNode(node.left, score, extra);
		} else {
			ranking += getRankingOfNode(node.right, score, extra);
		}

		return ranking;
	}

	private int getMiddleScore(int lowerScore, int upperScore) {
		final int middleScore = lowerScore + ((upperScore - lowerScore) >> 1);
		return middleScore;
	}

	private boolean isInsideNode(LeaderboardNode node, int score) {
		return score >= node.lowerScore && score <= node.upperScore;
	}

	private boolean isLeafNode(LeaderboardNode node) {
		return node.lowerScore == node.upperScore;
	}
}