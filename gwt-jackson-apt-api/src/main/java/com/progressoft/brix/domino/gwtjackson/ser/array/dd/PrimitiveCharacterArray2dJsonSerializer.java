/*
 * Copyright 2014 Nicolas Morel
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

package com.progressoft.brix.domino.gwtjackson.ser.array.dd;

import com.progressoft.brix.domino.gwtjackson.JsonSerializationContext;
import com.progressoft.brix.domino.gwtjackson.JsonSerializer;
import com.progressoft.brix.domino.gwtjackson.JsonSerializerParameters;
import com.progressoft.brix.domino.gwtjackson.stream.JsonWriter;

/**
 * Default {@link JsonSerializer} implementation for 2D array of char.
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public class PrimitiveCharacterArray2dJsonSerializer extends JsonSerializer<char[][]> {

    private static final PrimitiveCharacterArray2dJsonSerializer INSTANCE = new PrimitiveCharacterArray2dJsonSerializer();

    /**
     * <p>getInstance</p>
     *
     * @return an instance of {@link PrimitiveCharacterArray2dJsonSerializer}
     */
    public static PrimitiveCharacterArray2dJsonSerializer getInstance() {
        return INSTANCE;
    }

    private PrimitiveCharacterArray2dJsonSerializer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEmpty(char[][] value) {
        return null == value || value.length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSerialize(JsonWriter writer, char[][] values, JsonSerializationContext ctx, JsonSerializerParameters params) {
        if (!ctx.isWriteEmptyJsonArrays() && values.length == 0) {
            writer.cancelName();
            return;
        }

        writer.beginArray();
        for (char[] array : values) {
            if (ctx.isWriteCharArraysAsJsonArrays()) {
                writer.beginArray();
                for (char value : array) {
                    writer.value(Character.toString(value));
                }
                writer.endArray();
            } else {
                writer.value(new String(array));
            }
        }
        writer.endArray();

    }
}
