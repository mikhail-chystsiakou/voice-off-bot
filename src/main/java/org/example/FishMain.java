package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FishMain {
    public static void main(String[] args) {
//        new Solution().findMaxFish(new int[][]{{0,2,1,0},{4,0,0,3},{1,0,0,4},{0,3,2,0}});
//        new Solution().findMaxFish(new int[][]{{0,8,10},{2,8,0}});
//        new Solution1().findMaxFish(new int[][]{{0,4,2,0,9},{0,0,0,2,3},{0,4,7,4,0},{0,2,0,0,0},{5,9,0,0,5}});
//        new Solution4().countOperationsToEmptyArray(new int[]{1,2,4,3});
        new Solution41().countOperationsToEmptyArray(new int[]{1,2,4,3});
    }

    static class Solution41 {
        public long countOperationsToEmptyArray(int[] nums) {
            int[] dp = new int[nums.length];
            int min = Integer.MAX_VALUE;

            int c = 0;
            for (int i = 0; i < nums.length; i++) {
                if (nums[i] < min) {
                    min = nums[i];
                    c++;
                }
                min = Math.max(min, nums[i]);
                dp[i] = c;
            }

            long ops = 0;
            for (int i = nums.length - 1; i >= 0; i--) {
                if (i > 0 && dp[i-1] == dp[i]) {
                    ops++;
                    continue;
                } else if (i > 0) {
                    ops += i;
                    ops++;
                } else {
                    ops++;
                }
            }
            return ops;
        }
    }

    static class Solution4 {
        public long countOperationsToEmptyArray(int[] nums) {
            int[] ordered = new int[nums.length];
            System.arraycopy(nums, 0, ordered, 0, nums.length);
            Arrays.sort(ordered);

            LL head = null;
            LL tail = null;
            for (int i = nums.length-1; i >= 0; i--) {
                LL l = new LL();
                if (tail == null) tail = l;
                l.value = nums[i];
                l.next = head;
                head = l;
            }
            tail.next = head;

            int sp = 0;
            long ops = 0;
            LL prev = tail;
            while (head != null) {
                ops++;
                if (prev.next == prev) break;
                if (head.value == ordered[sp]) {
                    sp++;
                    prev.next = head.next;
                } else {
                    prev = head;
                }
                head = head.next;
            }

            return ops;
        }

        class LL {
            LL next;
            int value;
        }
    }

    static class Solution1 {
        public int findMaxFish(int[][] grid) {
            int n = grid.length;
            int m = grid[0].length;
            int size = n * m;
            UnionFind uf = new UnionFind(size);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i][j] == 0) continue;

                    // left
                    if (j > 0 && grid[i][j-1] > 0) {
                        uf.union(i * m + j, i * m + j - 1);
                    }

                    // right
                    if (j < m-1 && grid[i][j+1] > 0) {
                        uf.union(i * m + j, i * m + j + 1);
                    }

                    // top
                    if (i < n-1 && grid[i+1][j] > 0) {
                        uf.union(i * m + j, (i+1) * m + j);
                    }

                    // bot
                    if (i > 0 && grid[i-1][j] > 0) {
                        uf.union(i * m + j, (i-1) * m + j);
                    }
                }
            }
            for (int i = 0; i < uf.roots.length; i++) {
                relax(uf.roots, i);
            }
            Map<Integer, Integer> waters = new HashMap<>();
            for (int i = 0; i < uf.roots.length; i++) {
                int ii = i / m;
                int jj = i % m;
                waters.compute(uf.roots[i], (k, v) -> v == null ? grid[ii][jj] : v + grid[ii][jj]);
            }
            int sum = 0;
            for (Map.Entry<Integer, Integer> e : waters.entrySet()) {
                sum = Math.max(sum, e.getValue());
            }
            return sum;
        }

        private int relax(int[] roots, int x) {
            if (roots[x] == x) return x;

            return roots[x] = relax(roots, roots[x]);
        }


        class UnionFind {
            int roots[];
            int ranks[];
            int size;

            public UnionFind(int size) {
                this.size = size;
                roots = new int[size];
                ranks = new int[size];
                for (int i = 0; i < size; i++) {
                    roots[i] = i;
                    ranks[i] = 1;
                }
            }

            private int find(int x) {
                if (roots[x] == x) return x;

                return roots[x] = find(roots[x]);
            }

            public void union(int a, int b) {
                int ra = find(a);
                int rb = find(b);
                if (ra == rb) return;

                size--;

                if (ranks[ra] > ranks[rb]) {
                    roots[rb] = ra;
                } else if (ranks[rb] < ranks[ra]) {
                    roots[ra] = rb;
                } else {
                    roots[rb] = ra;
                    ranks[ra]++;
                }
            }
        }
    }
}
