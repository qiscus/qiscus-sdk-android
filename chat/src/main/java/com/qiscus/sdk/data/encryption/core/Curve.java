/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.data.encryption.core;
/* Ported from C to Java by Dmitry Skiba [sahn0], 23/02/08.
 * Original: http://cds.xs4all.nl:8081/ecdh/
 */
/* Generic 64-bit integer implementation of Curve25519 ECDH
 * Written by Matthijs van Duin, 200608242056
 * Public domain.
 *
 * Based on work by Daniel J Bernstein, http://cr.yp.to/ecdh.html
 */

public class Curve {

    /* key size */
    public static final int KEY_SIZE = 32;

    /* 0 */
    public static final byte[] ZERO = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /* the prime 2^255-19 */
    public static final byte[] PRIME = {
            (byte) 237, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 127
    };

    /* group order (a prime near 2^252+2^124) */
    public static final byte[] ORDER = {
            (byte) 237, (byte) 211, (byte) 245, (byte) 92,
            (byte) 26, (byte) 99, (byte) 18, (byte) 88,
            (byte) 214, (byte) 156, (byte) 247, (byte) 162,
            (byte) 222, (byte) 249, (byte) 222, (byte) 20,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 16
    };

    /********* KEY AGREEMENT *********/

    /* Private key clamping
     *   k [out] your private key for key agreement
     *   k  [in]  32 random bytes
     */
    public static void clamp(byte[] k) {
        k[31] &= 0x7F;
        k[31] |= 0x40;
        k[0] &= 0xF8;
    }

    /* Key-pair generation
     *   p  [out] your public key
     *   s  [out] your private key for signing
     *   k  [out] your private key for key agreement
     *   k  [in]  32 random bytes
     * s may be NULL if you don't care
     *
     * WARNING: if s is not NULL, this function has data-dependent timing */
    public static void keygen(byte[] p, byte[] s, byte[] k) {
        clamp(k);
        core(p, s, k, null);
    }

    /* Key agreement
     *   z  [out] shared secret (needs hashing before use)
     *   k  [in]  your private key for key agreement
     *   z  [in]  peer's public key
     */
    public static void curve(byte[] z, byte[] k, byte[] p) {
        core(z, null, k, p);
    }

    /********* DIGITAL SIGNATURES *********/

    /* deterministic EC-KCDSA
     *
     *    s is the private key for signing
     *    p is the corresponding public key
     *    p is the context data (signer public key or certificate, etc)
     *
     * signing:
     *
     *    m = hash(z, message)
     *    x = hash(m, s)
     *    keygen25519(Y, NULL, x);
     *    r = hash(Y);
     *    h = m XOR r
     *    sign25519(v, h, x, s);
     *
     *    output (v,r) as the signature
     *
     * verification:
     *
     *    m = hash(z, message);
     *    h = m XOR r
     *    verify25519(Y, v, h, p)
     *
     *    confirm  r == hash(Y)
     *
     * It would seem to me that it would be simpler to have the signer directly do
     * h = hash(m, Y) and send that to the recipient instead of r, who can verify
     * the signature by checking h == hash(m, Y).  If there are any problems with
     * such a scheme, please let me know.
     *
     * Also, EC-KCDSA (like most DS algorithms) picks x random, which is a waste of
     * perfectly good entropy, but does allow Y to be calculated in advance of (or
     * parallel to) hashing the message.
     */

    /* Signature generation primitive, calculates (x-h)s mod q
     *   v  [out] signature value
     *   h  [in]  signature hash (of message, signature pub key, and context data)
     *   x  [in]  signature private key
     *   s  [in]  private key for signing
     * returns true on success, false on failure (use different x or h)
     */
    public static boolean sign(byte[] v, byte[] h, byte[] x, byte[] s) {
        // v = (x - h) s  mod q
        int w, i;
        byte[] h1 = new byte[32], x1 = new byte[32];
        byte[] tmp1 = new byte[64];
        byte[] tmp2 = new byte[64];

        // Don't clobber the arguments, be nice!
        cpy32(h1, h);
        cpy32(x1, x);

        // Reduce modulo group order
        byte[] tmp3 = new byte[32];
        divmod(tmp3, h1, 32, ORDER, 32);
        divmod(tmp3, x1, 32, ORDER, 32);

        // v = x1 - h1
        // If v is negative, add the group order to it to become positive.
        // If v was already positive we don't have to worry about overflow
        // when adding the order because v < ORDER and 2*ORDER < 2^256
        mulaSmall(v, x1, 0, h1, 32, -1);
        mulaSmall(v, v, 0, ORDER, 32, 1);

        // tmp1 = (x-h)*s mod q
        mula32(tmp1, v, s, 32, 1);
        divmod(tmp2, tmp1, 64, ORDER, 32);

        for (w = 0, i = 0; i < 32; i++) {
            w |= v[i] = tmp1[i];
        }
        return w != 0;
    }

