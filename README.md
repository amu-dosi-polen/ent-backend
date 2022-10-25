# ENT BACKEND

java11

tomcat9

## Installation

* Un fichier à créer en s'inspirant du .sample

`\src\main\resources\application.properties`

* Paire de clef/certificat à générer pour le chiffrement des JWT
```
openssl genpkey -algorithm rsa -outform PEM -out ./src/main/resources/JWTPrivateKey.pem 
openssl rsa -in ./src/main/resources/JWTPrivateKey.pem -outform PEM -pubout -out ./src/main/resources/JWTPublicKey.pem
```

* Utilisation de maven pour la compilation :
```
mvn clean compile package
cp -a target/ent-amu-backend /var/lib/tomcat9/webapps

```

## Lancement via Docker pour test
```
docker build . -t backend
docker run backend:latest
```

