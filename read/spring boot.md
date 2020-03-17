# spring boot

## 优势
![image](https://user-images.githubusercontent.com/2216435/76397408-f93d5c80-63b5-11ea-9251-a749e0785761.png)

* 综合利用java and groovy开发
* 减少开发时间，提高效率
* 去除边角代码，xml配置
* 集成了tomcat等web容器
* 提供了cli工具，方便开发测试

[Spring Boot Tutorial](https://www.journaldev.com/7969/spring-boot-tutorial)

## 关键组件

![image](https://user-images.githubusercontent.com/2216435/76399123-11fb4180-63b9-11ea-8de5-b8c34086341d.png)

* Spring Boot Starters：simplifies project build dependencies
* Spring Boot AutoConfigurator：reduces the Spring Configuration
* Spring Boot CLI：run and test from command line
* Spring Boot Actuator: Management EndPoints & Applications Metrics

[Key Components and Internals of Spring Boot Framework](https://www.journaldev.com/7989/key-components-and-internals-of-spring-boot-framework)

## spring boot conditons
* @ConditionalOnBean
* @ConditionalOnClass
* @ConditionalOnExpression
* @ConditionalOnMissingBean
* @ConditionalOnMissingClass
* @ConditionalOnNotWebApplication
* @ConditionalOnResource
* @ConditionalOnWebApplication

[condition test](https://github.com/selfpoised/spring-boot-examples/tree/master/spring-boot-helloWorld)

## spring boot cli
[cli groovy test](https://github.com/selfpoised/spring-boot-examples/tree/master/spring-boot-helloWorld)

## 自定义starter
其核心在于遵守spring boot的约定：

	resources
		META-INF
			spring.factories
				org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.xxx.yyy.YourAutoConfiguration
				
![image](https://user-images.githubusercontent.com/2216435/76679202-613fad00-6619-11ea-9030-c5f59bc4a6ca.png)

[spring-boot-helloWorld](https://github.com/selfpoised/spring-boot-examples/tree/master/spring-boot-helloWorld)

[实战|如何自定义SpringBoot Starter？](https://github.com/selfpoised/spring-boot-examples/tree/master/spring-boot-helloWorld)

## Spring 自动配置核心

	@SpringBootApplication
		@SpringBootConfiguration
			@Configuration
		@EnableAutoConfiguration
			@AutoConfigurationPackage
				@Import(AutoConfigurationPackages.Registrar.class)
					AutoConfigurationPackages
			@Import(AutoConfigurationImportSelector.class)
		@ComponentScan

同样以之为例[spring-boot-helloWorld](https://github.com/selfpoised/spring-boot-examples/tree/master/spring-boot-helloWorld)

#### AutoConfigurationPackages
将spring boot 启动类所在的包："com.neo", 注册为BasePackages.class类型的beandefinition，后面AutoConfigurationImportSelector来看有什么用

#### AutoConfigurationImportSelector
见ConfigurationClassPostProcessor

#### ConfigurationClassPostProcessor

	AbstractApplicationContext
		refresh
			invokeBeanFactoryPostProcessors
			    // BeanDefinitionRegistryPostProcessor
			    // 支持扩展方式进行的bean定义
				invokeBeanDefinitionRegistryPostProcessors
					for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors)
						// ConfigurationClassPostProcessor
						// @Configuration注解处理器
						postProcessor.postProcessBeanDefinitionRegistry(registry);
							// ConfigurationClassPostProcessor
							// 拿到启动类SpringApplication bean
							processConfigBeanDefinitions
								// ConfigurationClassParser
								parse
									deferredImportSelectorHandler.process
										// DeferredImportSelectorGroupingHandler
										processGroupImports
											getImports
											   // AutoConfigurationGroup
												process
													// AutoConfigurationImportSelector
													getAutoConfigurationEntry
														// SpringFactoriesLoader.loadFactoryNames：META-INF/spring.factories 定义加载					
														getCandidateConfigurations
														// 再完成各种AutoConfiguration过滤（排除不符合条件者）
														
*BeanDefinitionRegistryPostProcessor*
![BeanDefinitionRegistryPostProcessor](https://user-images.githubusercontent.com/2216435/76842321-0aceaa80-6875-11ea-9b9e-25e08422c713.png)
*ConfigurationClassPostProcessor*
![ConfigurationClassPostProcessor](https://user-images.githubusercontent.com/2216435/76842413-3356a480-6875-11ea-987a-662774565c3b.png)

#### import注解

## 待研究点
### Groovy lang

![image](https://user-images.githubusercontent.com/2216435/76399550-bda49180-63b9-11ea-99e3-66dc8da5eae6.png)

### spring boot初始化过程，消息通知，资源加载等
## 参考
[Understanding Spring Boot
](https://geowarin.com/understanding-spring-boot/)

[深入springboot原理——一步步分析springboot启动机制（starter机制）
](https://www.cnblogs.com/hjwublog/p/10332042.html)

