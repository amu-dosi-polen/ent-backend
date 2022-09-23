# ENT BACKEND

java11

tomcat9

## Installation

* Un fichier à créer en s'inspirant du .sample

`\src\main\resources\application.properties`

* Utilisation de maven pour la compilation :
```
mvn clean compile package
cp -a target/ent-amu-backend /var/lib/tomcat9/webapps
```