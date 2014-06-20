PlantUML Gist
======================================
Render PlantUML from Github Gist & Gitlab

### Features

* gist & gitlab support
* Local cache in 2 minutes
* Error Image

### gitlab

Please create 'plantuml' account on your gitlab, and set token info in web.xml. 
If you want to render puml files on your repository, please add plantuml as your project developer member as readonly.

the Demo url is: 
http://localhost:8080/gitlab/namespace/repository_1/blob/master/your_plantuml.puml

### Manual Install

please modify web.xml and change 'baseUrl', 'gitlabUrl' and 'userToken', then deploy it to tomcat.
       