module "ecs" {
  source = "./modules/ecs" 
}

module "codepipeline" {
  source = "./modules/codepipeline"
  cluster_name = module.ecs.cluster_name
  service_name = module.ecs.service_name
  github_token = var.github_token
}

