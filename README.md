PlantUML Gist
======================================
Render PlantUML from Github Gist & Gitlab

### Features

* gist & gitlab support
* Error Image
* Cache support

### gitlab

Please create 'plantuml' account on your gitlab, and set token info in web.xml. 
If you want to render puml files on your repository, please add plantuml as your project developer member as readonly.

the Demo url is: 
http://localhost:8080/gitlab/namespace/repository_1/blob/master/your_plantuml.puml

### Manual Install

please modify web.xml and change 'baseUrl', 'gitlabUrl' and 'userToken', then deploy it to tomcat.
       
### Cache Strategy

* 2 minutes for url path on server side, no Cache-Control header.
* Cache for plantuml's image according to puml file's md5
* Cache for puml file content according to gist id or gitlab path if remote request failed.

You can modify ehcache.xml to adjust cache strategy.