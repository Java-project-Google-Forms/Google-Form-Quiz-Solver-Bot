# Используем легкий образ только для запуска (JRE), так как код уже скомпилирован
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Копируем созданный JAR-файл из папки сборки в образ
# Используем маску *.jar, чтобы не ошибиться в точном имени
COPY app/build/libs/*.jar app.jar

# Указываем Docker, как запускать бота
ENTRYPOINT ["java", "-jar", "app.jar"]