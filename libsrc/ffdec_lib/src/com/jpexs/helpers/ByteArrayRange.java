/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.helpers;

/**
 *
 * @author JPEXS
 */
public class ByteArrayRange {

    public static final ByteArrayRange EMPTY = new ByteArrayRange(new byte[0]);

    private final byte[] array;
    private final int pos;
    private final int length;

    public ByteArrayRange(byte[] array) {
        this.array = array;
        this.pos = 0;
        this.length = array.length;
    }

    public ByteArrayRange(byte[] array, int pos, int length) {
        this.array = array;
        this.pos = pos;
        this.length = length;
    }

    public byte[] getArray() {
        return array;
    }

    public int getPos() {
        return pos;
    }

    public int getLength() {
        return length;
    }

    public byte get(int index) {
        return array[pos + index];
    }

    public byte[] getRangeData() {
        byte[] data = new byte[length];
        System.arraycopy(array, pos, data, 0, length);
        return data;
    }

    public byte[] getRangeData(int pos, int length) {
        byte[] data = new byte[length];
        System.arraycopy(array, this.pos + pos, data, 0, length);
        return data;
    }
}
