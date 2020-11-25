output "cluster_name" {
  value = aws_ecs_cluster.msg-pipe-ecs-cluster.name
}

output "service_name" {
  value = aws_ecs_service.msg-pipe-ecs-service.name
}


