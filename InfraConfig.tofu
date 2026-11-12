terraform {
  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "5.28.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "5.10.0"
    }
  }
}

provider "azurerm" {
  features {}
  subscription_id = ""
}

resource "azurerm_resource_group" "rg" {
  location = "Southeast Asia"
  name     = "Nautilus"
}

provider "cloudflare" {

}
