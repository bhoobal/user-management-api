{{/*
Expand the name of the chart.
*/}}
{{- define "user-management-api.name" -}}
{{- .Chart.Name }}
{{- end }}

{{/*
Common labels applied to all resources.
*/}}
{{- define "user-management-api.labels" -}}
app: {{ include "user-management-api.name" . }}
version: {{ .Values.image.tag | quote }}
environment: {{ .Values.namespace.name }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels used by Deployment and Service to match pods.
*/}}
{{- define "user-management-api.selectorLabels" -}}
app: {{ include "user-management-api.name" . }}
{{- end }}
