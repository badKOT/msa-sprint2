На будущее:
Для первого задания:

```shell
> cd msa-sprint2/hotelio-monolith
> docker-compose up -d --build
```

Для второго задания:

```shell
> cd msa-sprint2/booking-service
> ./gradlew build
> cd ../tasks/task2
> docker-compose up -d --build
```

http://localhost:8084/api/bookings?userId=123

Для третьего задания:

```shell
%команды_из_второго_задания% # чтобы запустить микросервисы, которые будут обрабатывать запросы
> cd ../../tests
%команды_из_readme% # чтобы наполнить бд записями
> cd ../tasks/task3
> docker-compose up -d --build
```

localhost:4000

> Just a quick tip: для multiline комманд в Powershell можно использовать backtick (\`)

Для четвертого задания:
Dockerfile:

```shell
> cd task4
> docker build -t task4-booking-service -f "booking-service/Dockerfile" .
> docker run -p "8080:8080" -d -e "ENABLE_FEATURE_X=true" --name "go-booking" task4-booking-service
```

Для выполнения задания я пушил dockerfile и helm-чарт а artifact registry.

Dockerfile:

```shell
> docker login <registry_name>.cr.cloud.ru -u <key_id> -p <key_secret>
> docker tag task4-booking-service:1.0.0 <registry_name>.cr.cloud.ru/task4-booking-service:1.0.0
> docker push <registry_name>.cr.cloud.ru/task4-booking-service:1.0.0
```

Helm:

```shell
> cd task4/helm/booking-service
> docker build -t helm-pusher .
> docker run --rm -v "$(pwd):/output" -e "REGISTRY_NAME=<your_registry_name>" -e "KEY_ID=<your_api_key_id>" -e "KEY_SECRET=<your_api_key_secret>" --entrypoint sh helm-pusher -c "/push-chart.sh && cp *.tgz /output/"
```

REGISTRY_NAME, KEY_ID, KEY_SECRET положил в bitwarden.

Как я вручную задеплоил эту штуку в minikube:

```shell
> docker pull <your_registry_name>.cr.cloud.ru/task4-booking-service:1.0.0
> helm pull oci://<your_registry_name>.cr.cloud.ru/task4-helm/booking-service --version 1.0.0
> docker tag <your_registry_name>.cr.cloud.ru/task4-booking-service:1.0.0 task4-booking-service:latest # потому что именно task4-booking-service:latest указан в helm-чарте, при отличавшемся названии/теге получали статус пода ErrImageNeverPull.
> minikube image load task4-booking-service:latest
> helm install booking-service ./booking-service-1.0.0.tgz
> kubectl get pods
NAME 
booking-service-...
```

Как я проверил работу DNS и вообще работу подов:

```shell
> helm install def-not-booking-service ./booking-service-1.0.0.tgz
> kubectl get pods
NAME
booking-service-5f...
def-not-booking-service-5d...
> kubectl exec def-not-booking-service-5d... -- curl http://booking-service/ping
pong
> kubectl exec booking-service-5f... -- curl http://def-not-booking-service/feature
Feature X is enabled!
> helm delete booking-service
> helm delete def-not-booking-service
> minikube image rm <image-name>:<image-tag>
```

Как я автоматизировал эту фигню:

```shell
# На сервере с установленными git, docker, gitlab-ci-local, helm, minikube, с выпущенным в гитхабе Personal Access Token со всеми полномочиями
> git pull
> cd tasks/task4
> gitlab-ci-local build test tag deploy
> bash ./check-dns
```