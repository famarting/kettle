
deploy:
	mvn package
	java -jar target/api-server-1.0-SNAPSHOT-runner.jar

dev:
	mvn quarkus:dev

debug:
	mvn compile quarkus:dev -Dsuspend

native:
	mvn package -Pnative -Dquarkus.native.container-build=true

run-native:
	./target/api-server-1.0-SNAPSHOT-runner

.PHONY: deploy
