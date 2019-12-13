bash -c "cat << EOF > /tmp/namespace$1
apiVersion: core/v1beta1
kind: Namespace
metadata:
  name: $1
EOF"

http :7658/apis/core/v1beta1/namespaces/$1 Content-Type:application/yaml Accept:application/yaml < /tmp/namespace$1

rm /tmp/namespace$1