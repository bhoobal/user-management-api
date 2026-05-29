#!/bin/bash
set -e

IMAGE_NAME="user-management-api"
IMAGE_TAG="1.0.11"
NAMESPACE="development"
HELM_RELEASE="user-management-api"
HELM_CHART="helm/user-management-api"

echo "=================================================="
echo "  User Management API - Build & Deploy Script"
echo "=================================================="

# Step 1: Build Docker image
echo ""
echo "[1/4] Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}..."
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
echo "      ✅ Docker image built successfully"

# Step 2: Sync Chart.yaml appVersion with the image tag so helm history,
#         helm status, and resource labels all show the same version.
echo ""
echo "[2/4] Syncing Chart.yaml appVersion → ${IMAGE_TAG}..."
sed -i '' "s/^appVersion:.*/appVersion: \"${IMAGE_TAG}\"/" ${HELM_CHART}/Chart.yaml
echo "      ✅ Chart.yaml updated"

# Step 3: Deploy via Helm (--install makes this idempotent: first run = install,
#         subsequent runs = upgrade).
echo ""
echo "[3/4] Deploying Helm release '${HELM_RELEASE}'..."
helm upgrade --install ${HELM_RELEASE} ${HELM_CHART} \
  --namespace ${NAMESPACE} \
  --create-namespace \
  --set image.tag=${IMAGE_TAG}
echo "      ✅ Helm release applied"

# Step 4: Wait for rollout
echo ""
echo "[4/4] Waiting for rollout..."
kubectl rollout status deployment/${IMAGE_NAME} -n ${NAMESPACE} --timeout=120s
echo "      ✅ Deployment is live"

echo ""
echo "=================================================="
echo "  ✅ Deployment complete!"
echo "=================================================="
echo ""
echo "  helm status  ${HELM_RELEASE} -n ${NAMESPACE}"
echo "  helm history ${HELM_RELEASE} -n ${NAMESPACE}"
echo "  kubectl get pods -n ${NAMESPACE} --show-labels"
echo ""
echo "In Docker Desktop - port-forward to reach the service:"
kubectl port-forward svc/${IMAGE_NAME}-service 8081:80 -n ${NAMESPACE}
echo ""
echo "  API: http://localhost:8081/api/v1/"
echo "  Health: http://localhost:8081/actuator/health"
echo ""
echo "  POST   /api/v1/users"
echo "  GET    /api/v1/users"
echo "  GET    /api/v1/users/{id}"
echo "  POST   /api/v1/users/{userId}/groups/{groupId}"
echo "  DELETE /api/v1/users/{userId}/groups/{groupId}"
echo "  DELETE /api/v1/users/{id}"
echo "  POST   /api/v1/groups"
echo "  GET    /api/v1/groups"
echo "  GET    /api/v1/groups/{id}"
echo "  DELETE /api/v1/groups/{id}"
echo "=================================================="
