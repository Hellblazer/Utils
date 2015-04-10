/** 
 * (C) Copyright 2015 Chiral Behaviors LLC, All Rights Reserved
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
package com.hellblazer.utils;

/**
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */
abstract public class LookupScope<Key, Value> {
    private final String                  name;
    private final LookupScope<Key, Value> parent;

    public LookupScope(String name, LookupScope<Key, Value> parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Lookup the key first in the reciver, then in the hierarchical scope of
     * the parent
     * 
     * @param key
     * @return the value associated with the key in the reciever scope, or null
     */
    public Value lookup(Key key) {
        Value value = localLookup(key);
        if (value == null && parent != null) {
            return parent.lookup(key);
        }
        return null;
    }

    /**
     * Lookup the key in the named scope. The first scope found matching the
     * name in a traversal of parents is used.
     * 
     * @param scope
     * @param key
     * @return the value associated with the key in the named scope, or null
     */
    public Value lookup(String scope, Key key) {
        if (scope == null) {
            return lookup(key);
        }
        if (name != null && name.equals(scope)) {
            return lookup(key);
        }
        if (parent != null) {
            return parent.lookup(scope, key);
        }
        return null;
    }

    abstract protected Value localLookup(Key key);

}
