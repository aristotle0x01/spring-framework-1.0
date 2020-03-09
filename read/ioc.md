# 0. ioc核心接口设计
![spring ioc核心设计](https://user-images.githubusercontent.com/2216435/65381357-0c824000-dd22-11e9-9c33-4b9c19dc13ab.png)

# 1. beandefinition
xml或者code中bean定义的对应定义及其实现：The main intention is to allow BeanFactoryPostProcessors (like PropertyPlaceholderConfigurer) to access and modify property values.

抽象：

    org.springframework.beans.factory.config.BeanDefinition
    	MutablePropertyValues
    	ConstructorArgumentValues
    	getResourceDescription

具体实现：

    AbstractBeanDefinition
    	RootBeanDefinition
    	ChildBeanDefinition // 继承parent bean
# 2. beanfactory
原始的容器是啥样的？

BeanDefinition注册容器

    BeanDefinitionRegistry
        /** Map of bean definition objects, keyed by bean name */
	    private Map beanDefinitionMap = new HashMap();

抽象容器

    BeanFactory
        核心方法：
        Object getBean(String name) throws BeansException
    
    bean name是如何确定的？
        一般是xml中的bean id

具体实现

	XmlBeanFactory
		XmlBeanDefinitionReader.loadBeanDefinitions
			registerBeanDefinitions(Document doc, Resource resource)
				DefaultXmlBeanDefinitionParser.registerBeanDefinitions
					for (int i = 0; i < nl.getLength(); i++) {
						loadBeanDefinition((Element) node);
							parseBeanDefinition
								// 类加载，设置相关参数等
								Class clazz = Class.forName(className, true, this.beanClassLoader);
								rbd = new RootBeanDefinition(clazz, cargs, pvs);
								rbd.setInitMethodName(initMethodName);
								rbd.setDestroyMethodName(destroyMethodName);
								......
							// 完成注册，实际加入容器
							this.beanFactory.registerBeanDefinition(id, beanDefinition);
					}

### 测试验证

	org.springframework.beans.factory.xml.XmlListableBeanFactoryTests
		// 从xml中加载bean定义，并完成注册和容器初始化
		setUp
		
		// 先找当前容器，找不到则递归父亲容器
		testFactoryNesting


# 3. 实例容器+Bean初始化 
getbean会发生什么?

以XmlListableBeanFactoryTests.testFactoryNesting为例：

**AbstractBeanFactory:**

>This class provides singleton/prototype determination, singleton cache,aliases, FactoryBean handling, and bean definition merging for child bean definitions. It also allows for management of a bean factory hierarchy, implementing the HierarchicalBeanFactory interface.
	
	/** Cache of singletons: bean name --> bean instance */
	private final Map singletonCache = Collections.synchronizedMap(new HashMap());

	/** BeanPostProcessors to apply in createBean */
	private final List beanPostProcessors = new ArrayList();

    /** Parent bean factory, for bean inheritance support */
	private BeanFactory parentBeanFactory;
	
	getBean
		if singletonCache.get(beanName);
		else createBean *// AbstractAutowireCapableBeanFactory*
				// 递归依赖
				getBean(bean.getDependsOn()[i]);
				
				// 生成对象
				new BeanWrapperImpl(bean.getBeanClass());
						clazz.newInstance()

				// 设置属性
				applyPropertyValues
					// 递归创建可能的对象类型属性
					resolveValueIfNecessary(beanName, ...);
					// set之
					deepCopy.setPropertyValueAt(pv, i);
				
				// 后置处理
				BeanNameAware
				BeanFactoryAware
				bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
				invokeInitMethods(bean, beanName, mergedBeanDefinition);
				bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);

# 4. BeanPostProcessor
> public interface BeanPostProcessor
> 
> Factory hook that allows for custom modification of new bean instances, e.g. checking for marker interfaces or wrapping them with proxies.
> ApplicationContexts can autodetect BeanPostProcessor beans in their bean definitions and apply them to any beans subsequently created. Plain bean factories allow for programmatic registration of post-processors, applying to all beans created through this factory.
> 
> Typically, post-processors that populate beans via marker interfaces or the like will implement postProcessBeforeInitialization(java.lang.Object, java.lang.String), while post-processors that wrap beans with proxies will normally implement postProcessAfterInitialization(java.lang.Object, java.lang.String).

