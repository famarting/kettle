build:
	mvn clean install -DtrimStackTrace=false --no-transfer-progress

build-ctl:
	mvn clean install -DtrimStackTrace=false --no-transfer-progress -pl !kettle-ctl
	mvn install -DtrimStackTrace=false --no-transfer-progress -Pnative -Dquarkus.native.container-build=true -Dquarkus.profile=ctl -pl kettle-ctl

set-up-ctl:
	sudo ln -s $$(pwd)/kettle-ctl/target/kettle-ctl-1.0-SNAPSHOT-runner /usr/local/bin/kettle
	
run-mongo:
#-e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin
	docker run -it -p 27017:27017 mongo:4.0-xenial

run-api-server:
	$(MAKE) -C api-server deploy