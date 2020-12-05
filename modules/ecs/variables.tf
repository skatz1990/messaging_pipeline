variable "cluster_name" {
  type = string
  default = "msg-pipe-ecs-cluster"
}

variable "app_service_name" {
  type = string
  default = "msg-pipe-ecs-service"
}

variable "ami" {
  type = string
  default = "ami-07dd19a7900a1f049"
}

variable "vpc_id" {}
variable "msg_pipe_subnet_1" {}
variable "msg_pipe_subnet_2" {}
