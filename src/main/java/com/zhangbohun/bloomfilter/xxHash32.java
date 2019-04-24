package com.zhangbohun.bloomfilter;

/**
 * @author zhangbohun
 * Create Date 2019/04/24 17:57
 * Modify Date 2019/04/25 03:06
 */
public class xxHash32 {
    //int显示值无效，仅当作所定义的无符号数使用（使用二进制表示与原版中注释一致）
    private final static int PRIME32_1 = 0b10011110001101110111100110110001;
    private final static int PRIME32_2 = 0b10000101111010111100101001110111;
    private final static int PRIME32_3 = 0b11000010101100101010111000111101;
    private final static int PRIME32_4 = 0b00100111110101001110101100101111;
    private final static int PRIME32_5 = 0b00010110010101100110011110110001;

    //位操作，将value循环左移shift位
    private static int rotl32(int value, int shift) {
        return (value << shift) | (value >>> (32 - shift));
    }

    private static int get32bits(byte[] b, int off) {
        return (((int)b[off + 3] & 0xFF) << 24) | (((int)b[off + 2] & 0xFF) << 16) | (((int)b[off + 1] & 0xFF) << 8) | (
            (int)b[off] & 0xFF);
    }

    public static int hash(String s) {
        return hash(s.getBytes());
    }

    public static int hash(byte[] b) {
        return hash(b, 0);
    }

    public static int hash(byte[] b, int seed) {
        return hash(b, 0, b.length, seed);
    }

    /**
     * 32位值xxhash算法实现，返回值表示32位无符号整数
     * @param b
     * @param seed
     * @return
     */
    public static int hash(byte[] b, int off, int len, int seed) {
        int h32 = 0;
        int p = off;
        int end = off + len;
        if (len >= 16) {
            int v1 = seed + PRIME32_1 + PRIME32_2;
            int v2 = seed + PRIME32_2;
            int v3 = seed + 0;
            int v4 = seed - PRIME32_1;
            for (int limit = end - 16; p <= limit; ) {
                v1 += get32bits(b, p) * PRIME32_2;
                v1 = rotl32(v1, 13);//循环左移13位
                v1 *= PRIME32_1;
                p += 4;

                v2 += get32bits(b, p) * PRIME32_2;
                v2 = rotl32(v2, 13);
                v2 *= PRIME32_1;
                p += 4;

                v3 += get32bits(b, p) * PRIME32_2;
                v3 = rotl32(v3, 13);
                v3 *= PRIME32_1;
                p += 4;

                v4 += get32bits(b, p) * PRIME32_2;
                v4 = rotl32(v4, 13);
                v4 *= PRIME32_1;
                p += 4;
            }
            h32 = rotl32(v1, 1) + rotl32(v2, 7) + rotl32(v3, 12) + Integer.rotateLeft(v4, 18);
        } else {
            h32 = seed + PRIME32_5;
        }
        h32 += len;
        while (p + 4 <= end) {
            h32 += get32bits(b, p) * PRIME32_3;
            h32 = rotl32(h32, 17) * PRIME32_4;
            p += 4;
        }
        while (p < end) {
            h32 += ((int)b[p] & 0xFF) * PRIME32_5;
            h32 = rotl32(h32, 11) * PRIME32_1;
            p += 1;
        }
        /* mix all bits */
        h32 ^= h32 >>> 15;
        h32 *= PRIME32_2;
        h32 ^= h32 >>> 13;
        h32 *= PRIME32_3;
        h32 ^= h32 >>> 16;
        return h32;
    }
}