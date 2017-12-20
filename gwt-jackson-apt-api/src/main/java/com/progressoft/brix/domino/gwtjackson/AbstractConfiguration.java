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

package com.progressoft.brix.domino.gwtjackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.progressoft.brix.domino.gwtjackson.deser.map.key.KeyDeserializer;
import com.progressoft.brix.domino.gwtjackson.ser.map.key.KeySerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Abstract AbstractConfiguration class.</p>
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public abstract class AbstractConfiguration {

    public class PrimitiveTypeConfiguration {

        private final Class type;

        private PrimitiveTypeConfiguration( Class type ) {
            this.type = type;
        }

        public PrimitiveTypeConfiguration serializer( Class serializer ) {
            mapTypeToSerializer.put( type, serializer );
            return this;
        }

        public PrimitiveTypeConfiguration deserializer( Class deserializer ) {
            mapTypeToDeserializer.put( type, deserializer );
            return this;
        }
    }

    public class TypeConfiguration<T> {

        private final Class<T> type;

        private TypeConfiguration( Class<T> type ) {
            this.type = type;
        }

        public TypeConfiguration<T> serializer( Class<? extends JsonSerializer> serializer ) {
            mapTypeToSerializer.put( type, serializer );
            return this;
        }

        public TypeConfiguration<T> deserializer( Class<? extends JsonDeserializer> deserializer ) {
            mapTypeToDeserializer.put( type, deserializer );
            return this;
        }
    }

    public class KeyTypeConfiguration<T> {

        private final Class<T> type;

        private KeyTypeConfiguration( Class<T> type ) {
            this.type = type;
        }

        public KeyTypeConfiguration<T> serializer( Class<? extends KeySerializer> serializer ) {
            mapTypeToKeySerializer.put( type, serializer );
            return this;
        }

        public KeyTypeConfiguration<T> deserializer( Class<? extends KeyDeserializer> deserializer ) {
            mapTypeToKeyDeserializer.put( type, deserializer );
            return this;
        }
    }

    private final Map<Class, Class> mapTypeToSerializer = new HashMap<Class, Class>();

    private final Map<Class, Class> mapTypeToDeserializer = new HashMap<Class, Class>();

    private final Map<Class, Class> mapTypeToKeySerializer = new HashMap<Class, Class>();

    private final Map<Class, Class> mapTypeToKeyDeserializer = new HashMap<Class, Class>();

    private final Map<Class, Class> mapMixInAnnotations = new HashMap<Class, Class>();

    private final List<String> whitelist = new ArrayList<String>();

    private JsonAutoDetect.Visibility fieldVisibility = JsonAutoDetect.Visibility.DEFAULT;

    private JsonAutoDetect.Visibility getterVisibility = JsonAutoDetect.Visibility.DEFAULT;

    private JsonAutoDetect.Visibility isGetterVisibility = JsonAutoDetect.Visibility.DEFAULT;

    private JsonAutoDetect.Visibility setterVisibility = JsonAutoDetect.Visibility.DEFAULT;

    private JsonAutoDetect.Visibility creatorVisibility = JsonAutoDetect.Visibility.DEFAULT;

    /**
     * <p>Constructor for AbstractConfiguration.</p>
     */
    protected AbstractConfiguration() {
        configure();
    }

    /**
     * <p>primitiveType</p>
     *
     * @param type Type
     * @return a {@link PrimitiveTypeConfiguration} to configure serializer and/or deserializer for the given primitive type.
     */
    protected PrimitiveTypeConfiguration primitiveType( Class type ) {
        if ( !type.isPrimitive() ) {
            throw new IllegalArgumentException( "Type " + type + " is not a primitive. Call type(Class) instead" );
        }
        return new PrimitiveTypeConfiguration( type );
    }

    /**
     * <p>type</p>
     *
     * @param type Type
     * @return a {@link TypeConfiguration} to configure serializer and/or deserializer for the given type.
     * @param <T> a T object.
     */
    protected <T> TypeConfiguration<T> type( Class<T> type ) {
        if ( type.isPrimitive() ) {
            throw new IllegalArgumentException( "Type " + type + " is a primitive. Call primitiveType(Class) instead" );
        }
        return new TypeConfiguration<T>( type );
    }

    /**
     * Return a {@link KeyTypeConfiguration} to configure key serializer and/or deserializer for the given type.
     *
     * @param type a {@link Class} object.
     * @param <T> Type
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration.KeyTypeConfiguration} object.
     */
    protected <T> KeyTypeConfiguration<T> key( Class<T> type ) {
        if ( type.isPrimitive() ) {
            throw new IllegalArgumentException( "Primitive types cannot be used as a map's key" );
        }
        return new KeyTypeConfiguration<T>( type );
    }

    /**
     * Method to use for adding mix-in annotations to use for augmenting
     * specified class or interface. All annotations from
     * <code>mixinSource</code> are taken to override annotations
     * that <code>target</code> (or its supertypes) has.
     *
     * @param target Class (or interface) whose annotations to effectively override
     * @param mixinSource Class (or interface) whose annotations are to
     * be "added" to target's annotations, overriding as necessary
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration addMixInAnnotations( Class<?> target, Class<?> mixinSource ) {
        mapMixInAnnotations.put( target, mixinSource );
        return this;
    }

    /**
     * Method to add a regex into whitelist.
     * <p>
     * All the types matching whitelist are added to the subtype list of {@link Object} and
     * {@link Serializable} serializer/deserializer.
     * </p>
     *
     * @param regex the regex to add
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration whitelist( String regex ) {
        whitelist.add( regex );
        return this;
    }

    /**
     * Override the default behaviour of {@link JsonAutoDetect.Visibility#DEFAULT} for fields.
     *
     * @param visibility the new default behaviour
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration fieldVisibility( JsonAutoDetect.Visibility visibility ) {
        this.fieldVisibility = visibility;
        return this;
    }

    /**
     * Override the default behaviour of {@link JsonAutoDetect.Visibility#DEFAULT} for getters.
     *
     * @param visibility the new default behaviour
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration getterVisibility( JsonAutoDetect.Visibility visibility ) {
        this.getterVisibility = visibility;
        return this;
    }

    /**
     * Override the default behaviour of {@link JsonAutoDetect.Visibility#DEFAULT} for boolean getters.
     *
     * @param visibility the new default behaviour
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration isGetterVisibility( JsonAutoDetect.Visibility visibility ) {
        this.isGetterVisibility = visibility;
        return this;
    }

    /**
     * Override the default behaviour of {@link JsonAutoDetect.Visibility#DEFAULT} for setters.
     *
     * @param visibility the new default behaviour
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration setterVisibility( JsonAutoDetect.Visibility visibility ) {
        this.setterVisibility = visibility;
        return this;
    }

    /**
     * Override the default behaviour of {@link JsonAutoDetect.Visibility#DEFAULT} for creators.
     *
     * @param visibility the new default behaviour
     * @return a {@link com.progressoft.brix.domino.gwtjackson.AbstractConfiguration} object.
     */
    protected AbstractConfiguration creatorVisibility( JsonAutoDetect.Visibility visibility ) {
        this.creatorVisibility = visibility;
        return this;
    }

    /**
     * <p>configure</p>
     */
    protected abstract void configure();

    /**
     * <p>Getter for the field <code>mapTypeToSerializer</code>.</p>
     *
     * @return a {@link Map} object.
     */
    public Map<Class, Class> getMapTypeToSerializer() {
        return mapTypeToSerializer;
    }

    /**
     * <p>Getter for the field <code>mapTypeToDeserializer</code>.</p>
     *
     * @return a {@link Map} object.
     */
    public Map<Class, Class> getMapTypeToDeserializer() {
        return mapTypeToDeserializer;
    }

    /**
     * <p>Getter for the field <code>mapTypeToKeySerializer</code>.</p>
     *
     * @return a {@link Map} object.
     */
    public Map<Class, Class> getMapTypeToKeySerializer() {
        return mapTypeToKeySerializer;
    }

    /**
     * <p>Getter for the field <code>mapTypeToKeyDeserializer</code>.</p>
     *
     * @return a {@link Map} object.
     */
    public Map<Class, Class> getMapTypeToKeyDeserializer() {
        return mapTypeToKeyDeserializer;
    }

    /**
     * <p>Getter for the field <code>mapMixInAnnotations</code>.</p>
     *
     * @return a {@link Map} object.
     */
    public Map<Class, Class> getMapMixInAnnotations() {
        return mapMixInAnnotations;
    }

    /**
     * <p>Getter for the field <code>whitelist</code>.</p>
     *
     * @return a {@link List} object.
     */
    public List<String> getWhitelist() {
        return whitelist;
    }

    /**
     * <p>Getter for the field <code>fieldVisibility</code>.</p>
     *
     * @return a {@link com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility} object.
     */
    public Visibility getFieldVisibility() {
        return fieldVisibility;
    }

    /**
     * <p>Getter for the field <code>getterVisibility</code>.</p>
     *
     * @return a {@link com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility} object.
     */
    public Visibility getGetterVisibility() {
        return getterVisibility;
    }

    /**
     * <p>Getter for the field <code>isGetterVisibility</code>.</p>
     *
     * @return a {@link com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility} object.
     */
    public Visibility getIsGetterVisibility() {
        return isGetterVisibility;
    }

    /**
     * <p>Getter for the field <code>setterVisibility</code>.</p>
     *
     * @return a {@link com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility} object.
     */
    public Visibility getSetterVisibility() {
        return setterVisibility;
    }

    /**
     * <p>Getter for the field <code>creatorVisibility</code>.</p>
     *
     * @return a {@link com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility} object.
     */
    public Visibility getCreatorVisibility() {
        return creatorVisibility;
    }
}
