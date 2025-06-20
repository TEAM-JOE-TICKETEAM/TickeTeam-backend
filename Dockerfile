# --- 1단계: 애플리케이션 빌드 (Builder Stage) ---
# 빌드를 위한 베이스 이미지로 Gradle과 JDK 17이 포함된 이미지를 사용합니다.
# 'AS builder'는 이 단계를 'builder'라는 이름으로 명명합니다.
FROM gradle:8.5-jdk17 AS builder

# 컨테이너 내의 작업 디렉토리를 설정합니다.
WORKDIR /build

# 빌드에 필요한 파일들을 먼저 복사하여 도커의 레이어 캐시를 활용합니다.
# 이렇게 하면 소스 코드가 변경되지 않았을 경우, 매번 의존성을 새로 다운로드하지 않아 빌드 속도가 향상됩니다.
COPY build.gradle settings.gradle ./

# 애플리케이션 소스 코드를 복사합니다.
COPY src ./src

# Gradle wrapper를 사용한다면 아래 라인을 사용하세요.
# COPY gradlew .
# COPY gradle ./gradle
# RUN ./gradlew bootJar --no-daemon

# Gradle wrapper를 사용하지 않는다면 아래 라인을 사용하세요.
# '--no-daemon' 옵션은 CI/CD 및 컨테이너 환경에서 권장됩니다.
RUN gradle bootJar --no-daemon


# --- 2단계: 최종 이미지 생성 (Final Stage) ---
# 실제 애플리케이션 실행을 위한 베이스 이미지입니다.
# 빌드 단계보다 훨씬 가벼운 Java 런타임 이미지를 사용합니다.
FROM amazoncorretto:17

# 작업 디렉토리를 설정합니다.
WORKDIR /app

# 'builder' 단계에서 빌드된 실행 가능한 JAR 파일을 최종 이미지로 복사합니다.
# build/libs/ 디렉토리 아래의 모든 .jar 파일을 app.jar 라는 이름으로 복사합니다.
COPY --from=builder /build/build/libs/*.jar app.jar

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
# 이 명령어로 Spring Boot 애플리케이션이 실행됩니다.
ENTRYPOINT ["java", "-jar", "app.jar"]