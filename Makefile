.PHONY: build docker-build docker-run all

all: docker-build docker-run

build:
	./gradlew clean build

docker-build:
	docker build -t expense-manager .

# --user 0:0 is a rootless-Docker workaround: container root = host user,
# so the bind-mounted outputs/ is writable. Image still defaults to non-root
# for Kubernetes deployments.
docker-run:
	docker run --rm \
	--user 0:0 \
	-v $(PWD)/src/main/resources/data:/app/src/main/resources/data \
	-v $(PWD)/outputs:/app/outputs \
	expense-manager
