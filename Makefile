PROJECTS = api-server integration-tests
DOCKER_TARGETS = docker_build docker_tag docker_push

all: build 
docker: $(DOCKER_TARGETS)

build: $(PROJECTS)
$(PROJECTS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

run-infinispan:
	docker run -it -p 11222:11222 -p 9990:9990 -e "MGMT_USER=admin" -e "MGMT_PASS=admin" jboss/infinispan-server:10.0.0.Beta2

run-api-server:
	$(MAKE) -C api-server deploy

run-integration-tests:
	$(MAKE) -C integration-tests test

$(DOCKER_TARGETS): $(PROJECTS)
$(PROJECTS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: build $(DOCKER_TARGETS) $(PROJECTS) test deploy run-integration-tests

