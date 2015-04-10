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
import java.util.Comparator;
import java.util.Iterator;

public class AVLTree<E> implements SortedCollection<E> {
    public class MyIterator implements Iterator<E> {
        private ArrayList<E> list;
        private E            next;

        public MyIterator(ArrayList<E> els) {
            next = null;
            list = els;

        }

        @Override
        public boolean hasNext() {

            return !list.isEmpty();
        }

        @Override
        public E next() {

            if (hasNext()) {
                next = list.remove(0);
                return next;
            }
            return null;
        }

        @Override
        public void remove() {
            AVLTree.this.remove(next);

        }
    }

    static enum Traversal {
        PREORDER, INORDER, POSTORDER, BYLEVEL
    }

    /** The root of the Tree. */

    private AVLNode<E>            root;

    /** Size of the Tree. */

    private int                   size = 0;

    /** The Traversal data member. */

    private Traversal             traversal;

    /** ArrayList to hold the Elements. */

    private ArrayList<E>          myElements;

    /** Comparator used to compare Items in the Tree.. */

    private Comparator<? super E> comp;

    boolean                       contains;

    /**
     * The First Constructor which constructs a new AVLTree where the nodes
     * should be compared and stored obey's the given comparator.
     * 
     * @param comparator
     *            - comparator which will compare the elements would be added to
     *            the AVLTree
     */
    public AVLTree(Comparator<? super E> comparator) {
        root = null;
        if (comparator == null) {
            throw new NullPointerException("Invalid Comparator");
        }
        comp = comparator;

        traversal = Traversal.INORDER;
    }

    /**
     * @param col
     */
    public AVLTree(SortedCollection<E> col) {
        traversal = Traversal.INORDER;
        size = col.size();
        comp = col.comparator();
        for (E element : col) {
            add(element);
        }

    }

    @Override
    public void add(E element) {
        if (element == null) {
            throw new NullPointerException("Invalid element to be added.");
        }
        insert(element, root);

    }

