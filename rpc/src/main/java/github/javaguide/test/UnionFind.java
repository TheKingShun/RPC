package github.javaguide.test;

import sun.font.TrueTypeFont;

import java.util.Arrays;

/**
 * @author ICDM_王顺
 * @Classname UnionFind
 * @Description TODO
 * @Date 2022/7/12 11:29
 * @Created by TheKing_Shun
 */

class UnionFind {
    int[] parents;
    int[] size;
    int n;
    int setCount;

    UnionFind(int n) {
        this.n = n;
        parents = new int[n];
        size = new int[n];
        setCount = n;
        Arrays.fill(size, 1);
        for(int i = 0 ; i < n ; ++i){
            parents[i] = i;
        }
    }

    public  int find(int x) {
        if (x != parents[x]) {
            parents[x] = find(parents[x]);
        }
        return parents[x];
    }

    public boolean union(int x, int y) {
        int i = find(x);
        int j = find(y);
        if(i == j)return false;
        setCount--;
        if (size[i] > size[j]) {
            parents[j] = i;
            size[i] += size[j];
        }else{
            parents[i] = j;
            size[j] += size[i];
        }
        return true;
    }

    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    public static void main(String[] args) {
        UnionFind unionFind = new UnionFind(10);
        System.out.println(unionFind.connected(0, 1));
        unionFind.union(0, 1);
        System.out.println(unionFind.connected(0, 1));

    }
}
