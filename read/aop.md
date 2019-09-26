# spring aop #
### **aop需求** ###
![spring 框架](https://user-images.githubusercontent.com/2216435/65569139-12308d80-df8f-11e9-9a3c-e9d7b2498941.png)

### **aop术语** ###
![aop](https://user-images.githubusercontent.com/2216435/65569137-10ff6080-df8f-11e9-84a1-4365be5b69cb.png)

# 调试源码 #

## 代理如何织入 ##

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
										result = beanProcessor.postProcessAfterInitialization(result, name); // 乃成于此
									}

## 是否施加代理 ##

    AbstractAutoProxyCreator
    	postProcessAfterInitialization
			getInterceptorsAndAdvisorsForBean
![getInterceptorsAndAdvisorsForBean](https://user-images.githubusercontent.com/2216435/65667911-4b422e00-e073-11e9-8026-c119e4ffa8af.png)

	// 配置bean name，比对发现(private List beanNames)
    BeanNameAutoProxyCreator
		getInterceptorsAndAdvisorsForBean
    	
    
	// 根据接口等动态匹配发现，是Spring默认方式(DefaultAdvisorAutoProxyCreator)
	AbstractAdvisorAutoProxyCreator
		getInterceptorsAndAdvisorsForBean
			findEligibleAdvisors
				findCandidateAdvisors();
					BeanFactoryUtils.beanNamesIncludingAncestors(owningFactory, Advisor.class)
				eligibleAdvice=AopUtils.canApply(candidate, clazz, null) // 逐个比对可否施加于clazz
					// 由此可以看出pointcut应该由具体的Advisor，比如事物拦截器，性能拦截器
					// 来实现，以此仅对其应该施加的对象进行代理
					Pointcut
						ClassFilter
						MethodMatcher

如何使用不同的PointCut

	>由具体实现决定，可以继承既有StaticMethodMatcherPointcut，DynamicMethodMatcherPointcutAdvisor

动静匹配有何区别

# 研究点 #
## jdk dynamic proxy ##

## cglib ##

## threadlocal 内部实现 ##

## 类内部调用的问题 ##

## targetsource有什么用 ##

# 参考 #
[Spring源码分析](https://juejin.im/post/5ada8a5cf265da0b9347df8c)
