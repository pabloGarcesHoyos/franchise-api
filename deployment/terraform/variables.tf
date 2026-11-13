variable "aws_profile" {
  description = "AWS CLI profile used to deploy"
  type        = string
  default     = "franchise"
}

variable "aws_region" {
  description = "AWS region for every resource"
  type        = string
  default     = "us-east-1"
}

variable "atlas_org_id" {
  description = "MongoDB Atlas organization ID, provided through TF_VAR_atlas_org_id"
  type        = string
}

variable "project_name" {
  description = "Name used for the Atlas project and the AWS resources"
  type        = string
  default     = "franchise-api"
}

variable "database_name" {
  description = "MongoDB database used by the application"
  type        = string
  default     = "franchises"
}

variable "instance_type" {
  description = "EC2 instance type, arm64 to match the application image"
  type        = string
  default     = "t4g.micro"
}

variable "image_tag" {
  description = "Image tag the instance pulls from ECR"
  type        = string
  default     = "latest"
}

variable "ecr_images_to_keep" {
  description = "Number of most recent images kept in ECR"
  type        = number
  default     = 5
}
