# Test tooling container: bash + curl + python3 + psql client.
# Used by run_tests.sh to execute the API and smoke scripts without requiring
# any of these tools on the host.
FROM alpine:3.20

RUN apk add --no-cache \
    bash \
    curl \
    python3 \
    postgresql16-client \
    ca-certificates \
    coreutils \
    grep \
    sed \
 && ln -sf /usr/bin/python3 /usr/bin/python

WORKDIR /workspace
CMD ["bash"]
