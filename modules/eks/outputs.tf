output "endpoint" {
  value = aws_eks_cluster.msg_pipe_cluster.endpoint
}

output "kubeconfig-certificate-authority-data" {
  value = aws_eks_cluster.msg_pipe_cluster.certificate_authority[0].data
}
