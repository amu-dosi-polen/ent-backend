#!/bin/bash
mvn clean compile package
rm -rf /var/lib/tomcat9/webapps/ent-amu-backend-old
mv  /var/lib/tomcat9/webapps/ent-amu-backend /var/lib/tomcat9/webapps/ent-amu-backend-old
cp -a target/ent-amu-backend /var/lib/tomcat9/webapps
