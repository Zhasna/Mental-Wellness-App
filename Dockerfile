# Use official Tomcat 10 with Java 17
FROM tomcat:10.1-jdk17-temurin-jammy

# Set working directory
WORKDIR /usr/local/tomcat

# Remove default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file from target directory
COPY target/MentalJournalApp.war /usr/local/tomcat/webapps/ROOT.war

# Create data directory for H2 database with proper permissions
RUN mkdir -p /usr/local/tomcat/data && \
    chmod 755 /usr/local/tomcat/data

# Set environment variables for production
ENV CATALINA_OPTS="-Xms256m -Xmx512m"
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# Expose Tomcat port (Render will map this to PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Start Tomcat
CMD ["catalina.sh", "run"]

