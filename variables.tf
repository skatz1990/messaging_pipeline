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

variable "ami_id" {
  type = string
  description = "AMI ID"
  default = "ami-07dd19a7900a1f049"
}

variable "ssh_key" {
  type = string
  description = "SSH key for messaging instance"
  default = "messaging_key"
}

variable "user_data_file" {
  type = string
  description = "User data to setup messaging pipeline on EC2"
  default = "scripts/user_data.tpl"
}

