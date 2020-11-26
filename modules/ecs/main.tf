# Creating ECR repo
resource "aws_ecr_repository" "msg-pipe-ecr-repo" {
  name                 = "msg-pipe-ecr-repo"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_repository_policy" "msg-pipe-ecr-policy" {
  repository = aws_ecr_repository.msg-pipe-ecr-repo.name

  policy = <<EOF
{
    "Version": "2008-10-17",
    "Statement": [
        {
            "Sid": "new policy",
            "Effect": "Allow",
            "Principal": "*",
            "Action": [
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:BatchCheckLayerAvailability",
                "ecr:PutImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload",
                "ecr:DescribeRepositories",
                "ecr:GetRepositoryPolicy",
                "ecr:ListImages",
                "ecr:DeleteRepository",
                "ecr:BatchDeleteImage",
                "ecr:SetRepositoryPolicy",
                "ecr:DeleteRepositoryPolicy"
            ]
        }
    ]
}
EOF
}

#data "aws_ecr_image" "nginx-image" {
#  repository_name = aws_ecr_repository.msg-pipe-ecr-repo.name
#  image_tag       = "latest"
#}

# Creating ECS task, cluster and service. As well as fargate
resource "aws_ecs_task_definition" "msg-pipe-ecs-task" {
  family                = "msg-pipe-services"
  container_definitions = file("task-definitions/services.json")
#  requires_compatibilities = ["FARGATE"]
#  cpu = 4096
#  memory = 8192

  placement_constraints {
    type       = "memberOf"
    expression = "attribute:ecs.availability-zone in [us-west-2a, us-west-2b]"
  }
}

resource "aws_ecs_cluster" "msg-pipe-ecs-cluster" {
    name = var.cluster_name

}

resource "aws_ecs_service" "msg-pipe-ecs-service" {
  name            = "msg-pipe-ecs-service"
  cluster         = aws_ecs_cluster.msg-pipe-ecs-cluster.id
  task_definition = aws_ecs_task_definition.msg-pipe-ecs-task.arn
  desired_count   = 1
#  launch_type     = "FARGATE"
#  iam_role        = aws_iam_role.msg-pipe-ecs-role.arn
  depends_on      = [aws_ecs_cluster.msg-pipe-ecs-cluster]

  ordered_placement_strategy {
    type  = "binpack"
    field = "cpu"
  }

#  load_balancer {
#    target_group_arn = aws_lb_target_group.foo.arn
#    container_name   = "mongo"
#    container_port   = 8080
#  }

  placement_constraints {
    type       = "memberOf"
    expression = "attribute:ecs.availability-zone in [us-west-2a, us-west-2b]"
  }
}
