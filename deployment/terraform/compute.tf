data "aws_ssm_parameter" "al2023_arm64" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_ec2_instance_type_offerings" "available" {
  location_type = "availability-zone"

  filter {
    name   = "instance-type"
    values = [var.instance_type]
  }
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "default-for-az"
    values = ["true"]
  }

  filter {
    name   = "availability-zone"
    values = data.aws_ec2_instance_type_offerings.available.locations
  }
}

resource "aws_security_group" "app" {
  name        = "${var.project_name}-app"
  description = "Inbound HTTP on 8080 for the Franchise API"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.app.id
  description       = "Franchise API"
  ip_protocol       = "tcp"
  from_port         = 8080
  to_port           = 8080
  cidr_ipv4         = "0.0.0.0/0"
}

resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.app.id
  description       = "ECR, SSM and MongoDB Atlas"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

locals {
  registry = split("/", aws_ecr_repository.app.repository_url)[0]
  image    = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"

  user_data = <<-EOT
    #!/bin/bash
    set -euo pipefail

    dnf install -y docker
    systemctl enable --now docker

    cat > /usr/local/bin/franchise-api-start <<'SCRIPT'
    #!/bin/bash
    set -euo pipefail
    aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${local.registry}
    docker pull ${local.image}
    umask 077
    printf 'MONGODB_URI=%s\n' "$(aws ssm get-parameter --region ${var.aws_region} --name ${aws_ssm_parameter.mongodb_uri.name} --with-decryption --query Parameter.Value --output text)" > /run/franchise-api.env
    docker rm -f franchise-api >/dev/null 2>&1 || true
    exec docker run --rm --name franchise-api -p 8080:8080 --memory 768m --env-file /run/franchise-api.env ${local.image}
    SCRIPT
    chmod 0755 /usr/local/bin/franchise-api-start

    cat > /etc/systemd/system/franchise-api.service <<'UNIT'
    [Unit]
    Description=Franchise API container
    Requires=docker.service
    After=docker.service network-online.target
    Wants=network-online.target
    StartLimitIntervalSec=0

    [Service]
    ExecStart=/usr/local/bin/franchise-api-start
    ExecStop=/usr/bin/docker stop franchise-api
    Restart=always
    RestartSec=30

    [Install]
    WantedBy=multi-user.target
    UNIT

    systemctl daemon-reload
    systemctl enable --now franchise-api.service
  EOT
}

resource "aws_instance" "app" {
  ami                         = data.aws_ssm_parameter.al2023_arm64.insecure_value
  instance_type               = var.instance_type
  subnet_id                   = sort(data.aws_subnets.default.ids)[0]
  vpc_security_group_ids      = [aws_security_group.app.id]
  iam_instance_profile        = aws_iam_instance_profile.app.name
  user_data                   = local.user_data
  user_data_replace_on_change = true

  metadata_options {
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  root_block_device {
    volume_type = "gp3"
    encrypted   = true
  }

  tags = {
    Name = var.project_name
  }

  lifecycle {
    ignore_changes = [ami]
  }
}

resource "aws_eip" "app" {
  domain = "vpc"

  tags = {
    Name = var.project_name
  }
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}