    /* Signature verification primitive, calculates y = vp + hg
     *   Y  [out] signature public key
     *   v  [in]  signature value
     *   h  [in]  signature hash
     *   P  [in]  public key
     */
    public static void verify(byte[] y, byte[] v, byte[] h, byte[] publicKey) {
        /* Y = v abs(p) + h G  */
        byte[] d = new byte[32];
        Long10[]
                p = new Long10[]{new Long10(), new Long10()},
                s = new Long10[]{new Long10(), new Long10()},
                yx = new Long10[]{new Long10(), new Long10(), new Long10()},
                yz = new Long10[]{new Long10(), new Long10(), new Long10()},
                t1 = new Long10[]{new Long10(), new Long10(), new Long10()},
                t2 = new Long10[]{new Long10(), new Long10(), new Long10()};

        int vi = 0, hi = 0, di = 0, nvh = 0, i, j, k;

        /* set p[0] to G and p[1] to P  */

        set(p[0], 9);
        unpack(p[1], publicKey);

        /* set s[0] to P+G and s[1] to P-G  */

        /* s[0] = (Py^2 + Gy^2 - 2 Py Gy)/(Px - Gx)^2 - Px - Gx - 486662  */
        /* s[1] = (Py^2 + Gy^2 + 2 Py Gy)/(Px - Gx)^2 - Px - Gx - 486662  */

        xtoy2(t1[0], t2[0], p[1]);    /* t2[0] = Py^2  */
        sqrt(t1[0], t2[0]);    /* t1[0] = Py or -Py  */
        j = isNegative(t1[0]);        /*      ... check which  */
        t2[0].i0 += 39420360;        /* t2[0] = Py^2 + Gy^2  */
        mul(t2[1], BASE_2Y, t1[0]); /* t2[1] = 2 Py Gy or -2 Py Gy  */
        sub(t1[j], t2[0], t2[1]);    /* t1[0] = Py^2 + Gy^2 - 2 Py Gy  */
        add(t1[1 - j], t2[0], t2[1]); /* t1[1] = Py^2 + Gy^2 + 2 Py Gy  */
        cpy(t2[0], p[1]);        /* t2[0] = Px  */
        t2[0].i0 -= 9;            /* t2[0] = Px - Gx  */
        sqr(t2[1], t2[0]);        /* t2[1] = (Px - Gx)^2  */
        recip(t2[0], t2[1], 0);    /* t2[0] = 1/(Px - Gx)^2  */
        mul(s[0], t1[0], t2[0]);    /* s[0] = t1[0]/(Px - Gx)^2  */
        sub(s[0], s[0], p[1]);    /* s[0] = t1[0]/(Px - Gx)^2 - Px  */
        s[0].i0 -= 9 + 486662;        /* s[0] = X(P+G)  */
        mul(s[1], t1[1], t2[0]);    /* s[1] = t1[1]/(Px - Gx)^2  */
        sub(s[1], s[1], p[1]);    /* s[1] = t1[1]/(Px - Gx)^2 - Px  */
        s[1].i0 -= 9 + 486662;        /* s[1] = X(P-G)  */
        mulSmall(s[0], s[0], 1);    /* reduce s[0] */
        mulSmall(s[1], s[1], 1);    /* reduce s[1] */


        /* prepare the chain  */
        for (i = 0; i < 32; i++) {
            vi = (vi >> 8) ^ (v[i] & 0xFF) ^ ((v[i] & 0xFF) << 1);
            hi = (hi >> 8) ^ (h[i] & 0xFF) ^ ((h[i] & 0xFF) << 1);
            nvh = ~(vi ^ hi);
            di = (nvh & (di & 0x80) >> 7) ^ vi;
            di ^= nvh & (di & 0x01) << 1;
            di ^= nvh & (di & 0x02) << 1;
            di ^= nvh & (di & 0x04) << 1;
            di ^= nvh & (di & 0x08) << 1;
            di ^= nvh & (di & 0x10) << 1;
            di ^= nvh & (di & 0x20) << 1;
            di ^= nvh & (di & 0x40) << 1;
            d[i] = (byte) di;
        }

        di = ((nvh & (di & 0x80) << 1) ^ vi) >> 8;

        /* initialize state */
        set(yx[0], 1);
        cpy(yx[1], p[di]);
        cpy(yx[2], s[0]);
        set(yz[0], 0);
        set(yz[1], 1);
        set(yz[2], 1);

        /* y[0] is (even)P + (even)G
         * y[1] is (even)P + (odd)G  if current d-bit is 0
         * y[1] is (odd)P + (even)G  if current d-bit is 1
         * y[2] is (odd)P + (odd)G
         */

        vi = 0;
        hi = 0;

        /* and go for it! */
        for (i = 32; i-- != 0; ) {
            vi = (vi << 8) | (v[i] & 0xFF);
            hi = (hi << 8) | (h[i] & 0xFF);
            di = (di << 8) | (d[i] & 0xFF);

            for (j = 8; j-- != 0; ) {
                montPrep(t1[0], t2[0], yx[0], yz[0]);
                montPrep(t1[1], t2[1], yx[1], yz[1]);
                montPrep(t1[2], t2[2], yx[2], yz[2]);

                k = ((vi ^ vi >> 1) >> j & 1)
                        + ((hi ^ hi >> 1) >> j & 1);
                montDbl(yx[2], yz[2], t1[k], t2[k], yx[0], yz[0]);

                k = (di >> j & 2) ^ ((di >> j & 1) << 1);
                montAdd(t1[1], t2[1], t1[k], t2[k], yx[1], yz[1],
                        p[di >> j & 1]);

                montAdd(t1[2], t2[2], t1[0], t2[0], yx[2], yz[2],
                        s[((vi ^ hi) >> j & 2) >> 1]);
            }
        }

        k = (vi & 1) + (hi & 1);
        recip(t1[0], yz[k], 0);
        mul(t1[1], yx[k], t1[0]);

        pack(t1[1], y);
    }

