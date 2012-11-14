import java.util.*;

public class TreapIndexedList {

	// specific code
	static final int NEUTRAL_VALUE = Integer.MIN_VALUE;
	static final int NEUTRAL_DELTA = 0;

	static int joinValues(int leftValue, int rightValue) {
		return Math.max(leftValue, rightValue);
	}

	static int joinDeltas(int oldDelta, int newDelta) {
		return oldDelta + newDelta;
	}

	static int joinValueWithDelta(int value, int delta, int length) {
		return value + delta;
	}

	// generic code
	static Random random = new Random();

	static class Treap {
		int nodeValue;
		int subTreeValue;
		int delta;
		int count;
		long prio;
		Treap left;
		Treap right;

		Treap(int value) {
			nodeValue = value;
			subTreeValue = value;
			delta = NEUTRAL_DELTA;
			count = 1;
			prio = random.nextLong();
		}

		void update() {
			subTreeValue = joinValues(joinValues(getSubTreeValue(left), nodeValue), getSubTreeValue(right));
			count = 1 + getCount(left) + getCount(right);
		}
	}

	static void applyDelta(Treap root, int delta) {
		if (root == null)
			return;
		root.delta = joinDeltas(root.delta, delta);
		root.nodeValue = joinValueWithDelta(root.nodeValue, delta, 1);
		root.subTreeValue = joinValueWithDelta(root.subTreeValue, delta, root.count);
	}

	static void pushDelta(Treap root) {
		if (root == null)
			return;
		applyDelta(root.left, root.delta);
		applyDelta(root.right, root.delta);
		root.delta = NEUTRAL_DELTA;
	}

	static int getCount(Treap root) {
		return root == null ? 0 : root.count;
	}

	static int getSubTreeValue(Treap root) {
		return root == null ? NEUTRAL_VALUE : root.subTreeValue;
	}

	static class TreapPair {
		Treap left;
		Treap right;

		TreapPair(Treap left, Treap right) {
			this.left = left;
			this.right = right;
		}
	}

	static TreapPair split(Treap root, int minRight) {
		if (root == null)
			return new TreapPair(null, null);
		pushDelta(root);
		if (getCount(root.left) >= minRight) {
			TreapPair sub = split(root.left, minRight);
			root.left = sub.right;
			root.update();
			sub.right = root;
			return sub;
		} else {
			TreapPair sub = split(root.right, minRight - getCount(root.left) - 1);
			root.right = sub.left;
			root.update();
			sub.left = root;
			return sub;
		}
	}

	static Treap merge(Treap left, Treap right) {
		pushDelta(left);
		pushDelta(right);
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left.prio > right.prio) {
			left.right = merge(left.right, right);
			left.update();
			return left;
		} else {
			right.left = merge(left, right.left);
			right.update();
			return right;
		}
	}

	static Treap insert(Treap root, int index, int value) {
		TreapPair t = split(root, index);
		return merge(merge(t.left, new Treap(value)), t.right);
	}

	static Treap remove(Treap root, int index) {
		TreapPair t = split(root, index);
		return merge(t.left, split(t.right, index + 1 - getCount(t.left)).right);
	}

	static Treap modify(Treap root, int a, int b, int delta) {
		TreapPair t1 = split(root, b + 1);
		TreapPair t2 = split(t1.left, a);
		applyDelta(t2.right, delta);
		return merge(merge(t2.left, t2.right), t1.right);
	}

	static class TreapAndResult {
		Treap treap;
		int value;

		TreapAndResult(Treap t, int value) {
			this.treap = t;
			this.value = value;
		}
	}

	static TreapAndResult query(Treap root, int a, int b) {
		TreapPair t1 = split(root, b + 1);
		TreapPair t2 = split(t1.left, a);
		int value = getSubTreeValue(t2.right);
		return new TreapAndResult(merge(merge(t2.left, t2.right), t1.right), value);
	}

	static void print(Treap root) {
		if (root == null)
			return;
		pushDelta(root);
		print(root.left);
		System.out.print(root.nodeValue + " ");
		print(root.right);
	}

	// Random test
	public static void main(String[] args) {
		Treap treap = null;
		List<Integer> list = new ArrayList<Integer>();
		Random rnd = new Random();
		for (int step = 0; step < 100000; step++) {
			int cmd = rnd.nextInt(6);
			if (cmd < 2 && list.size() < 100) {
				int pos = rnd.nextInt(list.size() + 1);
				int delta = rnd.nextInt(100);
				list.add(pos, delta);
				treap = insert(treap, pos, delta);
			} else if (cmd < 3 && list.size() > 0) {
				int pos = rnd.nextInt(list.size());
				list.remove(pos);
				treap = remove(treap, pos);
			} else if (cmd < 4 && list.size() > 0) {
				int b = rnd.nextInt(list.size());
				int a = rnd.nextInt(b + 1);
				int res = list.get(a);
				for (int i = a + 1; i <= b; i++)
					res = joinValues(res, list.get(i));
				TreapAndResult tr = query(treap, a, b);
				treap = tr.treap;
				if (res != tr.value) {
					System.out.println(list);
					print(treap);
					return;
				}
			} else if (cmd < 5 && list.size() > 0) {
				int b = rnd.nextInt(list.size());
				int a = rnd.nextInt(b + 1);
				int delta = rnd.nextInt(100);
				for (int i = a; i <= b; i++)
					list.set(i, joinValueWithDelta(list.get(i), delta, 1));
				treap = modify(treap, a, b, delta);
			} else {
				for (int i = 0; i < list.size(); i++) {
					TreapAndResult tr = query(treap, i, i);
					treap = tr.treap;
					int v = tr.value;
					if (list.get(i) != v) {
						System.out.println(list);
						print(treap);
						return;
					}
				}
			}
		}
		System.out.println("Test passed");
	}
}