test "${TRAVIS_PULL_REQUEST}" == "false" && \
    test "${TRAVIS_JDK_VERSION}" == "oraclejdk7" && \
    test "${TRAVIS_TAG}" != "" && \
    echo deploying && \
    mvn deploy --settings settings.xml
