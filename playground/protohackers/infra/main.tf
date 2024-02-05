terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region = "eu-north-1"
}

resource "aws_ecr_repository" "protohackers-ecr" {
  name = "protohackers/echo-service"

  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecs_cluster" "protohackers-cluster" {
  name = "protohackers-ecs-cluster"
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

resource "aws_ecs_task_definition" "echo-service-task" {
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
          containerPort = 9003
          hostPort      = 9003
          protocol      = "tcp"
        }
      ]
    }
  ])
}
