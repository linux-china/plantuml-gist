<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="PlantUML for Github Gist">
    <meta name="author" content="linux_china">
    <title>PlantUML with Github support</title>
    <link href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/jumbotron-narrow.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <div class="header">
        <ul class="nav nav-pills pull-right">
            <li><a href="/">Plantuml Gist</a></li>
            <li><a href="/index_github.jsp">Plantuml Github</a></li>
            <li><a href="/index_gitlab.jsp">Plantuml Gitlab</a></li>
            <li><a href="https://github.com/linux-china/plantuml-gist" target="_blank">Source</a></li>
        </ul>
        <h3 class="text-muted">PlantUML Gravizo</h3>
    </div>

    <div class="jumbotron">
        <h1>PlantUML Gravizo</h1>

        <p class="lead">Render PlantUML graph in any document in github,gitlab,bitbucket etc.
        </p>

    </div>

    <div class="row marketing">
        <h3>How to use?</h3>
        <ul>
          <li>
            <p>markdown</p>
            <pre><code>![Alt Text](http://g.gravizo.com/g?
          @startuml

          actor User

          participant &quot;First Class&quot; as A
          participant &quot;Second Class&quot; as B
          participant &quot;Last Class&quot; as C
          User -&gt; A: DoWork
          activate A
          A -&gt; B: Create Request
          activate B
          B -&gt; C: DoWork
          activate C
          C --&gt; B: WorkDone
          destroy C
          B --&gt; A: Request Created
          deactivate B
          A --&gt; User: Done
          deactivate A

          @enduml
        )
        </code></pre>
          </li>
          <li>
            <p>html: </p>
            <pre><code>  &lt;img src=&#39;http://g.gravizo.com/g?
           @startuml

           actor User

           participant &quot;First Class&quot; as A
           participant &quot;Second Class&quot; as B
           participant &quot;Last Class&quot; as C
           User -&gt; A: DoWork
           activate A
           A -&gt; B: Create Request
           activate B
           B -&gt; C: DoWork
           activate C
           C --&gt; B: WorkDone
           destroy C
           B --&gt; A: Request Created
           deactivate B
           A --&gt; User: Done
           deactivate A

           @enduml
          &#39;/&gt;
        </code></pre>
          </li>
        </ul>
    </div>

    <div class="footer">
        <p>&copy; MvnSearch 2013 &nbsp;
            Links: &nbsp; <a href="http://plantuml.sourceforge.net" target="_blank">PlantUML</a>
            &nbsp; <a href="https://gist.github.com" target="_blank">GithubGist</a>
        </p>
    </div>
</div>
</body>
</html>
