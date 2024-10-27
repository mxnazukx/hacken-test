
Steps to start service:

1. mvn clean package -Dmaven.test.skip
2. Add ETHEREUM_ENDPOINT value to .env file
3. docker-compose up --build
4. Wait till all services are up and running

API doc:
http://localhost:8080/swagger-ui/index.html#

Health check:
http://localhost:8080/actuator/health

Metrics:
http://localhost:8080/actuator/metrics
