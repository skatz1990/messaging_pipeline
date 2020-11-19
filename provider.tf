# Declare provider
provider "aws" {
  access_key      = lookup(var.aws_access_keys, "access_key")
  secret_key      = lookup(var.aws_access_keys, "secret_key")
  region          = lookup(var.aws_access_keys, "region")
}

