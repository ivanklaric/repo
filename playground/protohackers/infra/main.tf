terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

variable "region" {
  default = "eu-north-1"
  type = string
}

provider "aws" {
  region = var.region
}

resource "aws_ecr_repository" "protohackers-ecr" {
  name = "protohackers/echo-service"

  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecs_cluster" "protohackers_cluster" {
  name = "protohackers-ecs-cluster"
}

variable "retention_in_days" {
  default = 1
  type = number
}

variable "memory" {
  description = "ECS task memory in MB"
  default     = 512
  type        = number
}

variable "cpu_units" {
  description = "ECS task CPU units (1024 = 1 vCPU)"
  default     = 256
  type        = number
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "protohackers_ecs_taskexecutionrole"
  assume_role_policy = data.aws_iam_policy_document.task_assume_role_policy.json
}

data "aws_iam_policy_document" "task_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"

      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_cloudwatch_log_group" "log_group" {
  name              = "/protohackers/ecs/echo-service"
  retention_in_days = var.retention_in_days
}

resource "aws_ecs_task_definition" "echo_service_task" {
  family                   = "protohackers-ecs-taskdefinition"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  cpu                      = var.cpu_units
  memory                   = var.memory

  container_definitions = jsonencode([
    {
      name      = "echo-service"
      image     = "${aws_ecr_repository.protohackers-ecr.repository_url}:latest"
      cpu       = var.cpu_units
      memory    = var.memory
      essential = true
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "prime_"
          "awslogs-group"         = aws_cloudwatch_log_group.log_group.name
        }
      }
    }
  ])
}

resource "aws_default_vpc" "default" {
}


variable "container_port" {
  default = 9003
  type = number
}

resource "aws_security_group" "ecs_container_instance" {
  name        = "Protohackers_ECS_Task_SecurityGroup"
  description = "Security group for ECS task running on Fargate"
  vpc_id      = aws_default_vpc.default.id

  ingress {
    description     = "Allow ingress traffic"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all egress traffic"
    from_port   = 0
    to_port     = 0
    protocol    = -1
    cidr_blocks = ["0.0.0.0/0"]
  }
}

variable "az_count" {
  default = 2
  type = number
}

data "aws_availability_zones" "available" {}

resource "aws_default_subnet" "default1" {
  availability_zone = "eu-north-1a"
}
resource "aws_default_subnet" "default2" {
  availability_zone = "eu-north-1b"
}
resource "aws_default_subnet" "default3" {
  availability_zone = "eu-north-1c"
}

resource "aws_ecs_service" "service" {
  name                               = "protohackers_ecs_service"
  cluster                            = aws_ecs_cluster.protohackers_cluster.id
  task_definition                    = aws_ecs_task_definition.echo_service_task.arn
  desired_count                      = 1
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100
  launch_type                        = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.ecs_container_instance.id]
    subnets          = [aws_default_subnet.default1.id, aws_default_subnet.default2.id, aws_default_subnet.default3.id]
    assign_public_ip = true
  }
}
