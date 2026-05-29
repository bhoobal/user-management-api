# ── Stage 1: Build + custom JRE ───────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
# Build jar, then unzip it so BOOT-INF/classes and BOOT-INF/lib are accessible
# as plain directories — this lets the runtime use the standard system class
# loader instead of Spring Boot's LaunchedClassLoader / JarUrlClassLoader.
RUN mvn clean package -DskipTests -q && \
    mkdir -p /app/extracted && \
    cd /app/extracted && jar -xf /app/target/user-management-1.0.0.jar

# Build a minimal JRE. Modules cover Spring Boot 4 + JPA + H2 + Actuator + Validation.
# java.desktop     → java.beans (PropertyEditorSupport) required by Spring data binding
# java.management.rmi / jdk.management.agent → Spring Boot Actuator JMX
# java.transaction.xa → JTA used by Spring @Transactional
# java.sql.rowset  → JDBC RowSet API used by H2
# jdk.unsupported  → sun.misc.Unsafe needed by CGLIB, Byte Buddy, H2
# jdk.security.auth → JAAS used by Spring Security internals
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.management.rmi,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto,jdk.attach,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.httpserver,jdk.jfr,jdk.management,jdk.management.agent,jdk.naming.dns,jdk.naming.rmi,jdk.net,jdk.security.auth,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /custom-jre

# ── Stage 2: Runtime (distroless base + custom JRE) ──────────────────────────
FROM gcr.io/distroless/base-debian12:nonroot

COPY --from=builder /custom-jre /opt/java/jre

WORKDIR /app

COPY --from=builder /app/extracted/BOOT-INF/classes ./classes
COPY --from=builder /app/extracted/BOOT-INF/lib     ./lib

EXPOSE 8080

# -cp bypasses Spring Boot's LaunchedClassLoader entirely. The standard system
# class loader delegates java.beans.* to the platform class loader, which
# resolves them correctly from the java.desktop module in the custom JRE.
ENTRYPOINT ["/opt/java/jre/bin/java", "-Djava.security.egd=file:/dev/./urandom", "-Djava.awt.headless=true", "-cp", "/app/classes:/app/lib/*", "com.api.usermanagement.UserManagementApplication"]
