# Filepod

A simple & security file upload/download service, depend on [file-repository 2.0.3](https://github.com/jusivv/file-repository).

## Usage

## Build 

```shell
  mvn clean package -U -am -pl boot -Dmaven.test.skip=true
```

You can get a fat jar named 'filepod-standalone.jar in boot/target'

## Configuration

### client.yml

List client(s) who use filepod.

```yaml
  <clientId>:
    scope: <clientId, ... or *> # who's file will be accessed, default itself
    accessController: <totp|concrete|concrete_v0.4.x|session>
    fileRepository: <local|alioss>
    defaultCipher: <aes.v2|aes.v1> # default cipher for stored file aes.v2 (recommended): CTR; aes.v1: CFB
    serverKey: <keyInBase64>
    totpSecret: <secretInBase64> # only for accessController=totp
```

### file-repository-<local|alioss>.yml

- At least one file repository to save file.
- Depend on [file-repository](https://github.com/jusivv/file-repository), currently only "local" supported.
- Yaml file content is arguments for creating file repository

### org.coodex.filepod.filter.ApacheCorsFilterFacade.yml

Configurations for Apache CorsFilter

### logback configuration files

Custom logback configuration files, can be assigned with environment "LogbackConfigFile" or argument "-l".

## Run

```shell
java -jar filepod-standalone.jar -c=<configPath>
```

filepod service will listening on 8080 port.

Some parameter can be assigned with environment (only full name) or argument.
- FilepodConfigPath (c for short), specify the path of filepod configurations.
- ServerAddress (h for short), specify server binding address, default 0.0.0.0.
- ServerPort (p for short), specify server listening port, default 8080.
- ServerBaseDir (b for short, optional), specify server base directory.
- ServerContextPath (s for short), specify server context path, default /.
- LogbackConfigFile (l for short), specify logback config file, default logback.xml.

## Modules

### filepod-api

Define the API of access controller & stream wrapper.

### filepod-webapp

Http servlet for upload/download, base on Servlet 4.0.

- Support file encryption
- Support multi-files download (in a zip)
- Support download by range (only single file download)

### filepod-security

Default implements of access controller & stream wrapper (for encryption)

### filepod-boot

Startup service with main class org.coodex.filepod.boot.Launcher.

## Upload

- upload by form
- upload url: http(s)://\<fileserver\>/attachments/upload/byform/{clientId}/{token}/{encrypt}
- clientId: file-client id
- token: file access token (session token or one-time-password)
- encrypt: 1 - save the encryption file, else save origin file
- support multi-upload
- response a array contains file information, for example:

```json
  [{
    "client": "test",
    "fileName": "image.jpg",
    "extName": "jpg",
    "fileId": "test$9813ada076f04c949e19eefcd9a5c4d0",
    "fileSize": 80384,
    "contentType": "image/jpeg",
    "cipherModel": "aes.v2"
  }]
```

## Download

- download url: http(s)://\<fileserver\>/attachments/download/{fileId};c={clientId};t={token}
- clientId: file-client id
- token: file access token (session token or one time password)
- fileId: file id which you want to download
- you can join fileId with "," to download multi-files, files will be packed in a zip file
- Support "RANGE" header (only single file & single range)

## Delete

- delete url: http(s)://\<fileserver\>/attachments/delete/{clientId}/{token}/{fileId,fileId,...}
- clientId: file-client id who has permission to delete files ("<clientId>.deletable" option in client.yml is true)
- token: file access token (session token or one time password)
- fileId: file id which you want to delete
- you can join fileId with "," to delete multi-files
- response status is 200 when success to delete file, response content: 

```json
  [{
    "client": "test",
    "fileName": "image.jpg",
    "extName": "jpg",
    "fileId": "test$9813ada076f04c949e19eefcd9a5c4d0",
    "fileSize": 80384,
    "contentType": "image/jpeg",
    "cipherModel": "aes.v2"
  }]
```

## Access controller

Implement in filepod-security

### TOTP based

Apply to server-to-server.
Authenticate by Time-based One Time Password.

### Client feedback

Apply to user-browser.
Authenticate by asking client service. 
Support Concrete framework, see filepod.manual.concrete.md.

#### Common feedback

- Request file-client by url http://\<host\>:\<port\>/\<path\>/\[ writable | readable | deletable \]
- Request Content-Type is 'application/json'
- Request body sample:

```JSON
  {
    "token": "file-client-user-token"
  }
```
or
```JSON
  {
    "token": "file-client-user-token",
    "fileId": "file-id or file-ids split with ','"
  }
```

- Response status 200 means allowed

## Dockerize

```shell
  # build
  docker build -t coodex/filepod:<latest release version> .
  # run & stop
  docker-compose up -d
  docker-compose down
```

## Install filepod as a linux system service

- Execute "systemctl --version", need 232 or newer
- Move filepod jar into your path, for example

```shell
  mv filepod-standalone.jar /usr/bin/filepod.jar
```

- Put your config files into directory /etc/filepod

- Create service file: /etc/systemd/system/filepod.service

```properties
  [Unit]
  Description=filepod
  After=network.target network-online.target
  Requires=network-online.target
  [Service]
  Type=simple
  ExecStart=java -jar /usr/bin/filepod.jar -c /etc/filepod
  ExecReload=/bin/kill -s HUP $MAINPID
  ExecStop=/bin/kill -s QUIT $MAINPID
  [Install]
  WantedBy=multi-user.target
```

- Create environment variables file for logback: /etc/systemd/system/filepod.service.d/logback.conf (file name must ends in ".conf")

```properties
  [Service]
  Environment="LogbackConfigFile=logback-file.xml"
  Environment="LOG_HOME=/var/log/filepod"
```

- Reload & enable filepod service

```shell
  sudo systemctl daemon-reload
  sudo systemctl enable --now filepod
```

- Verify that it is running

```shell
  systemctl status filepod
```

- Now filepod is ready to service you