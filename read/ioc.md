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
   getbean会发生什么

# 4. beanfactorypostprocessor #
   做了什么
# 5. beanpostprocessor #
   做了什么