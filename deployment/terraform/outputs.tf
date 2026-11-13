output "api_url" {
  description = "Base URL of the Franchise API"
  value       = "http://${aws_eip.app.public_ip}:8080"
}

output "swagger_ui_url" {
  description = "Swagger UI of the deployed API"
  value       = "http://${aws_eip.app.public_ip}:8080/swagger-ui/index.html"
}

output "ecr_repository_url" {
  description = "ECR repository where the application image is pushed"
  value       = aws_ecr_repository.app.repository_url
}
