FROM eclipse-temurin:21
WORKDIR /app
COPY . .
RUN chmod +x mvnw || true
CMD ["sh", "-c", "./mvnw spring-boot:run || mvn spring-boot:run"]