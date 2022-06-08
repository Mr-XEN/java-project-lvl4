FROM gradle:7.3.3-jdk17

WORKDIR /app

COPY . .

RUN gradle installDist

CMD build/install/app/bin/app