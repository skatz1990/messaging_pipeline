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

resource "aws_subnet" "msg_pipe_subnet_1" {
  vpc_id     = aws_vpc.msg_pipe_vpc.id
  cidr_block = cidrsubnet(aws_vpc.msg_pipe_vpc.cidr_block, 4, var.az_number[data.aws_availability_zone.az_1.name_suffix])

  tags = {
    Name = "msg_pipe_subnet_1"
  }
}

resource "aws_subnet" "msg_pipe_subnet_2" {
  vpc_id     = aws_vpc.msg_pipe_vpc.id
  cidr_block = cidrsubnet(aws_vpc.msg_pipe_vpc.cidr_block, 4, var.az_number[data.aws_availability_zone.az_2.name_suffix])

  tags = {
    Name = "msg_pipe_subnet_2"
  }
}

resource "aws_security_group" "msg_pipe_sg_1" {
  name        = "msg_pipe_sg_1"
  description = "Allow TLS inbound traffic"
  vpc_id      = aws_vpc.msg_pipe_vpc.id

  ingress {
    description = "TLS from VPC"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.msg_pipe_subnet_1.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "msg_pipe_sg_1"
  }
}

resource "aws_security_group" "msg_pipe_sg_2" {
  name        = "msg_pipe_sg_2"
  description = "Allow TLS inbound traffic"
  vpc_id      = aws_vpc.msg_pipe_vpc.id

  ingress {
    description = "TLS from VPC"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.msg_pipe_subnet_2.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "msg_pipe_sg_1"
  }
}