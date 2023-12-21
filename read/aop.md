# spring aop

## **aop需求**
![spring 框架](https://user-images.githubusercontent.com/2216435/65569139-12308d80-df8f-11e9-9a3c-e9d7b2498941.png)

## **aop术语**
![aop](https://user-images.githubusercontent.com/2216435/65569137-10ff6080-df8f-11e9-84a1-4365be5b69cb.png)

## AOP如何织入
![aop beanpostprocessor层次](https://user-images.githubusercontent.com/2216435/65811616-fd9e0080-e1ed-11e9-8ba3-f22d6c02acff.png)

首先，AOP的相关功能类(AbstractAutoProxyCreator)，作为BeanPostProcessor，自然会被加载。那么每一个原始的bean实例，通过postProcessAfterInitialization实质上可能变成了aop 
代理，即为JdkDynamicAopProxy or Cglib2AopProxy。而aop的各个特定功能则通过对Advisor的实现，整体作为集合放入了AopProxy内部，等待切面方法调用invoke具体执行的时候再行调用。在调用时候可以再进行静态和动态匹配等细节操作。

    BeanNameAutoProxyCreatorTests
    	setUp()
    		new ClassPathXmlApplicationContext("xxx.xml")
    			refresh()
    				registerBeanPostProcessors() // 代理类注册
    				beanFactory.preInstantiateSingletons()
    					getBean()
    						createBean()
    							bean = applyBeanPostProcessorsAfterInitialization(bean, beanName) 
    								for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
    									BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
    									
    									// 乃成于此
    									beanProcessor.postProcessAfterInitialization(result, name);
    										getInterceptorsAndAdvisorsForBean
    											// AbstractAdvisorAutoProxyCreator
    											getInterceptorsAndAdvisorsForBean
    											    getInterceptorsAndAdvisorsForBean
    											        findEligibleAdvisors
    											            // DefaultAdvisorAutoProxyCreator
    											            findCandidateAdvisors
    											               BeanFactoryUtils.beanNamesIncludingAncestors(owningFactory, Advisor.class)
    											        // 对切面进行排序
    											        sortAdvisors
    										// 需代理的接口
    										proxyFactory.addInterface
    									   
    										// aop切面实现功能类
    										proxyFactory.addAdvisor(advisor);
    									   
    										// 生成代理
    										proxyFactory.getProxy
    										   JdkDynamicAopProxy or Cglib2AopProxy
    								}

### 是否织入aop代理
找出所有aop实现，与实例化后的bean进行比对，比对从类层次，方法层次(任一个方法满足即可)。若发现，那么则生成代理，否则原bean返回。

    AbstractAutoProxyCreator
    	postProcessAfterInitialization
    		getInterceptorsAndAdvisorsForBean
![getInterceptorsAndAdvisorsForBean](https://user-images.githubusercontent.com/2216435/65667911-4b422e00-e073-11e9-8026-c119e4ffa8af.png)

	// 配置bean name，比对发现(private List beanNames)
	BeanNameAutoProxyCreator
		getInterceptorsAndAdvisorsForBean


​    
	// 根据接口等动态匹配发现，是Spring默认方式(DefaultAdvisorAutoProxyCreator)
	AbstractAdvisorAutoProxyCreator
		getInterceptorsAndAdvisorsForBean
			findEligibleAdvisors
				// 找出所有aop 实现
				findCandidateAdvisors();
					BeanFactoryUtils.beanNamesIncludingAncestors(owningFactory, Advisor.class)
					
				// 逐个比对可否施加于clazz
				eligibleAdvice=AopUtils.canApply(candidate, clazz, null) 
					// 由此可以看出pointcut应该由具体的Advisor，比如事物拦截器，性能拦截器
					// 来实现，以此仅对其应该施加的对象进行代理
					Pointcut
						ClassFilter
						MethodMatcher

### 增强何时执行
如果bean被增强，在生成代理，在调用相应方法时候，增强功能作为链表存储于代理类内部，按语义进行执行。

### 如何使用不同的PointCut

由具体实现决定，可以继承既有StaticMethodMatcherPointcut，DynamicMethodMatcherPointcutAdvisor
![](https://user-images.githubusercontent.com/2216435/65811716-528e4680-e1ef-11e9-8150-5fa3bf7fe25a.png)

### 动静匹配有何区别
Spring采用这样的机制：在创建代理时对目标类的每个连接点使用静态切点检查，如果仅通过静态切点检查就可以知道连接点是不匹配的，则在运行时就不再进行动态检查了；如果静态切点检查是匹配的，在运行时才进行动态切点检查。

在Spring中，不管是静态切面还是动态切面都是通过动态代理技术实现的。所谓静态切面是指在生成代理对象时，就确定了增强是否需要织入到目标类连接点上，而动态切面是指必须在运行期根据方法入参的值来判断增强是否织入到目标类连接点上

### 代理执行过程
根据上面所述，在bean的初始化阶段，jdk/cglib代理即形成一层包裹，并将可行的Advisor.class作为配置放入代理类内部；

在具体方法执行时，则通过invoke方法，并通过代理的配置找到增强实现。实际匹配执行，一是因为不是每个方法都需要代理，二是因为某些方法需要动态匹配执行时参数等。此时还通过methodcache缓存加速匹配查找。
![aop代理和函数执行](https://user-images.githubusercontent.com/2216435/65811550-de52a380-e1ec-11e9-8ec0-7c6299d13e8d.png)

关键代码详解：

	// debug入口
	AbstractAopProxyTests
		testReplaceArgument
	
	// 以jdk代理示例，任何被代理的方法调用会直接进入
	JdkDynamicAopProxy.invoke
		getInterceptorsAndDynamicInterceptionAdvice
			getInterceptorsAndDynamicInterceptionAdvice
				// HashMapCachingAdvisorChainFactory::methodCache， 缓存加速查找
				List cached = (List) this.methodCache.get(method);
				if (cached == null) {
					// 类匹配以及可能的方法动态匹配
					cached = AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
								PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
								if (pointcutAdvisor.getPointcut().getClassFilter().matches(targetClass)) {
									MethodInterceptor interceptor = (MethodInterceptor) GlobalAdvisorAdapterRegistry.getInstance().getInterceptor(advisor);
									MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
									if (mm.matches(method, targetClass)) {
										// 不管三七二十一先把静态匹配到的拦截器和可能的动态拦截器加进去
										// 实际的动态参数匹配还得执行时候才检查，这样通过methodCache加速
										if (mm.isRuntime()) {
											interceptors.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm) );
										}
										else {							
											interceptors.add(interceptor);
										}
									}
								}
					this.methodCache.put(method, cached);
				}
	
		invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);							
		// Proceed to the joinpoint through the interceptor chain
		retVal = invocation.proceed();
			if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
				return invokeJoinpoint();
			}
	
			Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
			if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
				// Evaluate dynamic method matcher here: static part will already have
				// been evaluated and found to match
				InterceptorAndDynamicMethodMatcher dm = (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
				// 在此进行动态参数匹配
				if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
					return dm.interceptor.invoke(this);
				}
				else {
					// Dynamic matching failed
					// Skip this interceptor and invoke the next in the chain
					return proceed();
				}
			}
			else {
				// It's an interceptor so we just invoke it: the pointcut will have
				// been evaluated statically before this object was constructed
				return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
			}
					
	AdvisorChainFactoryUtils
		calculateInterceptorsAndDynamicInterceptionAdvice
	
	HashMapCachingAdvisorChainFactory
		methodCache
	
	GlobalAdvisorAdapterRegistry
		getInterceptor

