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

package com.progressoft.brix.domino.gwtjackson.deser.array;

import com.progressoft.brix.domino.gwtjackson.JsonDeserializationContext;
import com.progressoft.brix.domino.gwtjackson.JsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.JsonDeserializerParameters;
import com.progressoft.brix.domino.gwtjackson.deser.BaseNumberJsonDeserializer.DoubleJsonDeserializer;
import com.progressoft.brix.domino.gwtjackson.stream.JsonReader;

import java.util.List;

/**
 * Default {@link JsonDeserializer} implementation for array of double.
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public class PrimitiveDoubleArrayJsonDeserializer extends AbstractArrayJsonDeserializer<double[]> {

    private static final PrimitiveDoubleArrayJsonDeserializer INSTANCE = new PrimitiveDoubleArrayJsonDeserializer();

    /**
     * <p>getInstance</p>
     *
     * @return an instance of {@link PrimitiveDoubleArrayJsonDeserializer}
     */
    public static PrimitiveDoubleArrayJsonDeserializer getInstance() {
        return INSTANCE;
    }

    private PrimitiveDoubleArrayJsonDeserializer() { }

    /** {@inheritDoc} */
    @Override
    public double[] doDeserializeArray( JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params ) {
        List<Double> list = deserializeIntoList( reader, ctx, DoubleJsonDeserializer.getInstance(), params );

        double[] result = new double[list.size()];
        int i = 0;
        for ( Double value : list ) {
            if ( null != value ) {
                result[i] = value;
            }
            i++;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected double[] doDeserializeSingleArray( JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params ) {
        return new double[]{DoubleJsonDeserializer.getInstance().deserialize( reader, ctx, params )};
    }
}
