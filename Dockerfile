# Use official Tomcat 10 with Java 17
FROM tomcat:10.1-jdk17

# Remove default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WAR file to Tomcat
COPY target/MentalJournalApp.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
