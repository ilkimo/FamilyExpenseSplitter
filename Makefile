.PHONY: build docker-build docker-run all

# Get the current user UID and GID
USER_ID=$(shell id -u)
GROUP_ID=$(shell id -g)

all: docker-build docker-run

build:
	./gradlew clean build

docker-build:
	docker build --build-arg USER_ID=$(USER_ID) --build-arg GROUP_ID=$(GROUP_ID) -t expense-manager .

docker-run:
	docker run --rm \
	-v $(PWD)/src/main/resources/data:/app/src/main/resources/data \
	-v $(PWD)/outputs:/app/outputs \
	expense-manager
