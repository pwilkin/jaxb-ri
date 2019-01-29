/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.xjc.addon.accessors;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Generates synchronized methods.
 * 
 * @author
 *     Martin Grebac (martin.grebac@sun.com)
 */
public class PluginImpl extends Plugin {

    public String getOptionName() {
        return "Xpropertyaccessors";
    }

    public String getUsage() {
        return "  -Xpropertyaccessors :  Use XmlAccessType PROPERTY instead of FIELD for generated classes";
    }

    @Override
    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;   // no option recognized
    }

    @SuppressWarnings("unchecked")
	public boolean run( Outline model, Options opt, ErrorHandler errorHandler ) {

        for( ClassOutline co : (Collection<ClassOutline>) model.getClasses() ) {
            Iterator<JAnnotationUse> ann = co.ref.annotations().iterator();
            while (ann.hasNext()) {
               try {
                    JAnnotationUse a = ann.next();
                    Field clazzField = a.getClass().getDeclaredField("clazz");
                    clazzField.setAccessible(true);
                    JClass cl = (JClass) clazzField.get(a);
                    if (cl.equals(model.getCodeModel()._ref(XmlAccessorType.class))) {
                        a.param("value", XmlAccessType.PROPERTY);
                        break;
                    }
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Iterator<JFieldVar> flds = co.implClass.fields().values().iterator();
            while (flds.hasNext()) {
            	try {
            		JFieldVar fld = flds.next();
            		String fldName = fld.name();
            		Set<JAnnotationUse> annotsCopy = new HashSet<JAnnotationUse>(fld.annotations());
            		String getterName = "get" + fldName.substring(0, 1).toUpperCase() + fldName.substring(1);
            		String escapedName = null;
            		for (JAnnotationUse annot : annotsCopy) {
            			 Field clazzField = annot.getClass().getDeclaredField("clazz");
                         clazzField.setAccessible(true);
                         JClass cl = (JClass) clazzField.get(annot);
                         if (cl.equals(model.getCodeModel()._ref(XmlElement.class))) {
                        	 if (annot.getAnnotationMembers().containsKey("name")) {
                        		 JAnnotationValue val = (JAnnotationValue) annot.getAnnotationMembers().get("name");
                        		 StringWriter sw = new StringWriter();
                        		 JFormatter ft = new JFormatter(sw); 
                        		 val.generate(ft);
                        		 String quotedName = sw.getBuffer().toString();
                        		 String name = quotedName.substring(quotedName.indexOf('"') + 1, quotedName.lastIndexOf('"'));
                        		 escapedName = NameConverter.standard.toClassName(name);
                        		 getterName = "get" + escapedName;
                        	 }
                         }
            		}
            		
            		JMethod getterMethod = co.implClass.getMethod(getterName, new JType[] {});
            		if (getterMethod != null) {
            			for (JAnnotationUse annot : annotsCopy) {
            				fld.removeAnnotation(annot);
            				getterMethod.annotate(annot);
            			}
            			String properGetterName = "get" + fldName.substring(0, 1).toUpperCase() + fldName.substring(1);
            			if (!properGetterName.equals(getterName)) {
            				// normalize the getter/setter names
            				getterMethod.name(properGetterName);
            				String setterName = "set" + escapedName;
            				JMethod setterMethod = co.implClass.getMethod(setterName, new JType[] { getterMethod.type() });
            				String propperSetterName = "set" + fldName.substring(0, 1).toUpperCase() + fldName.substring(1);
            				setterMethod.name(propperSetterName);
            			}
            		}
            	} catch (IllegalArgumentException ex) {
                    Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchFieldException e) {
                	Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, e);
				} catch (IllegalAccessException e) {
					Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, e);
				}
            }
        }        
        return true;
    }
    
}
