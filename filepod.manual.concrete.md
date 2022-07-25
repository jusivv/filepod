# Filepod 配合 Concrete 使用

支持 Concrete [0.4.X](https://github.com/coodex2016/concrete.coodex.org/tree/0.4.x) 和 [0.2.x](https://github.com/coodex2016/concrete.coodex.org/tree/0.2.x)

## Concrete 项目中的设置

- 增加对concrete-core-spring、concrete-attachments-jaxrs（传递依赖了concrete-attachments）的依赖
- 将 org.coodex.concrete.attachments.client.ClientServiceImpl 类注册为 REST 服务
- 注入 org.coodex.concrete.spring.aspects.ConcreteAOPChain 类（如已引入 org.coodex.concrete.spring.ConcreteSpringConfiguration 则自动注入），并将org.coodex.concrete.attachments.ClientAttachmentInterceptor 加入 ConcreteAOPChain
- 对于文件上传，用户登录成功后，执行方法“ClientServiceImpl.allowWrite()”，给予上传权限
- 对于文件下载，给包含 fileId 的 VO 属性上增加注解“org.coodex.concrete.attachments.Attachment”，在将 VO 呈现给前端时，拦截器会将加注解的属性值加入token，给予前端访问权限

## Filepod 项目中的设置

只需给对应的 client 配置合适的 Access Controller

```yaml
<clientId>:
  scope: "clientId1,clientId2" # 访问范围, * 表示无限制
  accessController: "concrete_v0.4.0" # 如使用 0.2.x，则为 "concrete"
  clientUrl: "http://<client server>/Client" # client 服务地址
  fileRepository: "local" # 使用的仓库，使用配置文件"file-repository-<fileRepository>.yml"中的配置
```
