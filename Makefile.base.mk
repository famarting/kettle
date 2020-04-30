build:
	mvn clean install -DtrimStackTrace=false --no-transfer-progress -Pprod

build-native:
	mvn clean install -DtrimStackTrace=false --no-transfer-progress -Pnative -Dquarkus.native.container-build=true -Pprod

dev:
	mvn quarkus:dev

debug:
	mvn compile quarkus:dev -Dsuspend

test:
	mvn test -DtrimStackTrace=false