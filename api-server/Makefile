
deploy:
	mvn package -DskipTests
	java -jar target/api-server-1.0-SNAPSHOT-runner.jar

dev:
	mvn quarkus:dev

debug:
	mvn compile quarkus:dev -Dsuspend

test:
	mvn test

native:
	mvn package -Pnative -Dquarkus.native.container-build=true

run-native:
	./target/api-server-1.0-SNAPSHOT-runner

container:
	docker build -f src/main/docker/Dockerfile.jvm -t api-server-jvm .

container-native:
	docker build -f src/main/docker/Dockerfile.native -t api-server-native .

.PHONY: deploy
