PROJECTS = api-server integration-tests
DOCKER_TARGETS = docker_build docker_tag docker_push

all: build 
docker: $(DOCKER_TARGETS)

build: $(PROJECTS)
$(PROJECTS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

run-mongo:
#-e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin
	docker run -it -p 27017:27017 mongo:4.0-xenial

run-api-server:
	$(MAKE) -C api-server deploy

run-integration-tests:
	$(MAKE) -C integration-tests test

native-api-server:
	$(MAKE) -C api-server native

run-native-api-server:
	$(MAKE) -C api-server run-native


$(DOCKER_TARGETS): $(PROJECTS)
$(PROJECTS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: build $(DOCKER_TARGETS) $(PROJECTS) test deploy run-integration-tests

