The following libraries are included in the Spring Framework distribution because they are
required either for building the framework or for running the sample apps. Note that each
of these libraries is subject to the respective license; check the respective project
distribution/website before using any of them in your own applications.

* ant/ant.jar
- Ant 1.6.1 (http://ant.apache.org)
- used to build the framework and the sample apps

* aopalliance/aopalliance.jar
- AOP Alliance 1.0 (http://aopalliance.sourceforge.net)
- required for building the framework
- required at runtime when using AOP functionality

* axis/axis.jar, axis/saaj.jar, axis/wsdl.jar
- Apache Axis 1.1 (http://ws.apache.org/axis)
- required for running JPetStore

* caucho/burlap-2.1.12.jar
- Burlap 2.1.12 (http://www.caucho.com/burlap)
- required for building the framework
- required at runtime when Spring's Burlap remoting support

* caucho/hessian-2.1.12.jar
- Hessian 2.1.12 (http://www.caucho.com/hessian)
- required for building the framework
- required at runtime when Spring's Hessian remoting support

* cglib/cglib-2.0.jar, cglib/asm.jar
- CGLIB 2.0 with ObjectWeb ASM 1.4 (http://cglib.sourceforge.net)
- required for building the framework
- required at runtime when proxying full target classes via Spring AOP

* cos/cos.jar
- Jason Hunter's COS 05Nov02 (http://www.servlets.com/cos)
- required for building the framework
- required at runtime when using Spring's CosMultipartResolver or CosMailSender

* dom4j/dom4j.jar
- DOM4J 1.4 XML parser (http://dom4j.sourceforge.net)
- required for running Petclinic (by Hibernate)

* easymock/easymock.jar
- EasyMock 1.0.1 (http://www.easymock.org)
- required for building the test suite

* freemarker/freemarker.jar
- FreeMarker 2.3 RC3 (http://www.freemarker.org)
- required for building the framework
- required at runtime when using Spring's FreeMarker support

* hibernate/ehcache.jar
- EHCache 0.6 (http://ehcache.sourceforge.net)
- required for running Petclinic (by Hibernate)

* hibernate/hibernate2.jar, hibernate/odmg.jar
- Hibernate 2.1.2 (http://www.hibernate.org)
- required for building the framework
- required at runtime when using Spring's Hibernate support

* hsqldb/hsqldb.jar
- HSQLDB 1.7.1 (http://hsqldb.sourceforge.net)
- required for running JPetStore and Petclinic

* ibatis/ibatis-common.jar, ibatis/ibatis-sqlmap.jar, ibatis/ibatis-sqlmap-2.jar
- iBATIS SQL Maps 1.3.1 and 2.0 RC1 (http://www.ibatis.com)
- required for building the framework
- required at runtime when using Spring's iBATIS SQL Maps support

* itext/itext-1.02b.jar
- iText PDF 1.02 (http://www.lowagie.com/itext)
- required for building the framework
- required at runtime when using Spring's AbstractPdfView

* j2ee/activation.jar
- JavaBeans Activation Framework 1.0.1 (http://java.sun.com/products/javabeans/glasgow/jaf.html)
- required for building the framework
- required at runtime when using Spring's JavaMailSender

* j2ee/ejb.jar
- Enterprise JavaBeans API 2.0 (http://java.sun.com/products/ejb)
- required for building the framework
- required at runtime when using Spring's EJB support

* j2ee/jaxrpc.jar
- JAX-RPC API 1.0 (http://java.sun.com/xml/jaxrpc)
- required for building the framework
- required at runtime when using Spring's JAX-RPC support

* j2ee/jdbc2_0-stdext.jar
- JDBC 2.0 Standard Extensions (http://java.sun.com/products/jdbc)
- required for building the framework on J2SE 1.3
- required at runtime when using Spring's JDBC support on J2SE 1.3

* j2ee/jms.jar
- Java Message Service API 1.0.2b (java.sun.com/products/jms)
- required for building the framework
- required at runtime when using Spring's AbstractJmsMessageDrivenBean

* j2ee/jstl.jar
- JSP Standard Tag Library API 1.0 (http://java.sun.com/products/jstl)
- required for building the framework
- required at runtime when using Spring's JstlView

* j2ee/jta.jar
- Java Transaction API 1.0.1b (http://java.sun.com/products/jta)
- required for building the framework
- required at runtime when using Spring's JtaTransactionManager

* j2ee/mail.jar
- JavaMail 1.2 (http://java.sun.com/products/javamail)
- required for building the framework
- required at runtime when using Spring's JavaMailSender

* j2ee/servlet.jar
- Servlet API 2.3, including JSP 1.2 (http://java.sun.com/products/servlet)
- required for building the framework
- required at runtime when using Spring's web support

* j2ee/xml-apis.jar
- JAXP, DOM and SAX APIs (taken from Xerces 2.6 distribution; http://xml.apache.org/xerces2-j)
- required for building the framework on J2SE 1.3
- required at runtime when using Spring's XmlBeanFactory on J2SE 1.3

* jakarta-commons/commons-attributes-*-SNAPSHOT.jar
- Commons Attributes March 19th snapshot (http://jakarta.apache.org/commons/sandbox/attributes)
- required for building the framework
- required at runtime when using Spring's Commons Attributes support

* jakarta-commons/commons-beanutils.jar
- Commons BeanUtils 1.6 (http://jakarta.apache.org/commons/beanutils)
- required for running JPetStore's Struts web tier

* jakarta-commons/commons-collections.jar
- Commons Collections 2.1 (http://jakarta.apache.org/commons/collections)
- required for running JPetStore's Struts web tier
- required for running Petclinic (by Hibernate)

* jakarta-commons/commons-dbcp.jar
- Commons DBCP 1.1 (http://jakarta.apache.org/commons/dbcp)
- required for building the framework
- required at runtime when using Spring's CommonsDbcpNativeJdbcExtractor
- required for running JPetStore and Image Database

* jakarta-commons/commons-digester.jar
- Commons Digester 1.5 (http://jakarta.apache.org/commons/digester)
- required for running JPetStore's Struts web tier

* jakarta-commons/commons-discovery.jar
- Commons Discovery 0.2 (http://jakarta.apache.org/commons/discovery)
- required for running JPetStore (by Axis)

* jakarta-commons/commons-fileupload.jar
- Commons FileUpload 1.0 (http://jakarta.apache.org/commons/fileupload)
- required for building the framework
- required at runtime when using Spring's CommonsMultipartResolver

* jakarta-commons/commons-lang.jar
- Commons Lang 1.0 (http://jakarta.apache.org/commons/lang)
- required for running JPetStore's Struts web tier

* jakarta-commons/commons-logging.jar
- Commons Logging 1.0.3 (http://jakarta.apache.org/commons/logging)
- required for building the framework
- required at runtime, as Spring uses it for all logging

* jakarta-commons/commons-pool.jar
- Commons Pool 1.1 (http://jakarta.apache.org/commons/pool)
- required for running JPetStore and Image Database

* jakarta-commons/commons-validator.jar
- Commons Validator 1.0.2 (http://jakarta.apache.org/commons/validator)
- required for running JPetStore's Struts web tier

* jakarta-taglibs/standard.jar
- Jakarta's JSTL implementation 1.0.5 (http://jakarta.apache.org/taglibs)
- required for running JPetStore, Petclinic, Countries, and Tiles Example

* jboss/jboss-common-jdbc-wrapper.jar
- JBoss connection pool classes (http://www.jboss.org)
- required for building the framework
- required at runtime when using Spring's JbossNativeJdbcExtractor

* jdo/jdo.jar
- JDO API 1.0.1 (http://access1.sun.com/jdo)
- required for building the framework
- required at runtime when using Spring's JDO support

* jdom/jdom.jar
- JDOM 1.0 beta 9 (http://www.jdom.org)
- required at runtime by iBATIS SQL Maps 1.3.1

* jotm/jotm.jar
- JOTM 1.4.3 (http://jotm.objectweb.org)
- required for building the framework
- required at runtime when using Spring's JotmFactoryBean

* jotm/xapool.jar
- XAPool 1.3.1 (taken from the JOTM distribution)
- required for building the framework
- required at runtime when using Spring's XAPoolNativeJdbcExtractor

* junit/junit.jar
- JUnit 3.8.1 (http://www.junit.org)
- required for building the test suite

* log4j/log4j-1.2.8.jar
- Log4J 1.2.8 (http://logging.apache.org/log4j)
- required for building the framework
- required at runtime when using Spring's Log4jConfigurer

* mockobjects/mockobjects-*-0.09.jar
- MockObjects 0.09 (http://www.mockobjects.com)
- required for building the test suite

* poi/poi-2.5.jar
- Apache POI 2.5 (http://jakarta.apache.org/poi)
- required for building the framework
- required at runtime when using Spring's AbstractExcelView

* quartz/quartz.jar
- Quartz 1.3.2 (http://www.quartzscheduler.org)
- required for building the framework
- required at runtime when using Spring's Quartz scheduling support

* rexexp/jakarta-oro-2.0.7.jar
- Jakarta ORO 2.0.7 regular expression parser (http://jakarta.apache.org/oro)
- required for building the framework
- required at runtime when using Spring's RegexpMethodPointcut

* struts/struts-1.1.jar
- Apache Struts 1.1 (http://jakarta.apache.org/struts)
- required for building the framework
- required at runtime when using Spring's TilesView
- required for running JPetStore's Struts web tier

* velocity/velocity-1.4-rc1.jar
- Velocity 1.4 RC1 (http://jakarta.apache.org/velocity)
- required for building the framework
- required at runtime when using Spring's VelocityView

* xdoclet/xjavadoc-1.0.jar
- XDoclet 1.0 (http://xdoclet.sourceforge.net)
- used by Commons Attributes to parse source-level metadata in the build process
- required for building the framework and the attributes version of JPetStore

