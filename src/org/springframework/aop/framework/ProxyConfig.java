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

package org.springframework.aop.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience superclass for configuration used in creating proxies,
 * to ensure that all proxy creators have consistent properties.
 * <br>
 * Note that it is no longer possible to configure subclasses to 
 * expose the MethodInvocation. Interceptors should normally manage their own
 * ThreadLocals if they need to make resources available to advised objects.
 * If it's absolutely necessary to expose the MethodInvocation, use an
 * interceptor to do so.
 * @author Rod Johnson
 * @version $Id: ProxyConfig.java,v 1.9 2004/03/18 02:46:05 trisberg Exp $
 */
public class ProxyConfig {
	
	/*
	 * Note that some of the instance variables in this class and AdvisedSupport
	 * are protected, rather than private, as is usually preferred in Spring
	 * (following "Expert One-on-One J2EE Design and Development", Chapter 4).
	 * This allows direct field access in the AopProxy implementations, which
	 * produces a 10-20% reduction in AOP performance overhead compared with method
	 * access. - RJ, December 10, 2003.
	 */
	
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean proxyTargetClass;
	
	private boolean optimize;
	
	/**
	 * Is this config frozen: that is, should it be impossible
	 * to change advice. Default is not frozen.
	 */
	private boolean frozen;
	
	/** Factory used to create AopProxy's. */
	private AopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();

	
	/**
	 * Should proxies obtained from this configuration expose
	 * the AOP proxy for the AopContext class to retrieve for targets?
	 * The default is false, as enabling this property may
	 * impair performance.
	 */
	protected boolean exposeProxy;

	
	public ProxyConfig() {
	}

	/**
	 * Copy configuration from the other config
	 * @param other object to copy configuration from
	 */
	public void copyFrom(ProxyConfig other) {
		this.optimize = other.getOptimize();
		this.proxyTargetClass = other.proxyTargetClass;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.aopProxyFactory = other.aopProxyFactory;
	}

	public boolean getProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * Set whether to proxy the target class directly as well as any interfaces.
	 * We can set this to true to force CGLIB proxying. Default is false
	 * @param proxyTargetClass whether to proxy the target class directly as well as any interfaces
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}
	
	/**
	 * @return whether proxies should perform agressive optimizations.
	 */
	public boolean getOptimize() {
		return this.optimize;
	}

	/**
	 * Set whether proxies should perform agressive optimizations.
	 * The exact meaning of "agressive optimizations" will differ
	 * between proxies, but there is usually some tradeoff. 
	 * For example, optimization will usually mean that advice changes won't take
	 * effect after a proxy has been created. For this reason, optimization
	 * is disabled by default. An optimize value of true may be ignored
	 * if other settings preclude optimization: for example, if exposeProxy
	 * is set to true and that's not compatible with the optimization.
	 * <br>For example, CGLIB-enhanced proxies may optimize out.
	 * overriding methods with no advice chain. This can produce 2.5x performance
	 * improvement for methods with no advice. 
	 * <br><b>Warning:</b> Setting this to true can produce large performance
	 * gains when using CGLIB (also set proxyTargetClass to true), so it's
	 * a good setting for performance-critical proxies. However, enabling this
	 * will mean that advice cannot be changed after a proxy has been obtained
	 * from this factory.
	 * @param optimize whether to enable agressive optimizations. 
	 * Default is false.
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}


	/**
	 * @return whether the AOP proxy will expose the AOP proxy for
	 * each invocation.
	 */
	public final boolean getExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * Set whether the proxy should be exposed by the AOP framework as a ThreadLocal for
	 * retrieval via the AopContext class. This is useful if an advised object needs
	 * to call another advised method on itself. (If it uses <code>this</code>, the invocation
	 * will not be advised).
	 * @param exposeProxy whether the proxy should be exposed. Default
	 * is false, for optimal pe3rformance.
	 */
	public final void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}
	
	
	/**
	 * Customise the AopProxyFactory, allowing different strategies
	 * to be dropped in without changing the core framework.
	 * For example, an AopProxyFactory could return an AopProxy using
	 * dynamic proxies, CGLIB or code generation strategy. 
	 * @param apf AopProxyFactory to use. The default uses dynamic
	 * proxies or CGLIB.
	 */
	public void setAopProxyFactory(AopProxyFactory apf) {
		this.aopProxyFactory = apf;
	}
	
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}
	

	/**
	 * @return whether the config is frozen, and no
	 * advice changes can be made
	 */
	public boolean isFrozen() {
		return frozen;
	}

	/**
	 * Set whether this config should be frozen.
	 * When a config is frozen, no advice changes can be
	 * made. This is useful for optimization, and useful
	 * when we don't want callers to be able to manipulate
	 * configuration after casting to Advised.
	 * @param frozen is this config frozen?
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("exposeProxy=" + exposeProxy + "; ");
		sb.append("frozen=" + frozen + "; ");
		sb.append("enableCglibSubclassOptimizations=" + optimize + "; ");
		sb.append("aopProxyFactory=" + aopProxyFactory + "; ");
		return sb.toString();
	}

}