    ///////////////////////////////////////////////////////////////////////////

    /* sahn0:
     * Using this class instead of long[10] to avoid bounds checks. */
    private static final class Long10 {
        public Long10() {
        }

        public Long10(
                long i0, long i1, long i2, long i3, long i4,
                long i5, long i6, long i7, long i8, long i9) {
            this.i0 = i0;
            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
            this.i4 = i4;
            this.i5 = i5;
            this.i6 = i6;
            this.i7 = i7;
            this.i8 = i8;
            this.i9 = i9;
        }

        private long i0, i1, i2, i3, i4, i5, i6, i7, i8, i9;
    }

    /********************* radix 2^8 math *********************/

    private static void cpy32(byte[] d, byte[] s) {
        int i;
        for (i = 0; i < 32; i++) {
            d[i] = s[i];
        }
    }

    /* p[m..n+m-1] = q[m..n+m-1] + z * x */
    /* n is the size of x */
    /* n+m is the size of p and q */
    private static int mulaSmall(byte[] p, byte[] q, int m, byte[] x, int n, int z) {
        int v = 0;
        for (int i = 0; i < n; ++i) {
            v += (q[i + m] & 0xFF) + z * (x[i] & 0xFF);
            p[i + m] = (byte) v;
            v >>= 8;
        }
        return v;
    }

    /* p += x * y * z  where z is a small integer
     * x is size 32, y is size t, p is size 32+t
     * y is allowed to overlap with p+32 if you don't care about the upper half  */
    private static int mula32(byte[] p, byte[] x, byte[] y, int t, int z) {
        final int n = 31;
        int w = 0;
        int i = 0;
        for (; i < t; i++) {
            int zy = z * (y[i] & 0xFF);
            w += mulaSmall(p, p, i, x, n, zy) +
                    (p[i + n] & 0xFF) + zy * (x[n] & 0xFF);
            p[i + n] = (byte) w;
            w >>= 8;
        }
        p[i + n] = (byte) (w + (p[i + n] & 0xFF));
        return w >> 8;
    }

    /* divide r (size n) by d (size t), returning quotient q and remainder r
     * quotient is size n-t+1, remainder is size t
     * requires t > 0 && d[t-1] != 0
     * requires that r[-1] and d[-1] are valid memory locations
     * q may overlap with r+t */
    private static void divmod(byte[] q, byte[] r, int n, byte[] d, int t) {
        int rn = 0;
        int dt = ((d[t - 1] & 0xFF) << 8);
        if (t > 1) {
            dt |= (d[t - 2] & 0xFF);
        }
        while (n-- >= t) {
            int z = (rn << 16) | ((r[n] & 0xFF) << 8);
            if (n > 0) {
                z |= (r[n - 1] & 0xFF);
            }
            z /= dt;
            rn += mulaSmall(r, r, n - t + 1, d, t, -z);
            q[n - t + 1] = (byte) ((z + rn) & 0xFF); /* rn is 0 or -1 (underflow) */
            mulaSmall(r, r, n - t + 1, d, t, -rn);
            rn = (r[n] & 0xFF);
            r[n] = 0;
        }
        r[t - 1] = (byte) rn;
    }

    private static int numsize(byte[] x, int n) {
        while (n-- != 0 && x[n] == 0) {

        }
        return n + 1;
    }

    /* Returns x if a contains the gcd, y if b.
     * Also, the returned buffer contains the inverse of a mod b,
     * as 32-byte signed.
     * x and y must have 64 bytes space for temporary use.
     * requires that a[-1] and b[-1] are valid memory locations  */
    private static byte[] egcd32(byte[] x, byte[] y, byte[] a, byte[] b) {
        int an, bn = 32, qn, i;
        for (i = 0; i < 32; i++) {
            x[i] = y[i] = 0;
        }
        x[0] = 1;
        an = numsize(a, 32);
        if (an == 0) {
            return y;    /* division by zero */
        }
        byte[] temp = new byte[32];
        while (true) {
            qn = bn - an + 1;
            divmod(temp, b, bn, a, an);
            bn = numsize(b, bn);
            if (bn == 0)
                return x;
            mula32(y, x, temp, qn, -1);

            qn = an - bn + 1;
            divmod(temp, a, an, b, bn);
            an = numsize(a, an);
            if (an == 0)
                return y;
            mula32(x, y, temp, qn, -1);
        }
    }

    /********************* radix 2^25.5 GF(2^255-19) math *********************/

    private static final int P25 = 33554431;    /* (1 << 25) - 1 */
    private static final int P26 = 67108863;    /* (1 << 26) - 1 */

