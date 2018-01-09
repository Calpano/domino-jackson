/*
 * Copyright 2013 Nicolas Morel
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

package com.progressoft.brix.domino.jacksonapt.deser;

import com.progressoft.brix.domino.jacksonapt.JsonDeserializationContext;
import com.progressoft.brix.domino.jacksonapt.JsonDeserializer;
import com.progressoft.brix.domino.jacksonapt.JsonDeserializerParameters;
import com.progressoft.brix.domino.jacksonapt.stream.JsonReader;
import com.progressoft.brix.domino.jacksonapt.stream.JsonToken;

/**
 * Default {@link JsonDeserializer} implementation for {@link Character}.
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public class CharacterJsonDeserializer extends JsonDeserializer<Character> {

    private static final CharacterJsonDeserializer INSTANCE = new CharacterJsonDeserializer();

    /**
     * <p>getInstance</p>
     *
     * @return an instance of {@link CharacterJsonDeserializer}
     */
    public static CharacterJsonDeserializer getInstance() {
        return INSTANCE;
    }

    private CharacterJsonDeserializer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Character doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        if (JsonToken.NUMBER.equals(reader.peek())) {
            return (char) reader.nextInt();
        } else {
            String value = reader.nextString();
            if (value.isEmpty()) {
                return null;
            }
            return value.charAt(0);
        }
    }
}
