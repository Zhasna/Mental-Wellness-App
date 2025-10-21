# Use Maven + JDK 17 image to build
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build WAR without running tests
RUN mvn clean package -DskipTests

# Use Tomcat image to run WAR
FROM tomcat:10.1-jdk17

# Remove default ROOT
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR from build stage
COPY --from=build /app/target/MentalJournalApp.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
