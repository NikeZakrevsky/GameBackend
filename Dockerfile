# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл сборки (например, jar файл) в контейнер
COPY out/artifacts/Game_jar/Game.jar mygame-server.jar

# Указываем команду для запуска сервера
CMD ["java", "-jar", "mygame-server.jar"]

# Если нужно, укажите порт, который будет использоваться
EXPOSE 8080
