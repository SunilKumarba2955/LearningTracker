FROM eclipse-temurin:21-jdk

WORKDIR /workspace

COPY . .

CMD ["sh", "scripts/run-learntrack.sh"]
