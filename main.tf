module "vpc" {
  source              =   "./modules/vpc"
}

module "ecs" {
  source = "./modules/ecs" 
  vpc_id              =   module.vpc.msg_pipe_vpc
  msg_pipe_subnet_1   =   module.vpc.msg_pipe_subnet_1
  msg_pipe_subnet_2   =   module.vpc.msg_pipe_subnet_2
}

// module "ec2" {
//   source = "./modules/ec2"
//   vpc_id = var.vpc_id
// }

// module "eks" {
//   source = "./modules/eks"
//   vpc_id = var.vpc_id
// }

// module "fargate" {
//   source = "./modules/fargate"
// }

module "codepipeline" {
  source              =   "./modules/codepipeline"
  cluster_name        =   module.ecs.cluster_name
  service_name        =   module.ecs.service_name
  github_token        =   var.github_token
  vpc_id              =   module.vpc.msg_pipe_vpc
  msg_pipe_subnet_1   =   module.vpc.msg_pipe_subnet_1
  msg_pipe_subnet_2   =   module.vpc.msg_pipe_subnet_2
  msg_pipe_sg_1       =   module.vpc.msg_pipe_sg_1
  msg_pipe_sg_2       =   module.vpc.msg_pipe_sg_2
  delete_bucket       =   "true"
}
