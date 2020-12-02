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

variable "vpc_id" {
  type = string
  default = "vpc-1b75d763"
}

variable "kms_key" {
  type = string
  default = ""
}

variable "kms_key_arn" {
  type = string
  default = ""
}

variable "github_token" {
  type = string
  default = ""
}
