/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * @author Kohsuke Kawaguchi
 */
class RuntimeMapPropertyInfoImpl extends MapPropertyInfoImpl<Type,Class,Field,Method> implements RuntimeMapPropertyInfo {
    private final Accessor acc;

    RuntimeMapPropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,Class,Field,Method> seed) {
        super(classInfo, seed);
        this.acc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
    }

    public Accessor getAccessor() {
        return acc;
    }

    public boolean elementOnlyContent() {
        return true;
    }

    public RuntimeNonElement getKeyType() {
        return (RuntimeNonElement)super.getKeyType();
    }

    public RuntimeNonElement getValueType() {
        return (RuntimeNonElement)super.getValueType();
    }

    public List<? extends RuntimeTypeInfo> ref() {
        return (List<? extends RuntimeTypeInfo>)super.ref();
    }
}
