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

variable "region_number" {
  # Arbitrary mapping of region name to number to use in
  # a VPC's CIDR prefix.
  default = {
    us-east-1      = 1
    us-west-1      = 2
    us-west-2      = 3
    eu-central-1   = 4
    ap-northeast-1 = 5
  }
}

variable "az_number" {
  # Assign a number to each AZ letter used in our configuration
  default = {
    a = 1
    b = 2
    c = 3
    d = 4
    e = 5
    f = 6
  }
}