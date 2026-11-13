locals {
  mongodb_host = trimprefix(mongodbatlas_advanced_cluster.this.connection_strings.standard_srv, "mongodb+srv://")
  mongodb_uri = format(
    "mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority&appName=%s",
    mongodbatlas_database_user.app.username,
    random_password.database.result,
    local.mongodb_host,
    var.database_name,
    var.project_name
  )
}

resource "aws_ssm_parameter" "mongodb_uri" {
  name        = "/${var.project_name}/mongodb-uri"
  description = "MongoDB Atlas connection string used by the application"
  type        = "SecureString"
  value       = local.mongodb_uri
}
