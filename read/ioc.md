# 1. beandefinition #
   bean 对应类在容器中的表示

抽象：

    org.springframework.beans.factory.config.BeanDefinition
    	MutablePropertyValues
    	ConstructorArgumentValues
    	getResourceDescription

具体实现：

    AbstractBeanDefinition
    	RootBeanDefinition
    	ChildBeanDefinition
# 2. beanfactory #
原始的容器是啥样的？

BeanDefinition注册容器

    BeanDefinitionRegistry
        /** Map of bean definition objects, keyed by bean name */
	    private Map beanDefinitionMap = new HashMap();

抽象容器

    BeanFactory
        核心方法：
        Object getBean(String name) throws BeansException

具体实现

	```
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
	```

# 测试验证 #

	org.springframework.beans.factory.xml.XmlListableBeanFactoryTests
		// 从xml中加载bean定义，并完成注册和容器初始化
		setUp
		
		// 先找当前容器，找不到则递归父亲容器
		testFactoryNesting


# 3. 何时初始化 #
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

# 4. beanfactorypostprocessor #
   做了什么
# 5. beanpostprocessor #
   做了什么