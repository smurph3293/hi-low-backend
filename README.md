cd local
docker-compose up -d

####

mvn clean package
sam package --template-file template.yml --output-template-file output.yaml --s3-bucket com-hi-low-backend-infra --profile sean

sam deploy --template-file output.yaml --stack-name HiLowBackendTest --capabilities CAPABILITY_IAM --profile sean

sam local start-api