![bean生命周期](https://user-images.githubusercontent.com/2216435/65381356-0ab87c80-dd22-11e9-8e31-901d8e7bf9fb.png)

# 5. Bean实例化时机&ApplicationContext
### 何时触发实例化
	// spring boot
	SpringApplication.run	
		refreshContext
			// AbstractApplicationContext
			refresh	
				finishBeanFactoryInitialization
					beanFactory.preInstantiateSingletons
						getBean

# 6. BeanFactoryPostProcessor

原始的BeanFactory这一层并未做实现，要到ApplicationContext层

> public interface BeanFactoryPostProcessor
> Allows for custom modification of an application context's bean definitions, adapting the bean property values of the context's underlying bean factory.
> Application contexts can auto-detect BeanFactoryPostProcessor beans in their bean definitions and apply them before any other beans get created.
> 
> Useful for custom config files targeted at system administrators that override bean properties configured in the application context.
> 
> See PropertyResourceConfigurer and its concrete implementations for out-of-the-box solutions that address such configuration needs.
> 
> A BeanFactoryPostProcessor may interact with and modify bean definitions, but never bean instances. Doing so may cause premature bean instantiation, violating the container and causing unintended side-effects. If bean instance interaction is required, consider implementing BeanPostProcessor instead.

![生命周期](https://user-images.githubusercontent.com/2216435/65381707-7fdb8000-dd29-11e9-8a08-8f4f2acce4d4.png)

# 7. 基于注解的bean加载扫描
	// spring boot
	SpringApplication.run	
		refreshContext
			// AbstractApplicationContext
			refresh
			    // Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory)
					// PostProcessorRegistrationDelegate
					invokeBeanFactoryPostProcessors
					    // postProcessor
						postProcessBeanDefinitionRegistry(registry)
						    // ConfigurationClassPostProcessor
							processConfigBeanDefinitions(registry)
								// ConfigurationClassParser
								parse
									processConfigurationClass
										doProcessConfigurationClass
											// Process any @ComponentScan annotations
											// sourceClass 此时为加了@SpringBootApplication main方法启动类
											Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
													sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
											if (!componentScans.isEmpty() &&
													!this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
												for (AnnotationAttributes componentScan : componentScans) {
													// The config class is annotated with @ComponentScan -> perform the scan immediately
													// 加载所有@Configuration,@Service, @Component, @Repository, @Controller注解的类
													Set<BeanDefinitionHolder> scannedBeanDefinitions =
															this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
													// Check the set of scanned definitions for any further config classes and parse recursively if needed
													for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
														if (ConfigurationClassUtils.checkConfigurationClassCandidate(
																holder.getBeanDefinition(), this.metadataReaderFactory)) {
																// 处理 @Configuration 中的bean定义
																parse(holder.getBeanDefinition().getBeanClassName(), holder.getBeanName());
														}
													}
												}
											}								
			    
			    ...
			 
			    // bean的实例化
				finishBeanFactoryInitialization


![WechatIMG509](https://user-images.githubusercontent.com/2216435/75622934-81ff1000-5be0-11ea-965b-3b7b5159aed2.jpg)

# 8. 值得研究的点
* nacos适配配置中心加载机制
* 事件机制，刷新时机
* 工厂方法
* 基于aware和postprocessor的扩展点
* 多重接口的层次设计
* wrapper方法和propertyvalue的抽象，避免直接反射
* 异常机制的设计

# 9. 参考
[Spring bean的生命流程](https://segmentfault.com/a/1190000010734016)

[Spring IOC 容器源码分析系列文章导读](https://segmentfault.com/a/1190000015089790)

[Spring 源码分析(二) —— 核心容器](https://my.oschina.net/kaywu123/blog/614325)