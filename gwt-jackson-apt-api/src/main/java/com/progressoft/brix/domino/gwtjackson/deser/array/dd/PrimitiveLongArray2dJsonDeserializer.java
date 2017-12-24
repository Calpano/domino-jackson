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

package com.progressoft.brix.domino.gwtjackson.deser.array.dd;

import com.progressoft.brix.domino.gwtjackson.JsonDeserializerParameters;
import com.progressoft.brix.domino.gwtjackson.JsonDeserializationContext;
import com.progressoft.brix.domino.gwtjackson.JsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.deser.BaseNumberJsonDeserializer.LongJsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.stream.JsonReader;

import java.util.List;

/**
 * Default {@link JsonDeserializer} implementation for 2D array of long.
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public class PrimitiveLongArray2dJsonDeserializer extends AbstractArray2dJsonDeserializer<long[][]> {

    private static final PrimitiveLongArray2dJsonDeserializer INSTANCE = new PrimitiveLongArray2dJsonDeserializer();

    /**
     * <p>getInstance</p>
     *
     * @return an instance of {@link PrimitiveLongArray2dJsonDeserializer}
     */
    public static PrimitiveLongArray2dJsonDeserializer getInstance() {
        return INSTANCE;
    }

    private PrimitiveLongArray2dJsonDeserializer() { }

    /** {@inheritDoc} */
    @Override
    public long[][] doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params ) {
        List<List<Long>> list = deserializeIntoList( reader, ctx, LongJsonDeserializer.getInstance(), params );

        if ( list.isEmpty() ) {
            return new long[0][0];
        }

        List<Long> firstList = list.get( 0 );
        if ( firstList.isEmpty() ) {
            return new long[list.size()][0];
        }

        long[][] array = new long[list.size()][firstList.size()];

        int i = 0;
        int j;
        for ( List<Long> innerList : list ) {
            j = 0;
            for ( Long value : innerList ) {
                if ( null != value ) {
                    array[i][j] = value;
                }
                j++;
            }
            i++;
        }
        return array;
    }
}
