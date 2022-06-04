package io.github.siltal.bluetoothdemo;

import java.util.List;
import java.util.Queue;

public class WaveBuffer {
    private static final int BUFFER_SIZE = 1024;
    Queue<List<Integer>> buffer;

    public WaveBuffer() {
        // TODO 待实现波形控制，将会循环播放强度
    }

    public void add(List<Integer> data) {
        buffer.add(data);
    }

    public List<Integer> get() {
        return buffer.poll();
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }


}
