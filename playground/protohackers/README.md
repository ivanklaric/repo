* Docker image build  
```
docker image build -t echo-service .
```
* Docker container start  
```
docker container run -P echo-service
```
* list all running containers
```
docker container ps
```
* list all available images
```
docker image ls
```
* kill a running container
```
docker kill $imageid
```
* tag an image
```
docker tag echo-service:latest $AWS_ACC_ID.dkr.ecr.$AWS_REGION.amazonaws.com/protohackers/echo-service
```
* push an image
```
docker push $AWS_ACC_ID.dkr.ecr.$AWS_REGION.amazonaws.com/protohackers/echo-service
```
* create ECR first with terraform
```
terraform apply -target=aws_ecr_repository.protohackers-ecr
```

