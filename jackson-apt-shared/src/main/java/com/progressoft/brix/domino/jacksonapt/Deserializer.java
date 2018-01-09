/*
 * Copyright 2016 Nicolas Morel
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

package com.progressoft.brix.domino.jacksonapt;

import com.progressoft.brix.domino.jacksonapt.deser.map.key.KeyDeserializer;

/**
 * Wrapper to access both key and json deserializer for a type.
 *
 * @author nicolasmorel
 * @version $Id: $
 */
public abstract class Deserializer<T> {

    private KeyDeserializer<T> key;

    private JsonDeserializer<T> json;

    /**
     * <p>key</p>
     *
     * @return a {@link com.progressoft.brix.domino.jacksonapt.deser.map.key.KeyDeserializer} object.
     */
    public KeyDeserializer<T> key() {
        if (null == key) {
            key = createKeyDeserializer();
        }
        return key;
    }

    /**
     * <p>createKeyDeserializer</p>
     *
     * @return a {@link com.progressoft.brix.domino.jacksonapt.deser.map.key.KeyDeserializer} object.
     */
    protected abstract KeyDeserializer<T> createKeyDeserializer();

    /**
     * <p>json</p>
     *
     * @return a {@link com.progressoft.brix.domino.jacksonapt.JsonDeserializer} object.
     */
    public JsonDeserializer<T> json() {
        if (null == json) {
            json = createJsonDeserializer();
        }
        return json;
    }

    /**
     * <p>createJsonDeserializer</p>
     *
     * @return a {@link com.progressoft.brix.domino.jacksonapt.JsonDeserializer} object.
     */
    protected abstract JsonDeserializer<T> createJsonDeserializer();

}
