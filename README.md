# Cash flow - Springboot backend 
This is a pet project for cash flow management whose goal is to practice microservices architecture development.
The idea is to transform the excel spreadsheet found [here](https://luz.vc/planilhas-empresariais/planilha-de-fluxo-de-caixa-excel) 
in a webapp, using spring boot to implement the microservice on the backend and Angular on the frontend

## Tech stack
- Spring boot / Java11/
- JWT
- MySQL
- Docker
- Angular/TypeScript 


## How to run with the development environment

- Ensure a properly configured MySQL database instance is running and create a new database to be used by this application  
- edit the properties __spring.datasource.url__, __spring.datasource.username__, __spring.datasource.password__ in the file  **src/main/resources/application.properties** and ensure they are properly configured for your enviroment. Property names are self explanatory.
- Use the following commands to create the mysql server container and databases
> docker pull mysql
> docker run --name mysql-db -p 3306:3306 -e MYSQL_ROOT_PASSWORD= ? -e MYSQL_USER=app -e MYSQL_PASSWORD=123 -e MYSQL_DATABASE=fluxo_de_caixa -d mysql
> docker exec -it mysql-db mysql -u root -p -e "create database fluxo_de_caixa_tests; grant all on fluxo_de_caixa_tests.* to app"
- If you have apache maven (3.1+) on your system's classpath, just run:
> mvn spring-boot:run 
- otherwise anf if you are on a linux box, run:
> ./mvnw spring-boot:run
- ... on a windows machine
> ./mvnw.bat spring-boot:run


## How to run in production mode
	mvn -Dspring-boot.run.profiles=production spring-boot:run

## Scope and TODO
1. CRUD of Book entries groups **[done]**
2. CRUD of Book entries ( expenses or incomes ) **[done]**
3. Signup api, Login with JWT and Multitenancy : **[done]**
   1. CRUD operations on book entries groups and book entries must be scoped to the logged user 
   2. Signup api must be public
4. Reports **[done]**
   1. Annual, monthly and Daily reports of income and expenses
6.  Cash flow goals - to define income and expenses goals in order to compare with the real cash flow **[done]**


## Reference and articles ##
1. [Java Money and the Currency API](https://www.baeldung.com/java-money-and-currency)
2. [Working with Relationships in Spring Data REST](https://www.baeldung.com/spring-data-rest-relationships)
3. [GLOSSÁRIO DE TERMOS USADOS NAS ÁREAS DE FINANÇAS, CONTABILIDADE E JURÍDICA ](https://www.sk.com.br/sk-fcj.html)
4. [Spring Boot - Unit Testing and Mocking with Mockito and JUnit](http://www.springboottutorial.com/spring-boot-unit-testing-and-mocking-with-mockito-and-junit)
5. [Spring Boot testing features](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)


## Tips
1. Creating a generic user
   > curl -i -H "content-type: application/json" -d '{"name":"Lucas Simão","email":"lsimaocosta@gmail.com","password":"123"}'  -v http://localhost:8080/users
2. Authenticatig as admin using curl in development enviroment
   > curl -i -H "content-type: application/json" -d '{"username":"admin@mycashflow.com","password":"123"}'  -v http://localhost:8080/login
3. Beteween the response headers from the previous request, will be the Authorization one. Just send the header value in any request
   > curl -i -H "content-type:application/json" -H "authorization: $AUTHORIZATION_TOKEN" -d '{"type":"Expense","description":"credit card" }'  http://localhost:8080/bookEntryGroups