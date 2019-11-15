
run-mongo:
#-e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin
	docker run -it -p 27017:27017 mongo:4.0-xenial

run-api-server:
	$(MAKE) -C api-server deploy

run-tests:
	$(MAKE) -C api-server test

native-api-server:
	$(MAKE) -C api-server native

run-native-api-server:
	$(MAKE) -C api-server run-native

.PHONY: run-mongo run-mongo-background

