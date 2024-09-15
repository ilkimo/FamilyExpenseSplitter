# Stage 1: Build the application
FROM gradle:8.1.1-jdk17 AS builder

WORKDIR /app

# Copy build files
COPY build.gradle.kts settings.gradle.kts /app/
COPY gradlew /app/
COPY gradle /app/gradle

# Copy source code
COPY src /app/src

# Build the application using the 'shadowJar' task
RUN ./gradlew clean shadowJar --no-daemon

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

WORKDIR /app

# Install necessary native libraries
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user with a dynamic UID/GID passed during build
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN groupadd -g ${GROUP_ID} mygroup && \
    useradd -m -u ${USER_ID} -g mygroup myuser

# Copy the built shadow jar file from the builder stage
COPY --from=builder /app/build/libs/ExpenseManager-1.0-SNAPSHOT-all.jar /app/application.jar

# Copy resource files
COPY src/main/resources /app/src/main/resources

# Create outputs directory and set permissions for the non-root user
RUN mkdir -p /app/outputs && chown -R myuser:mygroup /app

# Switch to the non-root user
USER myuser

# Set the entry point
CMD ["java", "-jar", "application.jar"]
