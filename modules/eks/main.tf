#data "aws_subnet_ids" "msg_pipe_subnet_ids" {
#  vpc_id = var.vpc_id
#}

#data "aws_subnet" "msg_pipe_subnets" {
#  for_each = data.aws_subnet_ids.msg_pipe_subnet_ids.ids
#  id       = each.value
#}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_region" "region" {
  name = "us-west-2"
}

data "aws_availability_zone" "az_1" {
  name = "us-west-2a"
}

data "aws_availability_zone" "az_2" {
  name = "us-west-2b"
}

# Create a VPC for the region associated with the AZ
resource "aws_vpc" "msg_pipe_vpc" {
  cidr_block = cidrsubnet("192.168.0.0/16", 4, var.region_number[data.aws_region.region.name])
}

resource "aws_subnet" "eks_subnet_1" {
  vpc_id     = aws_vpc.msg_pipe_vpc.id
  cidr_block = cidrsubnet(aws_vpc.msg_pipe_vpc.cidr_block, 4, var.az_number[data.aws_availability_zone.az_1.name_suffix])

  tags = {
    Name = "eks_subnet_1"
  }
}

resource "aws_subnet" "eks_subnet_2" {
  vpc_id     = aws_vpc.msg_pipe_vpc.id
  cidr_block = cidrsubnet(aws_vpc.msg_pipe_vpc.cidr_block, 4, var.az_number[data.aws_availability_zone.az_2.name_suffix])

  tags = {
    Name = "eks_subnet_2"
  }
}

resource "aws_eks_cluster" "msg_pipe_cluster" {
  name     = var.cluster_name
  role_arn = aws_iam_role.msg_pipe_role.arn

  vpc_config {
    subnet_ids = [aws_subnet.eks_subnet_1.id, aws_subnet.eks_subnet_2.id]
  }

  # Ensure that IAM Role permissions are created before and deleted after EKS Cluster handling.
  # Otherwise, EKS will not be able to properly delete EKS managed EC2 infrastructure such as Security Groups.
  depends_on = [
    aws_iam_role_policy_attachment.msg_pipe_AmazonEKSClusterPolicy,
    aws_iam_role_policy_attachment.msg_pipe_AmazonEKSVPCResourceController,
  ]
}

resource "aws_iam_role" "msg_pipe_role" {
  name = "msg-pipe-eks-cluster"

  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "eks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy_attachment" "msg_pipe_AmazonEKSClusterPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.msg_pipe_role.name
}

# Optionally, enable Security Groups for Pods
# Reference: https://docs.aws.amazon.com/eks/latest/userguide/security-groups-for-pods.html
resource "aws_iam_role_policy_attachment" "msg_pipe_AmazonEKSVPCResourceController" {
  policy_arn ="arn:aws:iam::aws:policy/AmazonEKSVPCResourceController"
  role       = aws_iam_role.msg_pipe_role.name
}
