echo $(pwd)/kettle-config.yaml
KETTLECONFIG=$(pwd)/kettle-config.yaml mvn clean install -DtrimStackTrace=false