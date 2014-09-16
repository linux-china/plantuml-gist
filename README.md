PlantUML Gist
======================================
Render PlantUML from Github Gist & Gitlab

### Features

* Github & Gist support
* Gitlab support
* Error Image
* Cache support

### Gitlab

Please create 'plantuml' account on your gitlab, and set token info in web.xml. 
If you want to render puml files on your repository, please add plantuml as your project developer member as readonly.

the Demo url is: 
http://localhost:8080/gitlab/namespace/repository_1/blob/master/your_plantuml.puml

### Manual Install

please modify web.xml and change 'baseUrl', 'gitlabUrl' and 'userToken', then deploy it to tomcat.
Please install following components:

* Graphviz: yum install Graphviz [\[http://www.graphviz.org/\]][1]
* Unicode Fonts:  yum install fonts-chinese
* puml file utf-8 encoding
       
### Cache Strategy

* 2 minutes for url path on server side, no Cache-Control header.
* Cache for plantuml's image according to puml file's md5
* Cache for puml file content according to gist id or gitlab path if remote request failed.

You can modify ehcache.xml to adjust cache strategy.

### Todo

* Stash Integration
* Bitbucket Integration




PlantUML Gist
======================================
Render PlantUML from Github Gist & Gitlab
根据 Github Gist 和 Gitlab 中的 puml 文件渲染 PlantUML 图

### 特性

* Github & Gist support
* Gitlab support
* Error Image
* Cache support

### Gitlab
在 gitlab 中创建 'plantuml' 账号，并将获得的 token 配置在 web.xml 中
如果你想在本地进行 puml 文件的渲染，请先将 plantuml 作为只读用户添加到你的项目中去
Please create 'plantuml' account on your gitlab, and set token info in web.xml. 
If you want to render puml files on your repository, please add plantuml as your project developer member as readonly.

Demo 地址为：
the Demo url is: 
http://localhost:8080/gitlab/namespace/repository_1/blob/master/your_plantuml.puml

### 安装手册
修改替换 web.xml 中的 'baseUrl', 'gitlabUrl' 以及 'userToekn', 将应用部署到 tomcat 中
在此之前请确保安装以下组件：

* Graphviz: yum install Graphviz [\[http://www.graphviz.org/\]][2]
* Unicode Fonts: yum install fonts-chinese
* puml file utf-8 encoding
       
### 缓存策略

* URL 路径将在服务端保留2分钟，请求不带 Cache-Control 头
* puml 生成的图片将根据 puml 文件的 MD5 值进行缓存
* 如果远程请求失败，puml 文件内容将根据 gist id 或者 gitlab 路径来缓存

你可以修改 ehcache.xml 文件来调整缓存策略


### Stash整合

* use browser api
* 验证走http basic

### 规划

* Stash Integration
* Bitbucket Integration


  [1]: http://www.graphviz.org/
  [2]: http://www.graphviz.org/
