# web_6Sem_hudz_kp21

Репозиторій студента групи КП-21 Гудзь Владислава для виконання лабораторної роботи

2025

Some useful commands:

* openssl pkcs12 -export -out cert.pfx -inkey keys/localhost+2-key.pem -in keys/localhost+2.pem
* keytool -import -trustcacerts -keystore "%JAVA_HOME%\jre\lib\security\cacerts" -storepass changeit -noprompt -alias casdoor -file /path/to/casdoor.crt