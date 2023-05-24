# https://github.com/kubernetes-sigs/prometheus-adapter/blob/master/Dockerfile
FROM golang:latest as build

WORKDIR /go/src/sigs.k8s.io/prometheus-adapter
COPY go.mod .
COPY go.sum .
RUN go mod download

COPY pkg pkg
COPY cmd cmd
COPY Makefile Makefile

ARG amd64
RUN make prometheus-adapter

FROM gcr.io/distroless/static:latest

COPY --from=build /go/src/sigs.k8s.io/prometheus-adapter/adapter /
USER 65534
ENTRYPOINT ["/adapter"]