    /* Convert to internal format from little-endian byte format */
    private static void unpack(Long10 x, byte[] m) {
        x.i0 = ((m[0] & 0xFF)) | ((m[1] & 0xFF)) << 8 |
                (m[2] & 0xFF) << 16 | ((m[3] & 0xFF) & 3) << 24;
        x.i1 = ((m[3] & 0xFF) & ~3) >> 2 | (m[4] & 0xFF) << 6 |
                (m[5] & 0xFF) << 14 | ((m[6] & 0xFF) & 7) << 22;
        x.i2 = ((m[6] & 0xFF) & ~7) >> 3 | (m[7] & 0xFF) << 5 |
                (m[8] & 0xFF) << 13 | ((m[9] & 0xFF) & 31) << 21;
        x.i3 = ((m[9] & 0xFF) & ~31) >> 5 | (m[10] & 0xFF) << 3 |
                (m[11] & 0xFF) << 11 | ((m[12] & 0xFF) & 63) << 19;
        x.i4 = ((m[12] & 0xFF) & ~63) >> 6 | (m[13] & 0xFF) << 2 |
                (m[14] & 0xFF) << 10 | (m[15] & 0xFF) << 18;
        x.i5 = (m[16] & 0xFF) | (m[17] & 0xFF) << 8 |
                (m[18] & 0xFF) << 16 | ((m[19] & 0xFF) & 1) << 24;
        x.i6 = ((m[19] & 0xFF) & ~1) >> 1 | (m[20] & 0xFF) << 7 |
                (m[21] & 0xFF) << 15 | ((m[22] & 0xFF) & 7) << 23;
        x.i7 = ((m[22] & 0xFF) & ~7) >> 3 | (m[23] & 0xFF) << 5 |
                (m[24] & 0xFF) << 13 | ((m[25] & 0xFF) & 15) << 21;
        x.i8 = ((m[25] & 0xFF) & ~15) >> 4 | (m[26] & 0xFF) << 4 |
                (m[27] & 0xFF) << 12 | ((m[28] & 0xFF) & 63) << 20;
        x.i9 = ((m[28] & 0xFF) & ~63) >> 6 | (m[29] & 0xFF) << 2 |
                (m[30] & 0xFF) << 10 | (m[31] & 0xFF) << 18;
    }

    /* Check if reduced-form input >= 2^255-19 */
    private static boolean isOverflow(Long10 x) {
        return (
                ((x.i0 > P26 - 19)) &&
                        ((x.i1 & x.i3 & x.i5 & x.i7 & x.i9) == P25) &&
                        ((x.i2 & x.i4 & x.i6 & x.i8) == P26)
        ) || (x.i9 > P25);
    }

    /* Convert from internal format to little-endian byte format.  The
     * number must be in a reduced form which is output by the following ops:
     *     unpack, mul, sqr
     *     set --  if input in range 0 .. P25
     * If you're unsure if the number is reduced, first multiply it by 1.  */
    private static void pack(Long10 x, byte[] m) {
        int ld = 0, ud = 0;
        long t;
        ld = (isOverflow(x) ? 1 : 0) - ((x.i9 < 0) ? 1 : 0);
        ud = ld * -(P25 + 1);
        ld *= 19;
        t = ld + x.i0 + (x.i1 << 26);
        m[0] = (byte) t;
        m[1] = (byte) (t >> 8);
        m[2] = (byte) (t >> 16);
        m[3] = (byte) (t >> 24);
        t = (t >> 32) + (x.i2 << 19);
        m[4] = (byte) t;
        m[5] = (byte) (t >> 8);
        m[6] = (byte) (t >> 16);
        m[7] = (byte) (t >> 24);
        t = (t >> 32) + (x.i3 << 13);
        m[8] = (byte) t;
        m[9] = (byte) (t >> 8);
        m[10] = (byte) (t >> 16);
        m[11] = (byte) (t >> 24);
        t = (t >> 32) + (x.i4 << 6);
        m[12] = (byte) t;
        m[13] = (byte) (t >> 8);
        m[14] = (byte) (t >> 16);
        m[15] = (byte) (t >> 24);
        t = (t >> 32) + x.i5 + (x.i6 << 25);
        m[16] = (byte) t;
        m[17] = (byte) (t >> 8);
        m[18] = (byte) (t >> 16);
        m[19] = (byte) (t >> 24);
        t = (t >> 32) + (x.i7 << 19);
        m[20] = (byte) t;
        m[21] = (byte) (t >> 8);
        m[22] = (byte) (t >> 16);
        m[23] = (byte) (t >> 24);
        t = (t >> 32) + (x.i8 << 12);
        m[24] = (byte) t;
        m[25] = (byte) (t >> 8);
        m[26] = (byte) (t >> 16);
        m[27] = (byte) (t >> 24);
        t = (t >> 32) + ((x.i9 + ud) << 6);
        m[28] = (byte) t;
        m[29] = (byte) (t >> 8);
        m[30] = (byte) (t >> 16);
        m[31] = (byte) (t >> 24);
    }

    /* Copy a number */
    private static void cpy(Long10 out, Long10 in) {
        out.i0 = in.i0;
        out.i1 = in.i1;
        out.i2 = in.i2;
        out.i3 = in.i3;
        out.i4 = in.i4;
        out.i5 = in.i5;
        out.i6 = in.i6;
        out.i7 = in.i7;
        out.i8 = in.i8;
        out.i9 = in.i9;
    }

