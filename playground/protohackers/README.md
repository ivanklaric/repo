* Docker image build
docker image build -t echo-service .
* Docker container start
docker container run -P echo-service
* list all running containers
docker container ps
* list all available images
docker image ls
* kill a running container
docker kill $imageid
