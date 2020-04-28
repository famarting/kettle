build:
	mvn clean install -DtrimStackTrace=false

deploy:
	mvn package -DskipTests
	java -jar target/*-runner.jar

dev:
	mvn quarkus:dev

debug:
	mvn compile quarkus:dev -Dsuspend

test:
	mvn test -DtrimStackTrace=false

native:
	mvn package -Pnative -Dquarkus.native.container-build=true

run-native:
	./target/*-runner

container:
	docker build -f src/main/docker/Dockerfile.jvm -t api-server-jvm .

container-native:
	docker build -f src/main/docker/Dockerfile.native -t api-server-native .

.PHONY: deploy
