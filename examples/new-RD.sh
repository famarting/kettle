curl --data-binary @$1 -H "Accept: application/yaml" -H "Content-Type: application/yaml" --connect-timeout 5 -X POST localhost:7658/apis/core/v1beta1/resourcesdefinitions/book-definition
