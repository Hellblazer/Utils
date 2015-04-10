/** (C) Copyright 2010 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.hellblazer.utils.collections;

import java.util.ArrayList;

public class AVLNode<E> {

    AVLNode<E>   parent;
    E            data;
    AVLNode<E>   left;
    AVLNode<E>   right;
    ArrayList<E> myElements = new ArrayList<E>();
    int          height;

    public AVLNode(E data) {
        if (data == null) {
            throw new NullPointerException(
                                           "Passed data to be stored cannot be Null.");
        }
        this.data = data;
        height = 0;
    }

    public AVLNode(E data, AVLNode<E> parent) {
        this.parent = parent;
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    public AVLNode(E data, AVLNode<E> leftS, AVLNode<E> rightS) {
        if (data == null) {
            throw new NullPointerException(
                                           "Passed data to be stored cannot be Null.");
        }
        this.data = data;
        left = leftS;
        right = rightS;
    }

    public AVLNode(E data, AVLNode<E> parent, AVLNode<E> left, AVLNode<E> right) {
        this.parent = parent;
        this.left = left;
        this.right = right;
        height = parent.height + 1;
        if (data == null) {
            throw new NullPointerException(
                                           "Passed data to be stored cannot be Null.");
        }
        this.data = data;
    }

    public int balanceFactor() {
        return heightRChild() - heightLChild();
    }

    public void recalculateHeight() {
        height = Math.max(heightLChild(), heightRChild()) + 1;
    }

    private int heightLChild() {
        return left == null ? 0 : left.height;
    }

    private int heightRChild() {
        return right == null ? 0 : right.height;
    }

    E getData() {
        return this.data;
    }
}
