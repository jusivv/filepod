# Filepod

A simple & security file upload/download service, depend on [file-repository 2.0.3](https://github.com/jusivv/file-repository).

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
Some parameter can be assigned with environment (only full name) or argument.
- FilepodConfigPath (c for short), specify the path of filepod configurations.
- ServerAddress (h for short), specify server binding address, default 0.0.0.0.
- ServerPort (p for short), specify server listening port, default 8080.
- ServerBaseDir (b for short, optional), specify server base directory.
- ServerContextPath (s for short), specify server context path, default /.
- LogbackConfigFile (l for short), specify logback config file, default logback.xml.

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
docker build -t filepod:1.3.0 .
# run & stop
docker-compose up -d
docker-compose down
```