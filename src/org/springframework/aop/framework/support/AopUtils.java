
/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.framework.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.AspectException;
import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Utility methods used by the AOP framework.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: AopUtils.java,v 1.15 2004/03/19 21:35:54 johnsonr Exp $
 */
public abstract class AopUtils {
	
	public static boolean isCglibProxy(Object o) {
		return o.getClass().getName().indexOf("$$") != -1;
	}
	
	public static boolean isJdkDynamicProxy(Object o) {
		return Proxy.isProxyClass(o.getClass());
	}
	
	public static boolean isAopProxy(Object o) {
		return isJdkDynamicProxy(o) || isCglibProxy(o);
	}
	
	/**
	 * Given a method, which may come from an interface, and a targetClass
	 * used in the current AOP invocation, find the most specific method
	 * if there is one. E.g. the method may be IFoo.bar() and the 
	 * target class may be DefaultFoo. In this case, the method may be
	 * DefaultFoo.bar(). This enables attributes on that method to be
	 * found.
	 * @param method method to be invoked, which may come from an interface
	 * @param targetClass target class for the curren invocation. May
	 * be null or may not even implement the method.
	 * @return the more specific method, or the original method if the targetClass
	 * doesn't specialize it or implement it, or is null.
	 */
	public static Method getMostSpecificMethod(Method method, Class targetClass) {
		if (targetClass != null) {
			try {
				method = targetClass.getMethod(method.getName(), method.getParameterTypes());
			}
			catch (NoSuchMethodException ex) {
				// Perhaps the target class doesn't implement this method:
				// that's fine, just use the original method
			}
		}
		return method;
	}
	
	/**
	 * Convenience method to convert a string array of interface names
	 * to a class array.
	 * @throws ClassNotFoundException if any of the classes can't be loaded
	 * @throws AspectException if any of the classes is not an interface
	 * @return an array of interface classes
	 */
	public static Class[] toInterfaceArray(String[] interfaceNames) throws AspectException, ClassNotFoundException {
		Class interfaces[] = new Class[interfaceNames.length];
		for (int i = 0; i < interfaceNames.length; i++) {
			interfaces[i] = Class.forName(interfaceNames[i], true, Thread.currentThread().getContextClassLoader());
			// Check it's an interface
			if (!interfaces[i].isInterface())
				throw new AspectException("Can proxy only interfaces: " + interfaces[i] + " is a class");
		}
		return interfaces;
	}

	/**
	 * Return all interfaces that the given object implements as array,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements as array
	 */
	public static Class[] getAllInterfaces(Object object) {
		List interfaces = getAllInterfacesAsList(object);
		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
	}

	/**
	 * Return all interfaces that the given object implements as List,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements as List
	 */
	public static List getAllInterfacesAsList(Object object) {
		List interfaces = new ArrayList();
		Class clazz = object.getClass();
		while (clazz != null) {
			for (int i = 0; i < clazz.getInterfaces().length; i++) {
				Class ifc = clazz.getInterfaces()[i];
				interfaces.add(ifc);
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}
	
	/**
	 * Is the given method declared on one of these interfaces?
	 * @param m method to check
	 * @param interfaces array of interfaces we want to check
	 * @return whether the method is declared on one of these interfaces
	 */
	public static boolean methodIsOnOneOfTheseInterfaces(Method m, Class[] interfaces) {
		if (interfaces == null)
			return false;
			
		for (int i = 0; i < interfaces.length; i++) {
			if (!interfaces[i].isInterface())
				throw new IllegalArgumentException(interfaces[i].getName() + " is not an interface");
			// TODO check that the method with this name actually comes from the interface?
			try {
				interfaces[i].getDeclaredMethod(m.getName(), m.getParameterTypes());
				return true;
			}
			catch (NoSuchMethodException ex) {
				// Didn't find it...keep going
			}
		}	// for
		return false;
	}

	/**
	 * Can the given pointcut apply at all on the given class?
	 * This is an important test as it can be used to optimize
	 * out a pointcut for a class
	 * @param pc pc static or dynamic pointcut
	 * @param targetClass class we're testing
	 * @param proxyInterfaces proxy interfaces. If null, all methods
	 * on class may be proxied
	 * @return whether the pointcut can apply on any method
	 */
	public static boolean canApply(Pointcut pc, Class targetClass, Class[] proxyInterfaces) {
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}
		
		// It may apply to the class
		// Check whether it can apply on any method
		// Checks public methods, including inherited methods
		Method[] methods = targetClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			// If we're looking only at interfaces and this method
			// isn't on any of them, skip it
			if (proxyInterfaces != null && !methodIsOnOneOfTheseInterfaces(m, proxyInterfaces)) {
				continue;
			}
			if (pc.getMethodMatcher().matches(m, targetClass))
				return true;
		}
		return false;
	}
	
	public static boolean canApply(Advisor advisor, Class targetClass, Class[] proxyInterfaces) {
		if (advisor instanceof IntroductionAdvisor) {
			return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor) {
			PointcutAdvisor pca = (PointcutAdvisor) advisor;
			return canApply(pca.getPointcut(), targetClass, proxyInterfaces);
		}
		else {
			// It doesn't have a pointcut so we assume it applies
			return true;
		}
	}

}