    @Override
    public void addAll(SortedCollection<? extends E> col) {
        if (col == null) {
            throw new NullPointerException(
                                           "Null/Invalid Collection has been passed. ");
        }
        for (E e : col) {
            add(e);
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comp;
    }

    @Override
    public boolean contains(E element) {
        contains = false;
        contains(new AVLNode<E>(element), root);

        return contains;
    }

    @Override
    public E find(E element) {
        if (element == null) {
            throw new NullPointerException(
                                           "Null Element Cannot be Stored in Tree's Nodes");
        }
        if (root == null) {
            throw new NullPointerException("Empty Tree.");
        }
        AVLNode<E> found = findNode(element);
        return found == null ? null : found.data;
    }

    @Override
    public E first() {
        return min(root).data;
    }

    /**
     * Gets the traversal.
     * 
     * @return The Traversal.
     */
    public Traversal getTravel() {

        return this.traversal;
    }

    public int height(AVLNode<E> t) {
        return t == null ? -1 : t.height;
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public Iterator<E> iterator() {

        myElements = new ArrayList<E>();

        switch (traversal) {

            case INORDER: {
                inorder(root);
                return new MyIterator(myElements);
            }
            case PREORDER: {
                preorder(root);
                return new MyIterator(myElements);
            }
            case POSTORDER: {
                postorder(root);
                return new MyIterator(myElements);
            }
            case BYLEVEL: {
                bylevel(root, 0, new ArrayList<ArrayList<E>>());
                return new MyIterator(myElements);
            }
            default: {
                inorder(root);
                return new MyIterator(myElements);
            }
        }
    }

    @Override
    public E last() {
        return max(root).data;
    }

    public AVLNode<E> remove(AVLNode<E> curr, E data) {

        if (curr != null) {
            if (curr.getData() == data) {
                if (curr.getData() == null || curr.right == null) {
                    AVLNode<E> temp = curr.right == null ? curr.left
                                                        : curr.right;
                    curr = null;
                    return temp;
                }
                AVLNode<E> heir = curr.left;
                while (heir.right != null) {
                    heir = heir.right;
                }
                curr.data = heir.getData();
                data = heir.getData();
                if (comp.compare(data, curr.getData()) > 0) {

                    curr.right = remove(curr.right, data);
                    if (getHeight(curr.right) - getHeight(curr.left) == -3) {
                        if (getHeight(curr.left.left) <= getHeight(curr.left.right)) {
                            curr = rotateWithLeftChild(curr);
                        } else {
                            curr = doubleWithLeftChild(curr);
                        }
                    } else {
                        curr.height = Math.max(getHeight(curr.left),
                                               getHeight(curr.right)) + 1;// 
                    }
                } else if (comp.compare(data, curr.getData()) <= 0) {
                    curr.left = remove(curr.left, data);
                    if (getHeight(curr.left) - getHeight(curr.left) == -3) {
                        if (getHeight(curr.right.right) >= getHeight(curr.right.left)) {
                            curr = rotateWithRightChild(curr);
                        } else {
                            curr = doubleWithRightChild(curr);
                        }
                    } else {
                        curr.height = Math.max(getHeight(curr.left),
                                               getHeight(curr.right)) + 1;
                    }
                }
            }
        }
        return curr;

    }

    @Override
    public void remove(E element) {
        if (element == null) {
            throw new IllegalArgumentException();
        }
        remove(root, element);
    }

    /**
     * Sets the given traverser .
     * 
     * @param traverser
     */
    public void setTravel(Traversal traverser) {

        traversal = traverser;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterable<E> subset(E from, E to) {
        if (comp.compare(from, to) >= 0) {
            return new AVLTree<E>(comp);
        }
        AVLTree<E> tree = new AVLTree<E>(comp);
        Iterator<E> itr = this.iterator();
        while (itr.hasNext()) {
            E e = itr.next();
            if (comp.compare(from, e) <= 0 && comp.compare(to, e) > 0) {
                tree.add(e);
            }
        }
        return tree;
    }

    private void bylevel(AVLNode<E> node, int level,
                         ArrayList<ArrayList<E>> listOfArrays) {
        ArrayList<E> levelNodes;
        try {
            levelNodes = listOfArrays.get(level);
        } catch (IndexOutOfBoundsException e) {
            listOfArrays.add(levelNodes = new ArrayList<E>());
        }
        levelNodes.add(node.data);
        level++;
        if (node.left != null) {
            bylevel(node.left, level, listOfArrays);
        }
        if (node.right != null) {
            bylevel(node.right, level, listOfArrays);
        }
        if (node == root) {
            for (ArrayList<E> nodeList : listOfArrays) {
                for (E element : nodeList) {
                    add(element);
                }
            }
        }
    }

    private void contains(AVLNode<E> lookFor, AVLNode<E> currentNode) {

        if (currentNode == null) {
            return;
        }
        if (comp.compare(currentNode.data, lookFor.data) == 0)

        {
            contains = true;
            return;
        } else if (comp.compare(lookFor.data, currentNode.data) < 0)

        {
            contains(lookFor, currentNode.left);
        } else {
            contains(lookFor, currentNode.right);
        }
        return;
    }

    private AVLNode<E> doubleWithLeftChild(AVLNode<E> k3) {
        k3.left = rotateWithRightChild(k3.left);
        return rotateWithLeftChild(k3);
    }

    private AVLNode<E> doubleWithRightChild(AVLNode<E> k1) {
        k1.right = rotateWithLeftChild(k1.right);
        return rotateWithRightChild(k1);
    }

    private AVLNode<E> findNode(E element) {

        AVLNode<E> node = root;
        AVLNode<E> tmp = null;

        do {
            int comarison = comp.compare(element, node.data);
            if (comarison == 0) {
                return node;
            }
            if (comarison >= 0) {
                tmp = node;
                node = node.right;
            } else {
                node = node.left;
            }

        } while (node != null);
        return tmp != null ? tmp : null;
    }

    private int getHeight(AVLNode<E> node) {
        if (node == null) {
            return -1;
        }
        return node.height;
    }

    private void inorder(AVLNode<E> current) {
        if (current != null) {
            inorder(current.left);
            myElements.add(current.data);
            inorder(current.right);
        }
    }

    private AVLNode<E> insert(E x, AVLNode<E> t) {
        if (t == null) {
            {
                t = new AVLNode<E>(x);
                t.parent = null;
            }
            if (size == 0) {
                root = t;
            }
            size++;
        }

        else if (comp.compare(x, t.data) <= 0) {
            t.left = insert(x, t.left);
            if (getHeight(t.left) - getHeight(t.right) == 3) {
                if (comp.compare(x, t.left.data) < 0) {
                    t = rotateWithLeftChild(t);
                } else {
                    t = doubleWithLeftChild(t);
                }
            }
        } else if (comp.compare(x, t.data) > 0) {
            t.right = insert(x, t.right);
            if (getHeight(t.right) - getHeight(t.left) == 3) {
                if (comp.compare(x, t.right.data) > 0) {
                    t = rotateWithRightChild(t);
                } else {
                    t = doubleWithRightChild(t);
                }
            }
        }

        t.height = Math.max(getHeight(t.left), getHeight(t.right)) + 1;
        return t;
    }

    private AVLNode<E> max(AVLNode<E> node) {
        if (isEmpty() || node == null) {
            return null;
        }
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    private AVLNode<E> min(AVLNode<E> node) {
        if (isEmpty() || node == null) {
            return null;
        }
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private void postorder(AVLNode<E> node) {
        if (node != null) {
            postorder(node.left);

            postorder(node.right);
            myElements.add(node.data);
        }
    }

    private void preorder(AVLNode<E> node) {
        if (node != null) {
            myElements.add(node.data);

            preorder(node.left);

            preorder(node.right);
        }
    }

    private AVLNode<E> rotateWithLeftChild(AVLNode<E> k2) {
        AVLNode<E> tempParent = k2.parent;
        AVLNode<E> k1 = k2.left; // k2 will be in position of k1
        {
            if (tempParent != null && tempParent.left == k2) {
                tempParent.left = k1;
            } else if (tempParent != null && tempParent.right == k2) {
                tempParent.right = k1;
            }
        }
        k2.left = k1.right;
        if (k1.right != null) {
            k1.right.parent = k2;
        }
        k1.right = k2;
        k1.parent = k2.parent;
        k2.parent = k1;
        k2.height = Math.max(getHeight(k2.left), getHeight(k2.right)) + 1;
        k1.height = Math.max(getHeight(k1.left), k1.height) + 1;
        if (k2 == root) {
            root = k1;
        }
        return k1;
    }

    private AVLNode<E> rotateWithRightChild(AVLNode<E> k1) {

        AVLNode<E> tempParent = k1.parent;
        AVLNode<E> k2 = k1.right;
        k2.parent = tempParent;

        {
            if (tempParent != null && tempParent.left == k1) {
                tempParent.left = k2;
            } else if (tempParent != null && tempParent.right == k1) {
                tempParent.right = k2;
            }
        }
        k1.right = k2.left;
        if (k2.left != null) {
            k2.left.parent = k1;
        }

        k2.left = k1;
        k1.parent = k2;
        k1.height = Math.max(getHeight(k1.left), getHeight(k1.right)) + 1;
        k2.height = Math.max(getHeight(k2.left), k1.height) + 1;

        if (k1 == root) {
            root = k2;
        }
        return k2;
    }

}
