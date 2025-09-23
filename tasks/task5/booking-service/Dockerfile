# Используем официальный минимальный образ Go
FROM golang:1.21-alpine

# Добавить необходимые пакеты (например, curl)
RUN apk add --no-cache curl

# Устанавливаем рабочую директорию
WORKDIR /app

# Открываем порт 8080
EXPOSE 8080
# Копируем Go-файл и скрипт проверки DNS
COPY booking-service/main.go .
COPY ../check-dns.sh .

# Сделать скрипт исполняемым
RUN chmod +x check-dns.sh

# Собрать бинарник из main.go
RUN go build -o booking-service main.go

# Задать команду запуска
CMD ["./booking-service"]
