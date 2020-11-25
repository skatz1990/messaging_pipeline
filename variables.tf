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
