# Объединяет файлы в один чарт и делает helm push в artifact registry
# Run with
# > docker build -t helm-pusher .
# > docker run --rm -v "$(pwd):/output" -e "REGISTRY_NAME=nightmare729" -e "KEY_ID=2ad87a9446a152d5471bb49ef54b4b50" -e "KEY_SECRET=bfefe40e2dce66ec51602d549769a31f" --entrypoint sh helm-pusher -c "/push-chart.sh && cp *.tgz /output/"
# Just a quick reminder: for multiline commands in Powershell use backtick (`)

FROM alpine/helm:latest
WORKDIR /helm

COPY Chart.yaml values.yaml ./
COPY templates/ ./templates/

# Create a script to package and push the chart
RUN echo '#!/bin/sh' > /push-chart.sh && \
    echo 'set -e' >> /push-chart.sh && \
    echo '' >> /push-chart.sh && \
    echo 'echo "Packaging Helm chart..."' >> /push-chart.sh && \
    echo 'helm package .' >> /push-chart.sh && \
    echo '' >> /push-chart.sh && \
    echo 'echo "Logging in to artifact registry..."' >> /push-chart.sh && \
    echo 'helm registry login $REGISTRY_NAME.cr.cloud.ru -u $KEY_ID -p $KEY_SECRET' >> /push-chart.sh && \
    echo '' >> /push-chart.sh && \
    echo 'echo "Pushing chart to OCI registry..."' >> /push-chart.sh && \
    echo 'helm push *.tgz "oci://$REGISTRY_NAME.cr.cloud.ru/task4-helm"' >> /push-chart.sh && \
    echo '' >> /push-chart.sh && \
    echo 'echo "Chart pushed successfully!"' >> /push-chart.sh && \
    chmod +x /push-chart.sh

CMD ["/push-chart.sh"]