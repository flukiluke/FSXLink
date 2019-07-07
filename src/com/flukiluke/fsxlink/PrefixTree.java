package com.flukiluke.fsxlink;

import java.util.HashMap;
import java.util.Map;

public class PrefixTree<T> {
    private PrefixTreeNode rootNode = new PrefixTreeNode();

    public void add(String key, T object) {
        PrefixTreeNode node = rootNode;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (node.getObject() != null) {
                throw new IllegalArgumentException("Prefix of key already exists");
            }
            if (!node.hasChild(c)) {
                node.addChildNode(new PrefixTreeNode(c));
            }
            node = node.getChild(c);
        }
        if (node.getObject() != null) {
            throw new IllegalArgumentException("Key already exists");
        }
        node.setObject(object);
    }

    public T get(String key) {
        PrefixTreeNode node = rootNode;
        for (int i = 0; i < key.length(); i++) {
            node = node.getChild(key.charAt(i));
            if (node == null) {
                return null;
            }
        }
        return node.getObject();
    }

    public boolean isValidPrefix(String keyPrefix) {
        PrefixTreeNode node = rootNode;
        for (int i = 0; i < keyPrefix.length(); i++) {
            node = node.getChild(keyPrefix.charAt(i));
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    public class PrefixTreeNode {
        public final Character character;
        private Map<Character, PrefixTreeNode> children = new HashMap<>();
        private T object;

        public PrefixTreeNode() {
            character = null;
        }

        public PrefixTreeNode(Character character) {
            this.character = character;
        }

        public boolean hasChild(char c) {
            return children.containsKey(c);
        }

        public PrefixTreeNode getChild(char c) {
            return children.get(c);
        }

        public void addChildNode(PrefixTreeNode childNode) {
            children.put(childNode.character, childNode);
        }

        public T getObject() {
            return object;
        }

        public void setObject(T object) {
            this.object = object;
        }
    }

}
