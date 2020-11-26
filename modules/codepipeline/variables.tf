variable "cluster_name" {
  type = string
  default = ""
}

variable "service_name" {
  type = string
  default = ""
}

variable "github_token" {
  type = string
  default = ""
}

variable "kms_key_alias" {
  type = string
  default = "s3_kms_key"
}

variable "kms_key" {
  type = string
  default = ""
}

variable "kms_key_arn" {
  type = string
  default = "KMS ARN"
}

variable "kms_key_description" {
  type = string
  default = "KMS key for S3 objects"
}

variable "tags" {
  type = map(string)

  default = {
    "Purpose" = "Demo",
    "CostCenter" = "infra"
  }
}

variable "kms_deletion_window_in_days" {
  type = string
  default = 7
}
