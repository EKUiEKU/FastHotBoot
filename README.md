# MyHotDeployment
## 模块介绍
<ul>
    <li><strong>Common</strong> 存放公共基础类</li>
    <li><strong>HotDeploymentAgent</strong> 实现ASM热部署的工具</li>
    <li><strong>HotDeploymentInjector</strong> 注入器,将ASM热部署工具注入到目标程序中</li>
    <li><strong>HotDeploymentSample</strong> 基于Spring Boot的实例程序</li>
    <li><strong>HotDeploymentStarter</strong> 热部署的Spring Boot Starter</li>
    <li><strong>HotDeploymentWeb</strong> 用于操作热部署的后台管理系统</li>
</ul>

## 演示

### 1.启动HotDeploymentSample模块的演示程序
### 2.执行 [测试接口](http://localhost:8080/say), 此时接口返回 <i>hello world</i>
### 3.执行 [热部署接口](http://localhost:8080/deploy), 将SayService类中的<i>方法</i>都替换掉
### 4.执行 [测试接口](http://localhost:8080/say), 此时接口返回 <i>我被干掉啦</i> 热部署成功!

<h1>AMS熱部署原理</h1>
<img src="./img/流程图 (3).png">


### 部分开发进程如下, 目前正在开发热部署的后台管理系统和部署之间数据传输的安全校验问题。

## 7.16
**mod** HotDeploymentClassLoader, 当热部署的文件夹找不到类,则往上从父类里面找;<br>
**add** 程序启动的时候自动把上传的class部署到IOC容器中<br>
**add** 部署的是Controller,则将url的映射注册到IOC容器中;<br>
**fix** 修改单独打包,无法运行的问题;<br>
**mod** 将启动时从磁盘载入部署的信息从EventStart改到RefreshEvent;<br>
**mod** 重写HotDeploymentClass的equals和hashcode,当部署的路径、类名、包名一致的时候,认为是同一个对象。<br>

## 8.1
**add** 将原对象进行缓存,方便后期的字节码回溯;<br>
**add** 定义三种部署方式;<br>
**add** 开启Agent的远程调用。<br>

## 8.3
**add** add 编写Agent远程调用的全部署(REPLACE_CLASS)和部分部署模式(REPLACE_METHOD),并且通过测试。<br>

## 8.4
**add** add 编写Agent远程调用的回滚(ROLL_BACK)模式,并且通过测试。<br>