
deploy:
	mvn package
	java -jar target/api-server-1.0-SNAPSHOT-runner.jar

dev:
	mvn quarkus:dev

debug:
	mvn compile quarkus:dev -Dsuspend

.PHONY: deploy
