# heroku-simple-web-app
Quickstart wro4j application. 
This application is deployed on [heroku](http://wro4j-quickstart.herokuapp.com/).

The application uses webjars provided as maven dependencies which are bundled inside a single resource merged by wro4j.

# Run locally

## Using Jetty maven plugin
    
    mvn jetty:run

## Using starter script
    
    mvn clean package
    sh target/appassembler/bin/app
    