    /* Set a number to value, which must be in range -185861411 .. 185861411 */
    private static void set(Long10 out, int in) {
        out.i0 = in;
        out.i1 = 0;
        out.i2 = 0;
        out.i3 = 0;
        out.i4 = 0;
        out.i5 = 0;
        out.i6 = 0;
        out.i7 = 0;
        out.i8 = 0;
        out.i9 = 0;
    }

    /* Add/subtract two numbers.  The inputs must be in reduced form, and the
     * output isn't, so to do another addition or subtraction on the output,
     * first multiply it by one to reduce it. */
    private static void add(Long10 xy, Long10 x, Long10 y) {
        xy.i0 = x.i0 + y.i0;
        xy.i1 = x.i1 + y.i1;
        xy.i2 = x.i2 + y.i2;
        xy.i3 = x.i3 + y.i3;
        xy.i4 = x.i4 + y.i4;
        xy.i5 = x.i5 + y.i5;
        xy.i6 = x.i6 + y.i6;
        xy.i7 = x.i7 + y.i7;
        xy.i8 = x.i8 + y.i8;
        xy.i9 = x.i9 + y.i9;
    }

    private static void sub(Long10 xy, Long10 x, Long10 y) {
        xy.i0 = x.i0 - y.i0;
        xy.i1 = x.i1 - y.i1;
        xy.i2 = x.i2 - y.i2;
        xy.i3 = x.i3 - y.i3;
        xy.i4 = x.i4 - y.i4;
        xy.i5 = x.i5 - y.i5;
        xy.i6 = x.i6 - y.i6;
        xy.i7 = x.i7 - y.i7;
        xy.i8 = x.i8 - y.i8;
        xy.i9 = x.i9 - y.i9;
    }

