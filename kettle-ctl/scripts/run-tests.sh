echo $(pwd)/kettle-config.yaml
KETTLECONFIG=$(pwd)/kettle-config.yaml mvn test -DtrimStackTrace=false