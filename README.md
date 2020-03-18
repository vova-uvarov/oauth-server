# oauth-server
Ключи для подписи токенов можно (для prod нужно) указывать через config-server
```yaml
security:
  oauth2:
    authorization:
      jwt:
        key-store: file:/opt/keys/kyestore.jks
        key-alias: key
        key-store-password: customKeystorePassword
```
Генерировать так:


First things first. We must generate a KeyStore file. To do that, go your Java install dir and there you'll find a jar named "keytool". Now execute the following:

```
keytool -genkeypair -alias jwt -keyalg RSA -keypass password -keystore jwt.jks -storepass password
```

The command will generate a file called jwt.jks which contains the Public and Private keys.

Now let's export the public key:

```
keytool -list -rfc --keystore jwt.jks | openssl x509 -inform pem -pubkey
```
Copy from (including) "-----BEGIN PUBLIC KEY-----" to (including) "-----END PUBLIC KEY-----" and save it in a file. You'll need this later in your resource servers.
