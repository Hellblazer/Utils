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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */
abstract public class OrderedLookupScope<Key, Value> extends
        LookupScope<Key, Value> {

    private final List<LookupScope<Key, Value>> additional;

    public OrderedLookupScope(String name, List<LookupScope<Key, Value>> imports) {
        super(name, (imports == null || imports.size() == 0) ? null
                                                            : imports.get(0));
        additional = new ArrayList<>();
        if (imports != null) {
            additional.addAll(imports.subList(1, imports.size()));
        }
    }

    public boolean add(LookupScope<Key, Value> scope) {
        return additional.add(scope);
    }

    /**
     * Lookup the key in the hierarchical scope of the receiver, searching first
     * in the receiver, then through the ordered list of parent scopes
     * 
     * @param key
     * @return the value associated with the key in the reciever scope, or null
     */
    @Override
    public Value lookup(Key key) {
        Value value = super.lookup(key);
        if (value == null) {
            for (LookupScope<Key, Value> c : additional) {
                value = c.lookup(key);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Lookup the key in the named scope. The first scope found matching the
     * name in a traversal of ordered parents is returned.
     * 
     * @param scope
     * @param key
     * @return the value associated with the key in the named scope, or null
     */
    @Override
    public Value lookup(String scope, Key key) {
        Value value = super.lookup(scope, key);
        if (value != null) {
            return value;
        }
        for (LookupScope<Key, Value> c : additional) {
            value = c.lookup(scope, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public boolean remove(LookupScope<Key, Value> scope) {
        return additional.remove(scope);
    }

    @Override
    abstract protected Value localLookup(Key key);

}
