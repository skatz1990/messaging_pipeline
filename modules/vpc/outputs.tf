output "msg_pipe_vpc" {
  value = aws_vpc.msg_pipe_vpc.id
}

output "msg_pipe_subnet_1" {
  value = aws_subnet.msg_pipe_subnet_1.id
}

output "msg_pipe_subnet_2" {
  value = aws_subnet.msg_pipe_subnet_2.id
}

output "msg_pipe_sg_1" {
  value = aws_security_group.msg_pipe_sg_1.id
}

output "msg_pipe_sg_2" {
  value = aws_security_group.msg_pipe_sg_2.id
}


