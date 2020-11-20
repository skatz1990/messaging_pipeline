# Variables
variable "aws_access_keys" {
  type = map(string)
  description = "AWS Access Keys for terraform deployment"

  default = {
      access_key = ""
      secret_key = ""
      region = "us-west-2"
  }
}

variable "github_token" {
  type = string
  default = ""
}

variable "cluster_name" {
  type = string
  default = "msg-pipe-ecs-cluster"
}

variable "app_service_name" {
  type = string
  default = "msg-pipe-ecs-service"
}

#variable "kms_key" {
#  type = string
#  default = "s3_kms_key"
#  description = "KMS key for codepipeline"
#{