**AopProxy**

![](https://user-images.githubusercontent.com/2216435/65811724-7487c900-e1ef-11e9-8fee-97a956c92a96.png)

**Invocation**

![](https://user-images.githubusercontent.com/2216435/65811725-75205f80-e1ef-11e9-86b4-dfcd106c2f14.png)

### 基于注解的aop实现
对于spring 5以上的版本而言，核心在于org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator

![](https://user-images.githubusercontent.com/2216435/75761074-5fe9c700-5d73-11ea-8d65-6f6f3aa85333.png)
加载beanpostprocessor时候该类实现了对@Aspect注解实现的扫描

	AbstractApplicationContext
		refresh
			registerBeanPostProcessors
				// 固定了AopConfigUtils:public static final String AUTO_PROXY_CREATOR_BEAN_NAME = “org.springframework.aop.config.internalAutoProxyCreator”
				// 字符串，实际会映射到AnnotationAwareAspectJAutoProxyCreator

如何映射到AnnotationAwareAspectJAutoProxyCreator

	spring.boot.autoconfigure
		META-INF
			spring.factories
				# Auto Configure
				org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
				org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
				...
![](https://user-images.githubusercontent.com/2216435/75762251-55303180-5d75-11ea-93d3-ffa32796e869.png)
![](https://user-images.githubusercontent.com/2216435/75762352-7e50c200-5d75-11ea-984f-5b68199e1fa3.png)
![](https://user-images.githubusercontent.com/2216435/75762447-a3453500-5d75-11ea-845b-3dee8e7fe6db.png)

## 事务aop实现

	spring.boot.autoconfigure
		META-INF
			spring.factories
				# Auto Configure
				org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
			    org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration,\
				...

![](https://user-images.githubusercontent.com/2216435/75842004-2a43ed00-5e0a-11ea-8e2b-5ed02a7ef830.png)
![](https://user-images.githubusercontent.com/2216435/75842095-6d05c500-5e0a-11ea-8b62-de701131fafb.png)
![](https://user-images.githubusercontent.com/2216435/75842164-97f01900-5e0a-11ea-9ba4-0f354003a6e4.png)
![image](https://user-images.githubusercontent.com/2216435/75842229-c241d680-5e0a-11ea-9134-f05d0fc68dd2.png)

事务功能的加载过程
	
	AbstractApplicationContext
		refresh
			registerBeanPostProcessors
			   // field
				beanFactory
					beanPostProcessors
					    // one post processor will be
						AnnotationAwareAspectJAutoProxyCreator
							advisedBeans
								transactionInterceptor
								transactionAttributeSource
								org.springframework.transaction.config.internalTransactionAdvisor
							aspectJAdvisorsBuilder
							   // 自定义aspect实现
								myAspect1
								myAspect2
								...

那么在bean实例化的时候，被增强的代理实现里面就会包括BeanFactoryTransactionAttributeSourceAdvisor以实现事务功能，当然还有其它的切面实现。			
![image](https://user-images.githubusercontent.com/2216435/75843731-d5ef3c00-5e0e-11ea-8b3f-ab8320e7c17f.png)

### 事务类内部调用的问题
虽然类会被增强，外部调用a，a实现调用b（带注解）。调用a时通过invoke(仍然是proxy调用，只是在其上找不到增强)，最终会进入原始的target类，那么调用a时实质上是this.b，未增强的，所以不会生效。

exposeProxy实质是通过threadlocal将增强的代理类回传到target类，比较怪异

## 参考类图
![总层次](https://user-images.githubusercontent.com/2216435/65811654-5f5e6a80-e1ee-11e9-8632-d5dc90aa1a32.jpg)

![](https://user-images.githubusercontent.com/2216435/65811698-0f33d800-e1ef-11e9-9c57-d5ef01f16f0c.png)

## jdk dynamic proxy

	JdkDynamicProxyTests
		testProxyIsJustInterface
			proxy.setAge
				JdkDynamicAopProxy.invoke
					AopProxyUtils.invokeJoinpointUsingReflection
					   // with actual target
						m.invoke
							Method.invoke
								MethodAccessor.invoke
								   // private static native Object invoke0(Method var0, Object var1, Object[] var2)
									NativeMethodAccessorImpl.invoke0(this.method, obj, args)


**method.invoke**
>If the underlying method is an instance method, it is invoked using dynamic method lookup

有意思，这就意味着Method其实只是描述，需要根据描述去具体的target上查找。涉及到jvm的具体实现

## jdk proxy & cglib

[Java 动态代理](https://github.com/selfpoised/java_proxy/blob/master/README.md)

### java 语言元素的抽象层次
	Method extends Executable extends AccessibleObject implements Member, GenericDeclaration

## 研究点
![](https://user-images.githubusercontent.com/2216435/65811682-dbf14900-e1ee-11e9-8170-067a926f895e.png)

### threadlocal 内部实现
WeakReference

### targetsource有什么用
HotSwappableTargetSource：双数据源互换实现

### java.lang.Object
每个方法都可谓宝藏，加以研究，必有所获

这一部分很多native实现，涉及jvm,linux,操作系统知识

	thread在各个操作系统的实现
	pthread/posix
	wait方法的实现
	锁在jvm内部的实现

## 参考
[Spring源码分析](https://juejin.im/post/5ada8a5cf265da0b9347df8c)