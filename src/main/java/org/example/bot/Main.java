package org.example.bot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        new Main().maximumOr(
                new int[] {
                    98,54,6,34,66,63,52,39,62,46,75,28,65,18,37,18,97,13,80,33,69,91,78,19,40
                },
                2
        );
    }

    public long maximumOr(int[] nums, int k) {

        Arrays.sort(nums);
        Set<Pair> longNums = new HashSet<>();
        for (int i = 0; i < nums.length; i++) longNums.add(new Pair(nums[i], i));


        int maxLen = 0;
        int maxNumInd = 0;
        Set<Pair> nextNums = new HashSet<>();

        for (Pair i : longNums) {
            int len = Integer.toBinaryString(i.num).length();
            if (maxLen > len) continue;

            if (maxLen < len) {
                nextNums.clear();
                maxLen = len;
            }
            nextNums.add(new Pair(removeMSB(i.num), i.i));
        }
        if (nextNums.size() == 1) {
            maxNumInd = nextNums.iterator().next().i;
        } else {
            maxNumInd = loop(nextNums);
        }

        long maxNum = nums[maxNumInd];
        long res = maxNum << k;
        for (int i = 0; i < nums.length; i++) {
            if (i == maxNumInd) continue;
            res |= nums[i];
        }
        return res;
    }

    private int loop(Set<Pair> longNums) {
        int maxLen = Integer.MAX_VALUE;
        Set<Pair> nextNums = new HashSet<>();

        for (Pair i : longNums) {
            int len = Integer.toBinaryString(i.num).length();
            if (len > maxLen) continue;

            if (len < maxLen) {
                nextNums.clear();
                maxLen = len;
            }
            nextNums.add(new Pair(removeMSB(i.num), i.i));
        }
        if (nextNums.size() == 1) {
            return nextNums.iterator().next().i;
        } else {
            return loop(nextNums);
        }
    }

    private int removeMSB(int i) {
        int b = 1;
        while (i > (b << 1)) b <<= 1;
        return i - b;
    }

    public static class Pair {
        int num;
        int i;

        public Pair(int num, int i) {
            this.num = num;
            this.i = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (num != pair.num) return false;
            return i == pair.i;
        }

        @Override
        public int hashCode() {
            int result = num;
            result = 31 * result + i;
            return result;
        }
    }
}
