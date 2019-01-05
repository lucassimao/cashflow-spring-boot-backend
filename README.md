# Cash flow - Springboot backend 
This is a pet project for cash flow management whose goal is to practice microservices architecture development.
The idea is to transform the excel spreadsheet found [here](https://luz.vc/planilhas-empresariais/planilha-de-fluxo-de-caixa-excel) 
in a webapp, using spring boot to implement the microservice on the backend and Angular on the frontend

## Tech stack
- Spring boot / Java
- MySQL
- Docker
- Angular/TypeScript 


## How to run with the development environment

- Ensure a properly configured MySQL database instance is running and create a new database to be used by this application  
- edit the properties __spring.datasource.url__, __spring.datasource.username__, __spring.datasource.password__ in the file  **src/main/resources/application.properties** and ensure they are properly configured for your enviroment. Property names are self explanatory.
- If you have apache maven (3.1+) on your system's classpath, just run:
> mvn spring-boot:run 
- otherwise anf if you are on a linux box, run:
> ./mvnw spring-boot:run
- ... on a windows machine
> ./mvnw.bat spring-boot:run


## How to run in production mode
	mvn -Dspring-boot.run.profiles=production spring-boot:run

## Scope and TODO
1. CRUD of income and expenses types
2. CRUD of expenses
3. CRUD of incomes
4. CRUD of Book entries
5. Annual, monthly and Daily dashboards for income and expenses tracking  
6. Cash flow reports
7. Cash flow goals - to define income and expenses goals in order to compare with the real cash flow
8. Demonstrative of fiscal year ( i.e  DRE - Demonstrativo de Resultado do Exercício)
9. Bills to pay and Bills to receive
10. Multitenancy
