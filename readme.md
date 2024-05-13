# spring 1.0源码阅读 #

## 古老版本

​	spring-framework 1.0大体开发于2003年，确实有点历史了。那为什么选择如此古老的版本呢？

​	之前学习源码的时候，总喜欢拉取最新版本开始研读，但其实新版本特性繁多，很容易让人迷失在细节里。而早期版本功能较少，没那么多细节，有利于理解核心框架设计。这也是偶然看到一篇文章提及，在实践上的一点感悟。



## idea下编译

​	大约四五年前读源码时，态度不及今天端正，如何编译都忽略了。今天因为准备面试的缘故再次复习，发现茫然于如何在idea下编译了？

​	因为源码工程古老，还在使用诸如ant，build.xml等上古神兵，本人都未曾接触过。导入idea后一篇茫然，连plugin都无法找到。后来删除导入，作为既有源码重新生成工程方可。大体如下：

<img src="https://github.com/aristotle0x01/spring-framework-1.0/assets/2216435/c7776803-b9a9-4e76-acaa-a39b35977847" alt="step1" width="70%"/>

<img src="https://github.com/aristotle0x01/spring-framework-1.0/assets/2216435/27ce8eee-8d15-46e6-9c7b-7566830cf5fe" alt="step2" width="60%"/>

<img src="https://github.com/aristotle0x01/spring-framework-1.0/assets/2216435/0ce31dac-11da-42a0-91cc-a04e44309c27" alt="step3" width="60%"/>

后面默认即可，idea会自动选择所有依赖"**lib/***.**jar**"及其它配置。其中“**language level**”:

<img src="https://github.com/aristotle0x01/spring-framework-1.0/assets/2216435/13a355a9-6638-4a13-949a-2b8de365f019" alt="langlevel" width="100%"/>



## 一张图

![aop代理和函数执行](https://user-images.githubusercontent.com/2216435/65811550-de52a380-e1ec-11e9-8ec0-7c6299d13e8d.png)
