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
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;

/**
 * @author Kohsuke Kawaguchi
 */
class RuntimeAttributePropertyInfoImpl extends AttributePropertyInfoImpl<Type,Class,Field,Method>
    implements RuntimeAttributePropertyInfo {

    RuntimeAttributePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,Class,Field,Method> seed) {
        super(classInfo, seed);
    }

    public boolean elementOnlyContent() {
        return true;
    }

    public RuntimeNonElement getTarget() {
        return (RuntimeNonElement) super.getTarget();
    }

    public List<? extends RuntimeNonElement> ref() {
        return (List<? extends RuntimeNonElement>)super.ref();
    }

    public RuntimePropertyInfo getSource() {
        return this;
    }

    public void link() {
        getTransducer();
        super.link();
    }
}
