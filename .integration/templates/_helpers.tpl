{{/* vim: set filetype=mustache: */}}
{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "playground.fullname" -}}
{{- printf "%s-%s" .Release.Name "kafka-playground" | trunc 63 -}}
{{- end -}}

{{- define "playground.zookeeper" -}}
{{- printf "%s-%s" .Release.Name "zookeeper" | trunc 63 -}}
{{- end -}}

{{- define "playground.zookeeper-svc" -}}
{{- printf "%s-%s" .Release.Name "zookeeper-svc" | trunc 63 -}}
{{- end -}}

{{- define "playground.kibana" -}}
{{- printf "%s-%s" .Release.Name "kibana" | trunc 63 -}}
{{- end -}}

{{- define "playground.kibana-svc" -}}
{{- printf "%s-%s" .Release.Name "kibana-svc" | trunc 63 -}}
{{- end -}}

{{- define "playground.elasticsearch" -}}
{{- printf "%s-%s" .Release.Name "elasticsearch" | trunc 63 -}}
{{- end -}}

{{- define "playground.elasticsearch-svc" -}}
{{- printf "%s-%s" .Release.Name "elasticsearch-svc" | trunc 63 -}}
{{- end -}}

{{- define "playground.cassandra" -}}
{{- printf "%s-%s" .Release.Name "cassandra" | trunc 63 -}}
{{- end -}}

{{- define "playground.cassandra-svc" -}}
{{- printf "%s-%s" .Release.Name "cassandra-svc" | trunc 63 -}}
{{- end -}}

{{- define "playground.kafka" -}}
{{- printf "%s-%s" .Release.Name "kafka" | trunc 63 -}}
{{- end -}}

{{- define "playground.kafka-svc" -}}
{{- printf "%s-%s" .Release.Name "kafka-svc" | trunc 63 -}}
{{- end -}}

{{- define "playground.consumer" -}}
{{- printf "%s-%s" .Release.Name "consumer" | trunc 63 -}}
{{- end -}}

{{- define "playground.producer" -}}
{{- printf "%s-%s" .Release.Name "producer" | trunc 63 -}}
{{- end -}}