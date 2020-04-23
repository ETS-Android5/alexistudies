# Copyright 2020 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

variable "project_id" {
  description = "The GCP project id"
  type        = string
}

variable "network_project_id" {
  description = "The project ID of the network host project."
  type        = string
}

variable "gke_region" {
  description = "The region to host the clusters in"
  type        = string
}

variable "network" {
  description = "The network to use for the clusters"
  type        = string
  default     = "default"
}

variable "subnetwork" {
  description = "The subnetwork to use for the clusters"
  type        = string
  default     = "default"
}

variable "repo_owner" {
  description = "Owner of the GitHub repo which contains the definitions of Docker images used by GKE"
  type        = string
}

variable "repo_name" {
  description = "Name of the GitHub repo which contains the definitions of Docker images used by GKE"
  type        = string
}

variable "cloudbuild_trigger_branch" {
  type    = string
  default = "master"
}