    /* Multiply a number by a small integer in range -185861411 .. 185861411.
     * The output is in reduced form, the input x need not be.  x and xy may point
     * to the same buffer. */
    private static Long10 mulSmall(Long10 xy, Long10 x, long y) {
        long t;
        t = (x.i8 * y);
        xy.i8 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x.i9 * y);
        xy.i9 = (t & ((1 << 25) - 1));
        t = 19 * (t >> 25) + (x.i0 * y);
        xy.i0 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x.i1 * y);
        xy.i1 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x.i2 * y);
        xy.i2 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x.i3 * y);
        xy.i3 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x.i4 * y);
        xy.i4 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x.i5 * y);
        xy.i5 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x.i6 * y);
        xy.i6 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x.i7 * y);
        xy.i7 = (t & ((1 << 25) - 1));
        t = (t >> 25) + xy.i8;
        xy.i8 = (t & ((1 << 26) - 1));
        xy.i9 += (t >> 26);
        return xy;
    }

    /* Multiply two numbers.  The output is in reduced form, the inputs need not
     * be. */
    private static Long10 mul(Long10 xy, Long10 x, Long10 y) {
        /* sahn0:
         * Using local variables to avoid class access.
         * This seem to improve performance a bit...
         */
        long
                x0 = x.i0, x1 = x.i1, x2 = x.i2, x3 = x.i3, x4 = x.i4,
                x5 = x.i5, x6 = x.i6, x7 = x.i7, x8 = x.i8, x9 = x.i9;
        long
                y0 = y.i0, y1 = y.i1, y2 = y.i2, y3 = y.i3, y4 = y.i4,
                y5 = y.i5, y6 = y.i6, y7 = y.i7, y8 = y.i8, y9 = y.i9;
        long t;
        t = (x0 * y8) + (x2 * y6) + (x4 * y4) + (x6 * y2) +
                (x8 * y0) + 2 * ((x1 * y7) + (x3 * y5) +
                (x5 * y3) + (x7 * y1)) + 38 *
                (x9 * y9);
        xy.i8 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x0 * y9) + (x1 * y8) + (x2 * y7) +
                (x3 * y6) + (x4 * y5) + (x5 * y4) +
                (x6 * y3) + (x7 * y2) + (x8 * y1) +
                (x9 * y0);
        xy.i9 = (t & ((1 << 25) - 1));
        t = (x0 * y0) + 19 * ((t >> 25) + (x2 * y8) + (x4 * y6)
                + (x6 * y4) + (x8 * y2)) + 38 *
                ((x1 * y9) + (x3 * y7) + (x5 * y5) +
                        (x7 * y3) + (x9 * y1));
        xy.i0 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x0 * y1) + (x1 * y0) + 19 * ((x2 * y9)
                + (x3 * y8) + (x4 * y7) + (x5 * y6) +
                (x6 * y5) + (x7 * y4) + (x8 * y3) +
                (x9 * y2));
        xy.i1 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x0 * y2) + (x2 * y0) + 19 * ((x4 * y8)
                + (x6 * y6) + (x8 * y4)) + 2 * (x1 * y1)
                + 38 * ((x3 * y9) + (x5 * y7) +
                (x7 * y5) + (x9 * y3));
        xy.i2 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x0 * y3) + (x1 * y2) + (x2 * y1) +
                (x3 * y0) + 19 * ((x4 * y9) + (x5 * y8) +
                (x6 * y7) + (x7 * y6) +
                (x8 * y5) + (x9 * y4));
        xy.i3 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x0 * y4) + (x2 * y2) + (x4 * y0) + 19 *
                ((x6 * y8) + (x8 * y6)) + 2 * ((x1 * y3) +
                (x3 * y1)) + 38 *
                ((x5 * y9) + (x7 * y7) + (x9 * y5));
        xy.i4 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x0 * y5) + (x1 * y4) + (x2 * y3) +
                (x3 * y2) + (x4 * y1) + (x5 * y0) + 19 *
                ((x6 * y9) + (x7 * y8) + (x8 * y7) +
                        (x9 * y6));
        xy.i5 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x0 * y6) + (x2 * y4) + (x4 * y2) +
                (x6 * y0) + 19 * (x8 * y8) + 2 * ((x1 * y5) +
                (x3 * y3) + (x5 * y1)) + 38 *
                ((x7 * y9) + (x9 * y7));
        xy.i6 = (t & ((1 << 26) - 1));
        t = (t >> 26) + (x0 * y7) + (x1 * y6) + (x2 * y5) +
                (x3 * y4) + (x4 * y3) + (x5 * y2) +
                (x6 * y1) + (x7 * y0) + 19 * ((x8 * y9) +
                (x9 * y8));
        xy.i7 = (t & ((1 << 25) - 1));
        t = (t >> 25) + xy.i8;
        xy.i8 = (t & ((1 << 26) - 1));
        xy.i9 += (t >> 26);
        return xy;
    }

    /* Square a number.  Optimization of  mul25519(x2, x, x)  */
    private static Long10 sqr(Long10 y, Long10 x) {
        long
                x0 = x.i0, x1 = x.i1, x2 = x.i2, x3 = x.i3, x4 = x.i4,
                x5 = x.i5, x6 = x.i6, x7 = x.i7, x8 = x.i8, x9 = x.i9;
        long t;
        t = (x4 * x4) + 2 * ((x0 * x8) + (x2 * x6)) + 38 *
                (x9 * x9) + 4 * ((x1 * x7) + (x3 * x5));
        y.i8 = (t & ((1 << 26) - 1));
        t = (t >> 26) + 2 * ((x0 * x9) + (x1 * x8) + (x2 * x7) +
                (x3 * x6) + (x4 * x5));
        y.i9 = (t & ((1 << 25) - 1));
        t = 19 * (t >> 25) + (x0 * x0) + 38 * ((x2 * x8) +
                (x4 * x6) + (x5 * x5)) + 76 * ((x1 * x9)
                + (x3 * x7));
        y.i0 = (t & ((1 << 26) - 1));
        t = (t >> 26) + 2 * (x0 * x1) + 38 * ((x2 * x9) +
                (x3 * x8) + (x4 * x7) + (x5 * x6));
        y.i1 = (t & ((1 << 25) - 1));
        t = (t >> 25) + 19 * (x6 * x6) + 2 * ((x0 * x2) +
                (x1 * x1)) + 38 * (x4 * x8) + 76 *
                ((x3 * x9) + (x5 * x7));
        y.i2 = (t & ((1 << 26) - 1));
        t = (t >> 26) + 2 * ((x0 * x3) + (x1 * x2)) + 38 *
                ((x4 * x9) + (x5 * x8) + (x6 * x7));
        y.i3 = (t & ((1 << 25) - 1));
        t = (t >> 25) + (x2 * x2) + 2 * (x0 * x4) + 38 *
                ((x6 * x8) + (x7 * x7)) + 4 * (x1 * x3) + 76 *
                (x5 * x9);
        y.i4 = (t & ((1 << 26) - 1));
        t = (t >> 26) + 2 * ((x0 * x5) + (x1 * x4) + (x2 * x3))
                + 38 * ((x6 * x9) + (x7 * x8));
        y.i5 = (t & ((1 << 25) - 1));
        t = (t >> 25) + 19 * (x8 * x8) + 2 * ((x0 * x6) +
                (x2 * x4) + (x3 * x3)) + 4 * (x1 * x5) +
                76 * (x7 * x9);
        y.i6 = (t & ((1 << 26) - 1));
        t = (t >> 26) + 2 * ((x0 * x7) + (x1 * x6) + (x2 * x5) +
                (x3 * x4)) + 38 * (x8 * x9);
        y.i7 = (t & ((1 << 25) - 1));
        t = (t >> 25) + y.i8;
        y.i8 = (t & ((1 << 26) - 1));
        y.i9 += (t >> 26);
        return y;
    }

    /* Calculates a reciprocal.  The output is in reduced form, the inputs need not
     * be.  Simply calculates  y = x^(p-2)  so it's not too fast. */
    /* When sqrtassist is true, it instead calculates y = x^((p-5)/8) */
    private static void recip(Long10 y, Long10 x, int sqrtassist) {
        Long10
                t0 = new Long10(),
                t1 = new Long10(),
                t2 = new Long10(),
                t3 = new Long10(),
                t4 = new Long10();
        int i;
        /* the chain for x^(2^255-21) is straight from djb's implementation */
        sqr(t1, x);    /*  2 == 2 * 1    */
        sqr(t2, t1);    /*  4 == 2 * 2    */
        sqr(t0, t2);    /*  8 == 2 * 4    */
        mul(t2, t0, x);    /*  9 == 8 + 1    */
        mul(t0, t2, t1);    /* 11 == 9 + 2    */
        sqr(t1, t0);    /* 22 == 2 * 11    */
        mul(t3, t1, t2);    /* 31 == 22 + 9
                    == 2^5   - 2^0    */
        sqr(t1, t3);    /* 2^6   - 2^1    */
        sqr(t2, t1);    /* 2^7   - 2^2    */
        sqr(t1, t2);    /* 2^8   - 2^3    */
        sqr(t2, t1);    /* 2^9   - 2^4    */
        sqr(t1, t2);    /* 2^10  - 2^5    */
        mul(t2, t1, t3);    /* 2^10  - 2^0    */
        sqr(t1, t2);    /* 2^11  - 2^1    */
        sqr(t3, t1);    /* 2^12  - 2^2    */
        for (i = 1; i < 5; i++) {
            sqr(t1, t3);
            sqr(t3, t1);
        } /* t3 */        /* 2^20  - 2^10    */
        mul(t1, t3, t2);    /* 2^20  - 2^0    */
        sqr(t3, t1);    /* 2^21  - 2^1    */
        sqr(t4, t3);    /* 2^22  - 2^2    */
        for (i = 1; i < 10; i++) {
            sqr(t3, t4);
            sqr(t4, t3);
        } /* t4 */        /* 2^40  - 2^20    */
        mul(t3, t4, t1);    /* 2^40  - 2^0    */
        for (i = 0; i < 5; i++) {
            sqr(t1, t3);
            sqr(t3, t1);
        } /* t3 */        /* 2^50  - 2^10    */
        mul(t1, t3, t2);    /* 2^50  - 2^0    */
        sqr(t2, t1);    /* 2^51  - 2^1    */
        sqr(t3, t2);    /* 2^52  - 2^2    */
        for (i = 1; i < 25; i++) {
            sqr(t2, t3);
            sqr(t3, t2);
        } /* t3 */        /* 2^100 - 2^50 */
        mul(t2, t3, t1);    /* 2^100 - 2^0    */
        sqr(t3, t2);    /* 2^101 - 2^1    */
        sqr(t4, t3);    /* 2^102 - 2^2    */
        for (i = 1; i < 50; i++) {
            sqr(t3, t4);
            sqr(t4, t3);
        } /* t4 */        /* 2^200 - 2^100 */
        mul(t3, t4, t2);    /* 2^200 - 2^0    */
        for (i = 0; i < 25; i++) {
            sqr(t4, t3);
            sqr(t3, t4);
        } /* t3 */        /* 2^250 - 2^50    */
        mul(t2, t3, t1);    /* 2^250 - 2^0    */
        sqr(t1, t2);    /* 2^251 - 2^1    */
        sqr(t2, t1);    /* 2^252 - 2^2    */
        if (sqrtassist != 0) {
            mul(y, x, t2);    /* 2^252 - 3 */
        } else {
            sqr(t1, t2);    /* 2^253 - 2^3    */
            sqr(t2, t1);    /* 2^254 - 2^4    */
            sqr(t1, t2);    /* 2^255 - 2^5    */
            mul(y, t1, t0);    /* 2^255 - 21    */
        }
    }

    /* checks if x is "negative", requires reduced input */
    private static int isNegative(Long10 x) {
        return (int) (((isOverflow(x) || (x.i9 < 0)) ? 1 : 0) ^ (x.i0 & 1));
    }

    /* a square root */
    private static void sqrt(Long10 x, Long10 u) {
        Long10 v = new Long10(), t1 = new Long10(), t2 = new Long10();
        add(t1, u, u);    /* t1 = 2u        */
        recip(v, t1, 1);    /* v = (2u)^((p-5)/8)    */
        sqr(x, v);        /* x = v^2        */
        mul(t2, t1, x);    /* t2 = 2uv^2        */
        t2.i0--;        /* t2 = 2uv^2-1        */
        mul(t1, v, t2);    /* t1 = v(2uv^2-1)    */
        mul(x, u, t1);    /* x = uv(2uv^2-1)    */
    }

    /********************* Elliptic curve *********************/

    /* y^2 = x^3 + 486662 x^2 + x  over GF(2^255-19) */

    /* t1 = ax + az
     * t2 = ax - az  */
    private static void montPrep(Long10 t1, Long10 t2, Long10 ax, Long10 az) {
        add(t1, ax, az);
        sub(t2, ax, az);
    }

    /* A = P + Q   where
     *  X(A) = ax/az
     *  X(P) = (t1+t2)/(t1-t2)
     *  X(Q) = (t3+t4)/(t3-t4)
     *  X(P-Q) = dx
     * clobbers t1 and t2, preserves t3 and t4  */
    private static void montAdd(Long10 t1, Long10 t2, Long10 t3, Long10 t4, Long10 ax, Long10 az, Long10 dx) {
        mul(ax, t2, t3);
        mul(az, t1, t4);
        add(t1, ax, az);
        sub(t2, ax, az);
        sqr(ax, t1);
        sqr(t1, t2);
        mul(az, t1, dx);
    }

    /* B = 2 * Q   where
     *  X(B) = bx/bz
     *  X(Q) = (t3+t4)/(t3-t4)
     * clobbers t1 and t2, preserves t3 and t4  */
    private static void montDbl(Long10 t1, Long10 t2, Long10 t3, Long10 t4, Long10 bx, Long10 bz) {
        sqr(t1, t3);
        sqr(t2, t4);
        mul(bx, t1, t2);
        sub(t2, t1, t2);
        mulSmall(bz, t2, 121665);
        add(t1, t1, bz);
        mul(bz, t1, t2);
    }

    /* Y^2 = X^3 + 486662 X^2 + X
     * t is a temporary  */
    private static void xtoy2(Long10 t, Long10 y2, Long10 x) {
        sqr(t, x);
        mulSmall(y2, x, 486662);
        add(t, t, y2);
        t.i0++;
        mul(y2, t, x);
    }

    /* P = kG   and  s = sign(P)/k  */
    private static void core(byte[] px, byte[] s, byte[] k, byte[] gx) {
        Long10
                dx = new Long10(),
                t1 = new Long10(),
                t2 = new Long10(),
                t3 = new Long10(),
                t4 = new Long10();
        Long10[]
                x = new Long10[]{new Long10(), new Long10()},
                z = new Long10[]{new Long10(), new Long10()};
        int i, j;

        /* unpack the base */
        if (gx != null) {
            unpack(dx, gx);
        } else {
            set(dx, 9);
        }

        /* 0G = point-at-infinity */
        set(x[0], 1);
        set(z[0], 0);

        /* 1G = G */
        cpy(x[1], dx);
        set(z[1], 1);

        for (i = 32; i-- != 0; ) {
            if (i == 0) {
                i = 0;
            }
            for (j = 8; j-- != 0; ) {
                /* swap arguments depending on bit */
                int bit1 = (k[i] & 0xFF) >> j & 1;
                int bit0 = ~(k[i] & 0xFF) >> j & 1;
                Long10 ax = x[bit0];
                Long10 az = z[bit0];
                Long10 bx = x[bit1];
                Long10 bz = z[bit1];

                /* a' = a + b    */
                /* b' = 2 b    */
                montPrep(t1, t2, ax, az);
                montPrep(t3, t4, bx, bz);
                montAdd(t1, t2, t3, t4, ax, az, dx);
                montDbl(t1, t2, t3, t4, bx, bz);
            }
        }

        recip(t1, z[0], 0);
        mul(dx, x[0], t1);
        pack(dx, px);

        /* calculate s such that s abs(P) = G  .. assumes G is std base point */
        if (s != null) {
            xtoy2(t2, t1, dx);    /* t1 = Py^2  */
            recip(t3, z[1], 0);    /* where Q=P+G ... */
            mul(t2, x[1], t3);    /* t2 = Qx  */
            add(t2, t2, dx);    /* t2 = Qx + Px  */
            t2.i0 += 9 + 486662;    /* t2 = Qx + Px + Gx + 486662  */
            dx.i0 -= 9;        /* dx = Px - Gx  */
            sqr(t3, dx);    /* t3 = (Px - Gx)^2  */
            mul(dx, t2, t3);    /* dx = t2 (Px - Gx)^2  */
            sub(dx, dx, t1);    /* dx = t2 (Px - Gx)^2 - Py^2  */
            dx.i0 -= 39420360;    /* dx = t2 (Px - Gx)^2 - Py^2 - Gy^2  */
            mul(t1, dx, BASE_R2Y);    /* t1 = -Py  */
            if (isNegative(t1) != 0) {    /* sign is 1, so just copy  */
                cpy32(s, k);
            } else {            /* sign is -1, so negate  */
                mulaSmall(s, ORDER_TIMES_8, 0, k, 32, -1);
            }

            /* reduce s mod q
             * (is this needed?  do it just in case, it's fast anyway) */
            //divmod((dstptr) t1, s, 32, order25519, 32);

            /* take reciprocal of s mod q */
            byte[] temp1 = new byte[32];
            byte[] temp2 = new byte[64];
            byte[] temp3 = new byte[64];
            cpy32(temp1, ORDER);
            cpy32(s, egcd32(temp2, temp3, s, temp1));
            if ((s[31] & 0x80) != 0) {
                mulaSmall(s, s, 0, ORDER, 32, 1);
            }
        }
    }

    /* smallest multiple of the order that's >= 2^255 */
    private static final byte[] ORDER_TIMES_8 = {
            (byte) 104, (byte) 159, (byte) 174, (byte) 231,
            (byte) 210, (byte) 24, (byte) 147, (byte) 192,
            (byte) 178, (byte) 230, (byte) 188, (byte) 23,
            (byte) 245, (byte) 206, (byte) 247, (byte) 166,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 128
    };

    /* constants 2Gy and 1/(2Gy) */
    private static final Long10 BASE_2Y = new Long10(
            39999547, 18689728, 59995525, 1648697, 57546132,
            24010086, 19059592, 5425144, 63499247, 16420658
    );
    private static final Long10 BASE_R2Y = new Long10(
            5744, 8160848, 4790893, 13779497, 35730846,
            12541209, 49101323, 30047407, 40071253, 6226132
    );
}
