package github.javaguide.test;

import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author ICDM_王顺
 * @Classname HeapSort
 * @Description TODO
 * @Date 2022/6/28 16:25
 * @Created by TheKing_Shun
 */

public class HeapSort {
    private void heapify(int[] arr , int current , int length){
        int l = current * 2 + 1, r = l + 1;
        int maxIdx = current;
        if(l < length && arr[l] > arr[maxIdx]){
            maxIdx = l;
        }
        if(r < length && arr[r] > arr[maxIdx]){
            maxIdx = r;
        }
        if(current != maxIdx){
            swap(arr , current , maxIdx);
            heapify(arr , maxIdx , length);
        }
    }

    public void buildMaxHeap(int[] arr) {
        int lastNode = arr.length - 1;
        int parent = (lastNode - 1) / 2;
        // 由于堆是一个完全二叉树,所以从最后一个非叶节点开始向前构建一个大顶堆
        for (int k = parent; k >= 0; --k) {
            heapify(arr , k  , arr.length);
        }
    }

    public void sort(int[] arr) {
        buildMaxHeap(arr);
        // 砍树
        for (int k = arr.length - 1; k >= 0; k--) {
            // 最后一个值与第一个节点进行交换
            swap(arr ,0 , k);
            // 重新构建一个大顶堆 , 因为砍掉了最后一个节点,所以传入的数组长度就是k!!!
            heapify(arr , 0 , k);
        }

    }
    private void swap(int[] arr , int x ,int y){
        if(x == y)return;
        arr[x] ^= arr[y];
        arr[y] ^= arr[x];
        arr[x] ^= arr[y];
    }

    static class Main {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 1;
            });
            // 这个地方进行阻塞
            System.out.println("1");
            System.out.println("2");
            System.out.println("3");
            System.out.println(future.get());
            System.out.println("4");
            System.out.println("....");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("sss");
                }
            }).start();
        }
    }
}
