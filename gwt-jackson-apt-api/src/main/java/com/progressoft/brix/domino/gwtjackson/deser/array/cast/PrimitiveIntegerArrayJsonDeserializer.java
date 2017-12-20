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

package com.progressoft.brix.domino.gwtjackson.deser.array.cast;

import com.progressoft.brix.domino.gwtjackson.JsonDeserializationContext;
import com.progressoft.brix.domino.gwtjackson.JsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.JsonDeserializerParameters;
import com.progressoft.brix.domino.gwtjackson.deser.BaseNumberJsonDeserializer.IntegerJsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.deser.array.AbstractArrayJsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.stream.JsonReader;
import com.progressoft.brix.domino.gwtjackson.stream.JsonToken;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayInteger;

/**
 * Default {@link JsonDeserializer} implementation for array of int.
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public class PrimitiveIntegerArrayJsonDeserializer extends AbstractArrayJsonDeserializer<int[]> {

    private static final PrimitiveIntegerArrayJsonDeserializer INSTANCE = new PrimitiveIntegerArrayJsonDeserializer();

    /**
     * <p>getInstance</p>
     *
     * @return an instance of {@link PrimitiveIntegerArrayJsonDeserializer}
     */
    public static PrimitiveIntegerArrayJsonDeserializer getInstance() {
        return INSTANCE;
    }

    private static native int[] reinterpretCast( JsArrayInteger value ) /*-{
        return value;
    }-*/;

    private static int DEFAULT;

    private PrimitiveIntegerArrayJsonDeserializer() { }

    /** {@inheritDoc} */
    @Override
    public int[] doDeserializeArray( JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params ) {
        JsArrayInteger jsArray = JsArrayInteger.createArray().cast();
        reader.beginArray();
        while ( JsonToken.END_ARRAY != reader.peek() ) {
            if ( JsonToken.NULL == reader.peek() ) {
                reader.skipValue();
                jsArray.push( DEFAULT );
            } else {
                jsArray.push( reader.nextInt() );
            }
        }
        reader.endArray();

        if ( GWT.isScript() ) {
            return reinterpretCast( jsArray );
        } else {
            int length = jsArray.length();
            int[] ret = new int[length];
            for ( int i = 0; i < length; i++ ) {
                ret[i] = jsArray.get( i );
            }
            return ret;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected int[] doDeserializeSingleArray( JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params ) {
        return new int[]{IntegerJsonDeserializer.getInstance().deserialize( reader, ctx, params )};
    }
}
