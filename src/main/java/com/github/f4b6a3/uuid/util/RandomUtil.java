/*
 * MIT License
 * 
 * Copyright (c) 2018-2019 Fabio Lima
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.f4b6a3.uuid.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomUtil {
	
	private RandomUtil() {
	}
	
	public static void nextBytes(byte[] bytes) {
		SecureRandomLazyHolder.INSTANCE.nextBytes(bytes);
	}
	
	public static long nextLong() {
		byte[] bytes = new byte[8];
		SecureRandomLazyHolder.INSTANCE.nextBytes(bytes);
		return ByteUtil.toNumber(bytes);
	}
	
	public static int nextInt(int max) {
		return SecureRandomLazyHolder.INSTANCE.nextInt(max);
	}
	
	public static int nextInt() {
		return SecureRandomLazyHolder.INSTANCE.nextInt();
	}
	
	public static String nextLongHexadecimal() {
		return ByteUtil.toHexadecimal(SecureRandomLazyHolder.INSTANCE.nextLong());
	}
	
	private static class SecureRandomLazyHolder {
		static final Random INSTANCE = new SecureRandom();
	}
}
